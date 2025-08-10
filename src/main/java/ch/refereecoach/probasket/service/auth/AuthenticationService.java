package ch.refereecoach.probasket.service.auth;

import ch.refereecoach.probasket.common.UserRole;
import ch.refereecoach.probasket.dto.auth.LoginResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public LoginResponseDTO authenticate(String username, String password) throws AuthenticationException {
        var authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        return new LoginResponseDTO(jwtService.generateJwtToken(authentication), username, roles(authentication));
    }

    private static List<UserRole> roles(Authentication authentication) {
        return authentication.getAuthorities().stream()
                             .map(grantedAuthority -> UserRole.valueOf(grantedAuthority.getAuthority()))
                             .toList();
    }

}
