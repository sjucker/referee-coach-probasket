package ch.refereecoach.probasket.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ping")
public class SecuredEndpoint {

    @GetMapping
    @Secured({"REFEREE_COACH", "ADMIN"})
    public ResponseEntity<String> ping(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok("pong: " + jwt.getSubject());
    }

    @GetMapping("/admin")
    @Secured({"ADMIN"})
    public ResponseEntity<String> pingAdmin(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok("admin pong: " + jwt.getSubject());
    }
}
