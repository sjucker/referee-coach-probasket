package ch.refereecoach.probasket.service.auth;

import ch.refereecoach.probasket.configuration.ApplicationProperties;
import ch.refereecoach.probasket.dto.auth.ChangePasswordDTO;
import ch.refereecoach.probasket.jooq.tables.daos.LoginDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * manages the locally stored passwords. only meaningful when {@code probasket.auth-provider} is {@code LOCAL} -
 * with basketplan authentication the passwords live in basketplan and are never stored here.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordService {

    private final ApplicationProperties applicationProperties;
    private final LoginDao loginDao;
    private final PasswordEncoder passwordEncoder;

    public String encode(String rawPassword) {
        requireLocalAuthentication();
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * changes the password of the given user after verifying the current one.
     *
     * @throws IllegalArgumentException if the user does not exist or the current password does not match
     */
    public void changeOwnPassword(Long userId, ChangePasswordDTO dto) {
        requireLocalAuthentication();

        var login = loginDao.fetchOptionalById(userId)
                            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        if (isBlank(login.getPassword()) || !passwordEncoder.matches(dto.currentPassword(), login.getPassword())) {
            log.warn("password change for userId {} rejected: current password does not match", userId);
            throw new IllegalArgumentException("Current password does not match");
        }

        login.setPassword(passwordEncoder.encode(dto.newPassword()));
        loginDao.update(login);
        log.info("password changed for userId {}", userId);
    }

    /**
     * sets the password of the given user without knowing the current one (admin reset).
     *
     * @throws IllegalArgumentException if the user does not exist
     */
    public void setPassword(Long userId, String rawPassword) {
        requireLocalAuthentication();

        var login = loginDao.fetchOptionalById(userId)
                            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        login.setPassword(passwordEncoder.encode(rawPassword));
        loginDao.update(login);
        log.info("password set for userId {}", userId);
    }

    private void requireLocalAuthentication() {
        if (applicationProperties.isBasketplanAuthentication()) {
            throw new IllegalStateException("passwords are managed in basketplan, local password management is disabled");
        }
    }
}
