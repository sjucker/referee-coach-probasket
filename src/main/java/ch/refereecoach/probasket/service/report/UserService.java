package ch.refereecoach.probasket.service.report;

import ch.refereecoach.probasket.dto.auth.UserDTO;
import ch.refereecoach.probasket.jooq.tables.daos.LoginDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final LoginDao loginDao;

    public UserDTO getByBasketplanUsername(String username) {
        return findByBasketplanUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("user %s not found".formatted(username)));
    }

    public Optional<UserDTO> findByBasketplanUsername(String username) {
        return loginDao.fetchOptionalByBasketplanUsername(username)
                       .map(it -> new UserDTO(it.getId(),
                                              it.getBasketplanUsername(),
                                              it.getFirstname(),
                                              it.getLastname(),
                                              it.getEmail(),
                                              it.getRefereeCoach(),
                                              it.getReferee(),
                                              it.getTrainerCoach(),
                                              it.getTrainer(),
                                              it.getAdmin()));
    }
}
