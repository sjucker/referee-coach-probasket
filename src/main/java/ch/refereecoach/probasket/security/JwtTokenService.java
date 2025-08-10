package ch.refereecoach.probasket.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
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
public class JwtTokenService {

    private final JwtEncoder encoder;

    public String generateJwtToken(Authentication authentication) {
        var userPrincipal = (String) authentication.getPrincipal();

        var now = Instant.now();
        var claims = JwtClaimsSet.builder()
                                 .issuer("self")
                                 .issuedAt(now)
                                 .expiresAt(now.plus(365, DAYS))
                                 .subject(userPrincipal)
                                 .build();

        var encoderParameters = JwtEncoderParameters.from(JwsHeader.with(HS256).build(), claims);
        return this.encoder.encode(encoderParameters).getTokenValue();
    }

}
