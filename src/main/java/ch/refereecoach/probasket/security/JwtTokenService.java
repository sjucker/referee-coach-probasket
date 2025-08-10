package ch.refereecoach.probasket.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

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
                                 // TODO how long?
                                 .expiresAt(now.plus(1, ChronoUnit.HOURS))
                                 .subject(userPrincipal)
                                 .build();

        var encoderParameters = JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims);
        return this.encoder.encode(encoderParameters).getTokenValue();
    }

}
