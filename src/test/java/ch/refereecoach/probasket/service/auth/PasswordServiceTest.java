package ch.refereecoach.probasket.service.auth;

import ch.refereecoach.probasket.configuration.ApplicationProperties;
import ch.refereecoach.probasket.dto.auth.ChangePasswordDTO;
import ch.refereecoach.probasket.jooq.tables.daos.LoginDao;
import ch.refereecoach.probasket.jooq.tables.pojos.Login;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static ch.refereecoach.probasket.common.AuthProvider.BASKETPLAN;
import static ch.refereecoach.probasket.common.AuthProvider.LOCAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PasswordServiceTest {

    private static final String CURRENT_PASSWORD = "current-password";
    private static final String NEW_PASSWORD = "new-password";

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final LoginDao loginDao = mock(LoginDao.class);

    @Test
    void changesPasswordWhenCurrentOneMatches() {
        var service = service(LOCAL);
        var login = login(passwordEncoder.encode(CURRENT_PASSWORD));
        when(loginDao.fetchOptionalById(42L)).thenReturn(Optional.of(login));

        service.changeOwnPassword(42L, new ChangePasswordDTO(CURRENT_PASSWORD, NEW_PASSWORD));

        assertThat(passwordEncoder.matches(NEW_PASSWORD, login.getPassword())).isTrue();
        verify(loginDao).update(login);
    }

    @Test
    void rejectsChangeWhenCurrentPasswordDoesNotMatch() {
        var service = service(LOCAL);
        when(loginDao.fetchOptionalById(42L)).thenReturn(Optional.of(login(passwordEncoder.encode(CURRENT_PASSWORD))));

        assertThatThrownBy(() -> service.changeOwnPassword(42L, new ChangePasswordDTO("wrong", NEW_PASSWORD)))
                .isInstanceOf(IllegalArgumentException.class);
        verify(loginDao, never()).update(any(Login.class));
    }

    @Test
    void rejectsChangeWhenNoPasswordIsSetYet() {
        var service = service(LOCAL);
        when(loginDao.fetchOptionalById(42L)).thenReturn(Optional.of(login(null)));

        assertThatThrownBy(() -> service.changeOwnPassword(42L, new ChangePasswordDTO(CURRENT_PASSWORD, NEW_PASSWORD)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsChangeForUnknownUser() {
        var service = service(LOCAL);
        when(loginDao.fetchOptionalById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.changeOwnPassword(42L, new ChangePasswordDTO(CURRENT_PASSWORD, NEW_PASSWORD)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void setsPasswordWithoutKnowingTheCurrentOne() {
        var service = service(LOCAL);
        var login = login(null);
        when(loginDao.fetchOptionalById(42L)).thenReturn(Optional.of(login));

        service.setPassword(42L, NEW_PASSWORD);

        assertThat(passwordEncoder.matches(NEW_PASSWORD, login.getPassword())).isTrue();
        verify(loginDao).update(login);
    }

    @Test
    void refusesAnyPasswordManagementWithBasketplanAuthentication() {
        var service = service(BASKETPLAN);

        assertThatThrownBy(() -> service.encode(NEW_PASSWORD)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> service.setPassword(42L, NEW_PASSWORD)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> service.changeOwnPassword(42L, new ChangePasswordDTO(CURRENT_PASSWORD, NEW_PASSWORD)))
                .isInstanceOf(IllegalStateException.class);
    }

    private PasswordService service(ch.refereecoach.probasket.common.AuthProvider authProvider) {
        var properties = new ApplicationProperties();
        properties.setAuthProvider(authProvider);
        properties.setBasketplanUserSyncEnabled(false);
        properties.setBasketplanGameLookupEnabled(false);
        return new PasswordService(properties, loginDao, passwordEncoder);
    }

    private static Login login(String password) {
        var login = new Login();
        login.setId(42L);
        login.setPassword(password);
        login.setActive(true);
        return login;
    }
}
