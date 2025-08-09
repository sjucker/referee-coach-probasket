package ch.refereecoach.probasket.configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@ToString
@Configuration
@ConfigurationProperties(prefix = "probasket")
public class ApplicationProperties {
    private boolean overrideRecipient;
    private String jwtSecret;
}
