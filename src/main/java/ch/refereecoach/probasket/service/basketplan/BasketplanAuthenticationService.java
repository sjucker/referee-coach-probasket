package ch.refereecoach.probasket.service.basketplan;

import org.springframework.stereotype.Service;

import static org.apache.commons.lang3.StringUtils.toRootLowerCase;

@Service
public class BasketplanAuthenticationService {

    public boolean authenticate(String username, String password) {
        // TODO call basketplan-service to authenticate

        return switch (toRootLowerCase(username)) {
            case "caspar.schaudt", "stefan.jucker", "nicolas.castro", "jovan.ljubanic" -> password.equals("pass");
            default -> false;
        };
    }

}
