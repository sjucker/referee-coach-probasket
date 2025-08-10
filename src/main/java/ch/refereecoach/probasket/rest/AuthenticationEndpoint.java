package ch.refereecoach.probasket.rest;

import ch.refereecoach.probasket.dto.auth.LoginRequestDTO;
import ch.refereecoach.probasket.dto.auth.LoginResponseDTO;
import ch.refereecoach.probasket.security.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationEndpoint {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> authenticateUser(@Valid @RequestBody LoginRequestDTO loginRequest) {
        log.info("POST /api/auth/login {}", loginRequest.username());
        return ResponseEntity.ok(authenticationService.authenticate(loginRequest.username(), loginRequest.password()));
    }

}
