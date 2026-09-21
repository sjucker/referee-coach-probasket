package ch.refereecoach.probasket.service.basketplan;

import ch.refereecoach.probasket.configuration.ApplicationProperties;
import ch.refereecoach.probasket.jooq.tables.daos.LoginDao;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import static ch.refereecoach.probasket.common.AuthProvider.LOCAL;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class BasketplanUserSyncServiceTest {

    private final WebClient.Builder webClientBuilder = mock(WebClient.Builder.class);
    private final LoginDao loginDao = mock(LoginDao.class);

    @Test
    void doesNothingWhenSyncIsDisabled() {
        var properties = new ApplicationProperties();
        properties.setAuthProvider(LOCAL);
        properties.setBasketplanUserSyncEnabled(false);
        properties.setBasketplanGameLookupEnabled(false);

        new BasketplanUserSyncService(properties, webClientBuilder, loginDao).syncReferees();

        verifyNoInteractions(webClientBuilder, loginDao);
    }
}
