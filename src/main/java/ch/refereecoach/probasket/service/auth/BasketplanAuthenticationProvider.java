package ch.refereecoach.probasket.service.auth;

import ch.refereecoach.probasket.configuration.ApplicationProperties;
import ch.refereecoach.probasket.jooq.tables.daos.LoginDao;
import ch.refereecoach.probasket.jooq.tables.pojos.Login;
import ch.refereecoach.probasket.service.basketplan.BasketplanAuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static ch.refereecoach.probasket.common.UserRole.ADMIN;
import static ch.refereecoach.probasket.common.UserRole.REFEREE;
import static ch.refereecoach.probasket.common.UserRole.REFEREE_COACH;
import static ch.refereecoach.probasket.common.UserRole.REFEREE_COACH_PLUS;
import static ch.refereecoach.probasket.common.UserRole.TRAINER;
import static ch.refereecoach.probasket.common.UserRole.TRAINER_COACH;
import static ch.refereecoach.probasket.util.DateUtil.now;
import static org.apache.commons.lang3.math.NumberUtils.toLong;

@Slf4j
@Component
@RequiredArgsConstructor
public class BasketplanAuthenticationProvider implements AuthenticationProvider {

    private final ApplicationProperties applicationProperties;
    private final LoginDao loginDao;
    private final BasketplanAuthenticationService basketplanAuthenticationService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        var username = authentication.getName();
        var password = authentication.getCredentials().toString();

        var impersonating = isImpersonationPassword(password);
        var userId = impersonating ? toLong(username) : basketplanAuthenticationService.authenticate(username, password)
                                                                                       .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        var login = loginDao.fetchOptionalById(userId).orElseThrow(() -> {
            log.error("basketplan-user {} with userId {} not found in database", username, userId);
            return new UsernameNotFoundException("User not found");
        });

        if (!login.getActive()) {
            log.error("basketplan-user {} with userId {} is not active", username, userId);
            throw new AccountExpiredException("User not active");
        }

        if (!impersonating) {
            login.setLastLogin(now());
            login.setUsername(username);
            loginDao.update(login);
        }

        return new UsernamePasswordAuthenticationToken(login.getId(), password, getAuthorities(login));
    }

    private boolean isImpersonationPassword(String password) {
        return passwordEncoder.matches(password, applicationProperties.getImpersonationPassword());
    }

    private List<GrantedAuthority> getAuthorities(Login login) {
        var authorities = new ArrayList<GrantedAuthority>();
        if (login.getRefereeCoach()) authorities.add(new SimpleGrantedAuthority(REFEREE_COACH.name()));
        if (login.getRefereeCoachPlus()) authorities.add(new SimpleGrantedAuthority(REFEREE_COACH_PLUS.name()));
        if (login.getReferee()) authorities.add(new SimpleGrantedAuthority(REFEREE.name()));
        if (login.getTrainerCoach()) authorities.add(new SimpleGrantedAuthority(TRAINER_COACH.name()));
        if (login.getTrainer()) authorities.add(new SimpleGrantedAuthority(TRAINER.name()));
        if (login.getAdmin()) authorities.add(new SimpleGrantedAuthority(ADMIN.name()));
        return authorities;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
