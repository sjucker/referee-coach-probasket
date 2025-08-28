package ch.refereecoach.probasket.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

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
    private String jwtSecret;
    private String basketplanApiKey;
    private Integer federationId;
}
