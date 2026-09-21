package ch.refereecoach.probasket.service.basketplan;

import ch.refereecoach.probasket.configuration.ApplicationProperties;
import ch.refereecoach.probasket.service.report.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import static ch.refereecoach.probasket.common.AuthProvider.LOCAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class BasketplanGameServiceTest {

    private final UserService userService = mock(UserService.class);
    private final WebClient.Builder webClientBuilder = mock(WebClient.Builder.class);

    @Test
    void returnsEmptyWhenGameLookupIsDisabled() {
        var properties = new ApplicationProperties();
        properties.setAuthProvider(LOCAL);
        properties.setBasketplanUserSyncEnabled(false);
        properties.setBasketplanGameLookupEnabled(false);

        var game = new BasketplanGameService(properties, userService, webClientBuilder).findGameByNumber("12345");

        assertThat(game).isEmpty();
        verifyNoInteractions(userService, webClientBuilder);
    }
}
