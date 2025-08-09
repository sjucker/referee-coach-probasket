package ch.refereecoach.probasket.configuration;

import ch.refereecoach.probasket.security.BasketplanAuthenticationProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.spec.SecretKeySpec;

import static ch.refereecoach.probasket.security.JwtTokenService.SIGNATURE_ALGORITHM;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final ApplicationProperties properties;

    @Bean
    public AuthenticationManager authManager() {
        return new ProviderManager(new BasketplanAuthenticationProvider());
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        var secretKey = new SecretKeySpec(properties.getJwtSecret().getBytes(UTF_8), SIGNATURE_ALGORITHM.getJcaName());
        return NimbusJwtDecoder.withSecretKey(secretKey).build();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http.httpBasic(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll() // to serve the Angular frontend
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwtSpec -> jwtSpec.decoder(jwtDecoder())))
                .build();
    }

}
