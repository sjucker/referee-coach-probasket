package ch.refereecoach.probasket.service.auth;

import ch.refereecoach.probasket.jooq.tables.pojos.Login;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.List;

import static ch.refereecoach.probasket.common.UserRole.ADMIN;
import static ch.refereecoach.probasket.common.UserRole.REFEREE;
import static ch.refereecoach.probasket.common.UserRole.REFEREE_COACH;
import static ch.refereecoach.probasket.common.UserRole.REFEREE_COACH_PLUS;
import static ch.refereecoach.probasket.common.UserRole.TRAINER;
import static ch.refereecoach.probasket.common.UserRole.TRAINER_COACH;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LoginAuthorities {

    public static List<GrantedAuthority> of(Login login) {
        var authorities = new ArrayList<GrantedAuthority>();
        if (login.getRefereeCoach()) authorities.add(new SimpleGrantedAuthority(REFEREE_COACH.name()));
        if (login.getRefereeCoachPlus()) authorities.add(new SimpleGrantedAuthority(REFEREE_COACH_PLUS.name()));
        if (login.getReferee()) authorities.add(new SimpleGrantedAuthority(REFEREE.name()));
        if (login.getTrainerCoach()) authorities.add(new SimpleGrantedAuthority(TRAINER_COACH.name()));
        if (login.getTrainer()) authorities.add(new SimpleGrantedAuthority(TRAINER.name()));
        if (login.getAdmin()) authorities.add(new SimpleGrantedAuthority(ADMIN.name()));
        return authorities;
    }
}
