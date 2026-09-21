package ch.refereecoach.probasket.service.auth;

import ch.refereecoach.probasket.configuration.ApplicationProperties;
import ch.refereecoach.probasket.jooq.tables.daos.LoginDao;
import ch.refereecoach.probasket.jooq.tables.pojos.Login;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static ch.refereecoach.probasket.common.AuthProvider.LOCAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LocalPasswordAuthenticationProviderTest {

    private static final String USERNAME = "jane.doe";
    private static final String PASSWORD = "very-secret";
    private static final String IMPERSONATION_PASSWORD = "impersonate-me";

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final LoginDao loginDao = mock(LoginDao.class);

    private LocalPasswordAuthenticationProvider provider;

    @BeforeEach
    void setUp() {
        var properties = new ApplicationProperties();
        properties.setAuthProvider(LOCAL);
        properties.setBasketplanUserSyncEnabled(false);
        properties.setBasketplanGameLookupEnabled(false);
        properties.setImpersonationPassword(passwordEncoder.encode(IMPERSONATION_PASSWORD));

        provider = new LocalPasswordAuthenticationProvider(properties, loginDao, passwordEncoder);
    }

    @Test
    void authenticatesUserWithMatchingPassword() {
        var login = login(42L, passwordEncoder.encode(PASSWORD), true);
        login.setReferee(true);
        login.setAdmin(true);
        when(loginDao.fetchByUsername(USERNAME)).thenReturn(List.of(login));

        var authentication = provider.authenticate(new UsernamePasswordAuthenticationToken(USERNAME, PASSWORD));

        assertThat(authentication.getPrincipal()).isEqualTo(42L);
        assertThat(authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority))
                .containsExactlyInAnyOrder("REFEREE", "ADMIN");
        assertThat(login.getLastLogin()).isNotNull();
        verify(loginDao).update(login);
    }

    @Test
    void rejectsWrongPassword() {
        when(loginDao.fetchByUsername(USERNAME)).thenReturn(List.of(login(42L, passwordEncoder.encode(PASSWORD), true)));

        assertThatThrownBy(() -> provider.authenticate(new UsernamePasswordAuthenticationToken(USERNAME, "wrong")))
                .isInstanceOf(BadCredentialsException.class);
        verify(loginDao, never()).update(any(Login.class));
    }

    @Test
    void rejectsUnknownUsername() {
        when(loginDao.fetchByUsername(USERNAME)).thenReturn(List.of());

        assertThatThrownBy(() -> provider.authenticate(new UsernamePasswordAuthenticationToken(USERNAME, PASSWORD)))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void rejectsAmbiguousUsername() {
        when(loginDao.fetchByUsername(USERNAME)).thenReturn(List.of(login(1L, passwordEncoder.encode(PASSWORD), true),
                                                                   login(2L, passwordEncoder.encode(PASSWORD), true)));

        assertThatThrownBy(() -> provider.authenticate(new UsernamePasswordAuthenticationToken(USERNAME, PASSWORD)))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void rejectsUserWithoutPassword() {
        when(loginDao.fetchByUsername(USERNAME)).thenReturn(List.of(login(42L, null, true)));

        assertThatThrownBy(() -> provider.authenticate(new UsernamePasswordAuthenticationToken(USERNAME, PASSWORD)))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void rejectsInactiveUser() {
        when(loginDao.fetchByUsername(USERNAME)).thenReturn(List.of(login(42L, passwordEncoder.encode(PASSWORD), false)));

        assertThatThrownBy(() -> provider.authenticate(new UsernamePasswordAuthenticationToken(USERNAME, PASSWORD)))
                .isInstanceOf(AccountExpiredException.class);
    }

    @Test
    void impersonationResolvesTheUsernameAsUserIdAndDoesNotTouchTheUser() {
        var login = login(42L, passwordEncoder.encode(PASSWORD), true);
        when(loginDao.fetchOptionalById(42L)).thenReturn(Optional.of(login));

        var authentication = provider.authenticate(new UsernamePasswordAuthenticationToken("42", IMPERSONATION_PASSWORD));

        assertThat(authentication.getPrincipal()).isEqualTo(42L);
        assertThat(login.getLastLogin()).isNull();
        verify(loginDao, never()).update(any(Login.class));
    }

    @Test
    void impersonationOfUnknownUserIsRejected() {
        when(loginDao.fetchOptionalById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> provider.authenticate(new UsernamePasswordAuthenticationToken("42", IMPERSONATION_PASSWORD)))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void supportsUsernamePasswordAuthenticationToken() {
        assertThat(provider.supports(UsernamePasswordAuthenticationToken.class)).isTrue();
    }

    private static Login login(Long id, String password, boolean active) {
        var login = new Login();
        login.setId(id);
        login.setUsername(USERNAME);
        login.setPassword(password);
        login.setActive(active);
        login.setRefereeCoach(false);
        login.setRefereeCoachPlus(false);
        login.setReferee(false);
        login.setTrainerCoach(false);
        login.setTrainer(false);
        login.setAdmin(false);
        return login;
    }
}
