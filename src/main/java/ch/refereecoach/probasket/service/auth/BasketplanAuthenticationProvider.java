package ch.refereecoach.probasket.service.auth;

import ch.refereecoach.probasket.jooq.tables.daos.LoginDao;
import ch.refereecoach.probasket.jooq.tables.pojos.Login;
import ch.refereecoach.probasket.service.basketplan.BasketplanAuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static ch.refereecoach.probasket.common.UserRole.ADMIN;
import static ch.refereecoach.probasket.common.UserRole.REFEREE;
import static ch.refereecoach.probasket.common.UserRole.REFEREE_COACH;
import static ch.refereecoach.probasket.common.UserRole.TRAINER;
import static ch.refereecoach.probasket.common.UserRole.TRAINER_COACH;
import static ch.refereecoach.probasket.util.DateUtil.now;

@Slf4j
@Component
@RequiredArgsConstructor
public class BasketplanAuthenticationProvider implements AuthenticationProvider {

    private final LoginDao loginDao;
    private final BasketplanAuthenticationService basketplanAuthenticationService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        var username = authentication.getName();
        var password = authentication.getCredentials().toString();

        if (basketplanAuthenticationService.authenticate(username, password)) {
            var login = loginDao.fetchOptionalByBasketplanUsername(username).orElseThrow(() -> {
                log.error("basketplan-user {} not found in database", username);
                return new UsernameNotFoundException("User not found");
            });

            login.setLastLogin(now());
            loginDao.update(login);

            return new UsernamePasswordAuthenticationToken(username, password, getAuthorities(login));
        } else {
            throw new BadCredentialsException("Invalid username or password");
        }
    }

    private List<GrantedAuthority> getAuthorities(Login login) {
        var authorities = new ArrayList<GrantedAuthority>();
        if (login.getRefereeCoach()) authorities.add(new SimpleGrantedAuthority(REFEREE_COACH.name()));
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
