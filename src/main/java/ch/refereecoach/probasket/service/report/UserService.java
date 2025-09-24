package ch.refereecoach.probasket.service.report;

import ch.refereecoach.probasket.common.Rank;
import ch.refereecoach.probasket.dto.auth.UserDTO;
import ch.refereecoach.probasket.jooq.tables.daos.LoginDao;
import ch.refereecoach.probasket.jooq.tables.pojos.Login;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final LoginDao loginDao;

    public UserDTO getById(Long id) {
        return findById(id)
                .orElseThrow(() -> new IllegalArgumentException("user %s not found".formatted(id)));
    }

    public Optional<UserDTO> findById(Long id) {
        return loginDao.fetchOptionalById(id).map(UserService::toDTO);
    }

    public static UserDTO toDTO(Login it) {
        return new UserDTO(it.getId(),
                           it.getFirstname(),
                           it.getLastname(),
                           it.getEmail(),
                           Rank.of(it.getRank()).orElse(null),
                           it.getRefereeCoach(),
                           it.getRefereeCoachPlus(),
                           it.getReferee(),
                           it.getTrainerCoach(),
                           it.getTrainer(),
                           it.getAdmin(),
                           it.getActive());
    }
}
