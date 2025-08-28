package ch.refereecoach.probasket.service.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.springframework.security.oauth2.jose.jws.MacAlgorithm.HS256;

@Component
@RequiredArgsConstructor
public class JwtService {

    public static final String CLAIM_AUTHORITIES = "authorities";

    private final JwtEncoder encoder;

    public String generateJwtToken(Authentication authentication) {
        var userId = (Long) authentication.getPrincipal();

        var now = Instant.now();
        var claims = JwtClaimsSet.builder()
                                 .issuer("self")
                                 .issuedAt(now)
                                 .expiresAt(now.plus(365, DAYS))
                                 .subject(String.valueOf(userId))
                                 .claim(CLAIM_AUTHORITIES, authentication.getAuthorities().stream()
                                                                         .map(GrantedAuthority::getAuthority)
                                                                         .toList())
                                 .build();

        var encoderParameters = JwtEncoderParameters.from(JwsHeader.with(HS256).build(), claims);
        return this.encoder.encode(encoderParameters).getTokenValue();
    }

}
