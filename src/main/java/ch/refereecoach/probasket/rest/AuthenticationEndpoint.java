package ch.refereecoach.probasket.rest;

import ch.refereecoach.probasket.configuration.ApplicationProperties;
import ch.refereecoach.probasket.dto.auth.AuthConfigDTO;
import ch.refereecoach.probasket.dto.auth.ChangePasswordDTO;
import ch.refereecoach.probasket.dto.auth.LoginRequestDTO;
import ch.refereecoach.probasket.dto.auth.LoginResponseDTO;
import ch.refereecoach.probasket.service.auth.AuthenticationService;
import ch.refereecoach.probasket.service.auth.PasswordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationEndpoint {

    private final ApplicationProperties applicationProperties;
    private final AuthenticationService authenticationService;
    private final PasswordService passwordService;

    @GetMapping("/config")
    public ResponseEntity<AuthConfigDTO> config() {
        return ResponseEntity.ok(new AuthConfigDTO(applicationProperties.getAuthProvider()));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> authenticateUser(@Valid @RequestBody LoginRequestDTO loginRequest) {
        log.info("POST /api/auth/login {}", loginRequest.username());
        return ResponseEntity.ok(authenticationService.authenticate(loginRequest.username(), loginRequest.password()));
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal Jwt jwt,
                                               @Valid @RequestBody ChangePasswordDTO dto) {
        log.info("POST /api/auth/change-password {}", jwt.getSubject());
        try {
            passwordService.changeOwnPassword(Long.valueOf(jwt.getSubject()), dto);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            log.warn("password change rejected: {}", e.getMessage());
            return ResponseEntity.status(409).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

}
