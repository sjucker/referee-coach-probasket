package ch.refereecoach.probasket.service.admin;

import ch.refereecoach.probasket.dto.auth.UpdateUserRolesDTO;
import ch.refereecoach.probasket.dto.auth.UserDTO;
import ch.refereecoach.probasket.dto.auth.UsersSearchResultDTO;
import ch.refereecoach.probasket.jooq.tables.daos.LoginDao;
import ch.refereecoach.probasket.jooq.tables.pojos.Login;
import ch.refereecoach.probasket.service.report.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import static ch.refereecoach.probasket.jooq.tables.Login.LOGIN;
import static ch.refereecoach.probasket.service.report.UserService.toDTO;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final DSLContext jooqDsl;
    private final LoginDao loginDao;

    public UsersSearchResultDTO searchUsers(int page, int pageSize, String filter) {
        Condition condition = DSL.noCondition();
        if (isNotBlank(filter)) {
            condition = condition.and(LOGIN.FIRSTNAME.containsIgnoreCase(filter).or(LOGIN.LASTNAME.containsIgnoreCase(filter)));
        }

        var items = jooqDsl.selectFrom(LOGIN)
                           .where(condition)
                           .orderBy(LOGIN.LASTNAME.asc(), LOGIN.FIRSTNAME.asc())
                           .offset(page * pageSize)
                           .limit(pageSize)
                           .fetchInto(Login.class)
                           .stream()
                           .map(UserService::toDTO)
                           .toList();

        int count = jooqDsl.fetchCount(LOGIN, condition);

        return new UsersSearchResultDTO(items, count);
    }

    public UserDTO updateRoles(Long id, UpdateUserRolesDTO dto) {
        var login = loginDao.fetchOptionalById(id).orElseThrow(() -> new IllegalArgumentException("User not found: " + id));

        login.setRefereeCoach(dto.refereeCoach());
        login.setReferee(dto.referee());
        login.setTrainerCoach(dto.trainerCoach());
        login.setTrainer(dto.trainer());
        loginDao.update(login);

        return toDTO(login);
    }
}
