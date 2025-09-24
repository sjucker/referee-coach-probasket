package ch.refereecoach.probasket.configuration;

import ch.refereecoach.probasket.service.auth.BasketplanAuthenticationProvider;
import ch.refereecoach.probasket.service.report.UserService;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.spec.SecretKeySpec;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static ch.refereecoach.probasket.common.UserRole.ADMIN;
import static ch.refereecoach.probasket.common.UserRole.REFEREE;
import static ch.refereecoach.probasket.common.UserRole.REFEREE_COACH;
import static ch.refereecoach.probasket.common.UserRole.REFEREE_COACH_PLUS;
import static ch.refereecoach.probasket.common.UserRole.TRAINER;
import static ch.refereecoach.probasket.common.UserRole.TRAINER_COACH;
import static ch.refereecoach.probasket.service.auth.JwtService.CLAIM_AUTHORITIES;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final ApplicationProperties properties;
    private final UserService userService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authManager(BasketplanAuthenticationProvider provider) {
        return new ProviderManager(provider);
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withSecretKey(new SecretKeySpec(properties.getJwtSecret().getBytes(UTF_8), "HmacSHA256")).build();
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        return new NimbusJwtEncoder(new ImmutableSecret<>(properties.getJwtSecret().getBytes()));
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http.httpBasic(AbstractHttpConfigurer::disable)
                   .csrf(AbstractHttpConfigurer::disable)
                   .exceptionHandling(withDefaults())
                   .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                   .authorizeHttpRequests(auth -> auth.requestMatchers("/actuator/health/**", "/actuator/info").permitAll()
                                                      .requestMatchers("/api/auth/**").permitAll()
                                                      .requestMatchers("/api/**").authenticated()
                                                      .anyRequest().permitAll() // to serve the Angular frontend
                                         )
                   .oauth2ResourceServer(oauth2 -> oauth2.jwt(
                           jwtSpec -> {
                               jwtSpec.decoder(jwtDecoder());
                               jwtSpec.jwtAuthenticationConverter(jwt -> {
                                   var currentAuthorities = getCurrentAuthorities(jwt);
                                   var currentAuthoritiesValues = new HashSet<>(currentAuthorities.stream().map(SimpleGrantedAuthority::getAuthority).toList());

                                   if (jwt.getClaimAsStringList(CLAIM_AUTHORITIES).stream().anyMatch(tokenAuthority -> !currentAuthoritiesValues.contains(tokenAuthority))) {
                                       throw new BadCredentialsException("JWT contains authority no longer granted");
                                   }

                                   return new JwtAuthenticationToken(jwt, currentAuthorities);
                               });
                           }))
                   .build();
    }

    private List<SimpleGrantedAuthority> getCurrentAuthorities(Jwt jwt) {
        var login = userService.findById(Long.valueOf(jwt.getSubject()))
                               .orElseThrow(() -> new UsernameNotFoundException("User not found: " + jwt.getSubject()));

        var currentAuthorities = new ArrayList<SimpleGrantedAuthority>();
        if (login.refereeCoach()) currentAuthorities.add(new SimpleGrantedAuthority(REFEREE_COACH.name()));
        if (login.refereeCoachPlus()) currentAuthorities.add(new SimpleGrantedAuthority(REFEREE_COACH_PLUS.name()));
        if (login.referee()) currentAuthorities.add(new SimpleGrantedAuthority(REFEREE.name()));
        if (login.trainerCoach()) currentAuthorities.add(new SimpleGrantedAuthority(TRAINER_COACH.name()));
        if (login.trainer()) currentAuthorities.add(new SimpleGrantedAuthority(TRAINER.name()));
        if (login.admin()) currentAuthorities.add(new SimpleGrantedAuthority(ADMIN.name()));
        return currentAuthorities;
    }

}
