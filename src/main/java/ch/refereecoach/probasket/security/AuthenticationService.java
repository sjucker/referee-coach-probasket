package ch.refereecoach.probasket.security;

import ch.refereecoach.probasket.dto.auth.LoginResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;

    public LoginResponseDTO authenticate(String username, String password) throws AuthenticationException {
        var authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        return new LoginResponseDTO(jwtTokenService.generateJwtToken(authentication));
    }

}
