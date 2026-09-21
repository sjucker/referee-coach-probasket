package ch.refereecoach.probasket.configuration;

import org.junit.jupiter.api.Test;

import static ch.refereecoach.probasket.common.AuthProvider.BASKETPLAN;
import static ch.refereecoach.probasket.common.AuthProvider.LOCAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApplicationPropertiesTest {

    @Test
    void defaultsKeepBasketplanEnabled() {
        var properties = new ApplicationProperties();

        assertThat(properties.getAuthProvider()).isEqualTo(BASKETPLAN);
        assertThat(properties.isBasketplanUserSyncEnabled()).isTrue();
        assertThat(properties.isBasketplanGameLookupEnabled()).isTrue();
        assertThat(properties.isBasketplanApiKeyRequired()).isTrue();
    }

    @Test
    void apiKeyIsRequiredWhenBasketplanAuthenticationIsEnabled() {
        var properties = fullyLocal();
        properties.setAuthProvider(BASKETPLAN);

        assertThat(properties.isBasketplanApiKeyRequired()).isTrue();
        assertThatThrownBy(properties::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("basketplan-api-key");
    }

    @Test
    void apiKeyIsRequiredWhenUserSyncIsEnabled() {
        var properties = fullyLocal();
        properties.setBasketplanUserSyncEnabled(true);

        assertThat(properties.isBasketplanApiKeyRequired()).isTrue();
        assertThatThrownBy(properties::validate).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void apiKeyIsNotRequiredForGameLookup() {
        // the game lookup endpoint is public and does not send the refApiKey header
        var properties = fullyLocal();
        properties.setBasketplanGameLookupEnabled(true);

        assertThat(properties.isBasketplanApiKeyRequired()).isFalse();
        assertThatCode(properties::validate).doesNotThrowAnyException();
    }

    @Test
    void apiKeyIsNotRequiredWhenNoBasketplanFeatureIsEnabled() {
        var properties = fullyLocal();

        assertThat(properties.isBasketplanApiKeyRequired()).isFalse();
        assertThatCode(properties::validate).doesNotThrowAnyException();
    }

    @Test
    void blankApiKeyIsRejectedJustLikeAMissingOne() {
        var properties = fullyLocal();
        properties.setBasketplanUserSyncEnabled(true);
        properties.setBasketplanApiKey("   ");

        assertThatThrownBy(properties::validate).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void configuredApiKeySatisfiesValidation() {
        var properties = new ApplicationProperties();
        properties.setBasketplanApiKey("secret");

        assertThatCode(properties::validate).doesNotThrowAnyException();
    }

    private static ApplicationProperties fullyLocal() {
        var properties = new ApplicationProperties();
        properties.setAuthProvider(LOCAL);
        properties.setBasketplanUserSyncEnabled(false);
        properties.setBasketplanGameLookupEnabled(false);
        return properties;
    }
}
