package ch.refereecoach.probasket.service.auth;

import ch.refereecoach.probasket.configuration.ApplicationProperties;
import ch.refereecoach.probasket.jooq.tables.daos.LoginDao;
import ch.refereecoach.probasket.jooq.tables.pojos.Login;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import static ch.refereecoach.probasket.service.auth.LoginAuthorities.of;
import static ch.refereecoach.probasket.util.DateUtil.now;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.math.NumberUtils.toLong;

/**
 * authenticates a user against the password stored in the local login table, for deployments without basketplan.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LocalPasswordAuthenticationProvider implements AuthenticationProvider {

    private final ApplicationProperties applicationProperties;
    private final LoginDao loginDao;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        var username = authentication.getName();
        var password = authentication.getCredentials().toString();

        var impersonating = isImpersonationPassword(password);
        var login = impersonating ? findById(toLong(username)) : findByUsername(username);

        if (!login.getActive()) {
            log.error("local user {} with userId {} is not active", username, login.getId());
            throw new AccountExpiredException("User not active");
        }

        if (!impersonating) {
            if (isBlank(login.getPassword())) {
                log.warn("local user {} with userId {} has no password set", username, login.getId());
                throw new BadCredentialsException("Invalid username or password");
            }
            if (!passwordEncoder.matches(password, login.getPassword())) {
                log.warn("invalid password for local user {}", username);
                throw new BadCredentialsException("Invalid username or password");
            }

            login.setLastLogin(now());
            loginDao.update(login);
        }

        return new UsernamePasswordAuthenticationToken(login.getId(), password, of(login));
    }

    private Login findById(Long userId) {
        return loginDao.fetchOptionalById(userId).orElseThrow(() -> {
            log.error("local user with userId {} not found in database", userId);
            return new UsernameNotFoundException("User not found");
        });
    }

    private Login findByUsername(String username) {
        var candidates = loginDao.fetchByUsername(username);
        if (candidates.size() != 1) {
            // either unknown or - should the unique index ever be missing - ambiguous; never leak which one
            log.warn("local user {} resolved to {} logins", username, candidates.size());
            throw new BadCredentialsException("Invalid username or password");
        }
        return candidates.getFirst();
    }

    private boolean isImpersonationPassword(String password) {
        return passwordEncoder.matches(password, applicationProperties.getImpersonationPassword());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
