package ch.refereecoach.probasket.security;

import ch.refereecoach.probasket.configuration.ApplicationProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;

import static io.jsonwebtoken.SignatureAlgorithm.HS256;
import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
@Component
public class JwtTokenService {

    public static final SignatureAlgorithm SIGNATURE_ALGORITHM = HS256;

    private final SecretKeySpec secretKey;

    public JwtTokenService(ApplicationProperties properties) {
        log.info(properties.toString());
        this.secretKey = new SecretKeySpec(properties.getJwtSecret().getBytes(UTF_8), SIGNATURE_ALGORITHM.getJcaName());
    }

    public String generateJwtToken(Authentication authentication) {

        var userPrincipal = (String) authentication.getPrincipal();

        // TODO improve this!
        return Jwts.builder()
                   .setSubject(userPrincipal)
                   .signWith(secretKey, HS256) // Sign with HMAC SHA256 using the secret key
                   .compact();
    }

}
