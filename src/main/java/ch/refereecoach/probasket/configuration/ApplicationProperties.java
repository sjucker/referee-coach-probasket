package ch.refereecoach.probasket.configuration;

import ch.refereecoach.probasket.common.AuthProvider;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import static ch.refereecoach.probasket.common.AuthProvider.BASKETPLAN;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "probasket")
public class ApplicationProperties {
    private String baseUrl;
    private boolean overrideRecipient;
    private String overrideRecipientMail;
    private String bccMail;
    private String ccMail;
    private String copyMail;
    private String jwtSecret;
    private String basketplanApiKey;
    private Integer federationId;
    private String impersonationPassword;

    /**
     * which authentication provider backs /api/auth/login
     */
    private AuthProvider authProvider = BASKETPLAN;
    /**
     * whether referees are periodically synchronized from basketplan
     */
    private boolean basketplanUserSyncEnabled = true;
    /**
     * whether games may be looked up in basketplan
     */
    private boolean basketplanGameLookupEnabled = true;

    public boolean isBasketplanAuthentication() {
        return authProvider == BASKETPLAN;
    }

    /**
     * every basketplan call needs the api key, so it is mandatory as soon as any basketplan feature is enabled.
     */
    public boolean isBasketplanApiKeyRequired() {
        return isBasketplanAuthentication() || basketplanUserSyncEnabled || basketplanGameLookupEnabled;
    }

    @PostConstruct
    public void validate() {
        if (isBasketplanApiKeyRequired() && isBlank(basketplanApiKey)) {
            throw new IllegalStateException(("probasket.basketplan-api-key (BASKETPLAN_API_KEY) must be set: "
                                             + "auth-provider=%s, basketplan-user-sync-enabled=%s, basketplan-game-lookup-enabled=%s")
                                                    .formatted(authProvider, basketplanUserSyncEnabled, basketplanGameLookupEnabled));
        }

        log.info("authentication provider: {}, basketplan user-sync enabled: {}, basketplan game-lookup enabled: {}",
                 authProvider, basketplanUserSyncEnabled, basketplanGameLookupEnabled);
    }
}
