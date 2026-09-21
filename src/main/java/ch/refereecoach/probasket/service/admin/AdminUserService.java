package ch.refereecoach.probasket.service.admin;

import ch.refereecoach.probasket.dto.auth.CreateUserDTO;
import ch.refereecoach.probasket.dto.auth.UpdateUserRolesDTO;
import ch.refereecoach.probasket.dto.auth.UserDTO;
import ch.refereecoach.probasket.dto.auth.UsersSearchResultDTO;
import ch.refereecoach.probasket.jooq.tables.daos.LoginDao;
import ch.refereecoach.probasket.jooq.tables.pojos.Login;
import ch.refereecoach.probasket.service.auth.PasswordService;
import ch.refereecoach.probasket.service.report.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.SortField;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import java.io.Serializable;

import static ch.refereecoach.probasket.dto.auth.UserDTO.Fields.active;
import static ch.refereecoach.probasket.dto.auth.UserDTO.Fields.admin;
import static ch.refereecoach.probasket.dto.auth.UserDTO.Fields.email;
import static ch.refereecoach.probasket.dto.auth.UserDTO.Fields.firstName;
import static ch.refereecoach.probasket.dto.auth.UserDTO.Fields.rank;
import static ch.refereecoach.probasket.dto.auth.UserDTO.Fields.referee;
import static ch.refereecoach.probasket.dto.auth.UserDTO.Fields.refereeCoach;
import static ch.refereecoach.probasket.dto.auth.UserDTO.Fields.refereeCoachPlus;
import static ch.refereecoach.probasket.dto.auth.UserDTO.Fields.trainer;
import static ch.refereecoach.probasket.dto.auth.UserDTO.Fields.trainerCoach;
import static ch.refereecoach.probasket.jooq.Sequences.LOGIN_LOCAL_ID_SEQ;
import static ch.refereecoach.probasket.jooq.tables.Login.LOGIN;
import static ch.refereecoach.probasket.service.report.UserService.toDTO;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final DSLContext jooqDsl;
    private final LoginDao loginDao;
    private final PasswordService passwordService;

    public UsersSearchResultDTO searchUsers(int page, int pageSize, String filter, String sortBy, String sortOrder) {
        Condition condition = DSL.noCondition();
        if (isNotBlank(filter)) {
            condition = condition.and(LOGIN.FIRSTNAME.containsIgnoreCase(filter).or(LOGIN.LASTNAME.containsIgnoreCase(filter)));
        }

        var items = jooqDsl.selectFrom(LOGIN)
                           .where(condition)
                           .orderBy(getSortOrder(sortBy, sortOrder))
                           .offset(page * pageSize)
                           .limit(pageSize)
                           .fetchInto(Login.class)
                           .stream()
                           .map(UserService::toDTO)
                           .toList();

        int count = jooqDsl.fetchCount(LOGIN, condition);

        return new UsersSearchResultDTO(items, count);
    }

    private static SortField<? extends Serializable> getSortOrder(String sortBy, String sortOrder) {
        var descending = "desc".equalsIgnoreCase(sortOrder);
        return switch (sortBy) {
            case firstName -> descending ? LOGIN.FIRSTNAME.desc() : LOGIN.FIRSTNAME.asc();
            case email -> descending ? LOGIN.EMAIL.desc() : LOGIN.EMAIL.asc();
            case rank -> descending ? LOGIN.RANK.desc() : LOGIN.RANK.asc();
            case active -> descending ? LOGIN.ACTIVE.desc() : LOGIN.ACTIVE.asc();
            case refereeCoach -> descending ? LOGIN.REFEREE_COACH.desc() : LOGIN.REFEREE_COACH.asc();
            case refereeCoachPlus -> descending ? LOGIN.REFEREE_COACH_PLUS.desc() : LOGIN.REFEREE_COACH_PLUS.asc();
            case referee -> descending ? LOGIN.REFEREE.desc() : LOGIN.REFEREE.asc();
            case trainerCoach -> descending ? LOGIN.TRAINER_COACH.desc() : LOGIN.TRAINER_COACH.asc();
            case trainer -> descending ? LOGIN.TRAINER.desc() : LOGIN.TRAINER.asc();
            case admin -> descending ? LOGIN.ADMIN.desc() : LOGIN.ADMIN.asc();
            default -> descending ? LOGIN.LASTNAME.desc() : LOGIN.LASTNAME.asc();
        };
    }

    /**
     * creates a local user. only usable with local authentication - with basketplan authentication the users are
     * owned by basketplan and created by {@code BasketplanUserSyncService}.
     *
     * @throws IllegalArgumentException if the username is already taken
     */
    public UserDTO createUser(CreateUserDTO dto) {
        var password = passwordService.encode(dto.password());

        if (!loginDao.fetchByUsername(dto.username()).isEmpty()) {
            throw new IllegalArgumentException("Username already taken: " + dto.username());
        }

        // login.id is the basketplan personId for synced users, so local users draw from a disjoint high range
        var id = jooqDsl.nextval(LOGIN_LOCAL_ID_SEQ);

        var login = new Login();
        login.setId(id);
        login.setFirstname(dto.firstName());
        login.setLastname(dto.lastName());
        login.setEmail(dto.email());
        login.setUsername(dto.username());
        login.setPassword(password);
        login.setActive(true);
        login.setRefereeCoach(false);
        login.setRefereeCoachPlus(false);
        login.setReferee(false);
        login.setTrainerCoach(false);
        login.setTrainer(false);
        login.setAdmin(false);
        loginDao.insert(login);

        log.info("created local user {} {} ({})", dto.firstName(), dto.lastName(), id);
        return toDTO(login);
    }

    public UserDTO updateRoles(Long id, UpdateUserRolesDTO dto) {
        var login = loginDao.fetchOptionalById(id).orElseThrow(() -> new IllegalArgumentException("User not found: " + id));

        login.setRefereeCoach(dto.refereeCoach());
        login.setRefereeCoachPlus(dto.refereeCoachPlus());
        login.setReferee(dto.referee());
        login.setTrainerCoach(dto.trainerCoach());
        login.setTrainer(dto.trainer());
        loginDao.update(login);

        return toDTO(login);
    }
}
