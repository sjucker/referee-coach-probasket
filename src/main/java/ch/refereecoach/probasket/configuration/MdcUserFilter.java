package ch.refereecoach.probasket.configuration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Puts the id of the logged-in user into the logging MDC for the duration of the request, so that it appears in
 * every log line (see {@code logging.pattern.correlation} and {@code logback-spring.xml}).
 * <p>
 * Must run after the authentication filters of the security filter chain, which is why it is registered there
 * rather than as a servlet filter bean.
 */
public class MdcUserFilter extends OncePerRequestFilter {

    public static final String MDC_USER_ID = "userId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var loggedIn = authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);

        if (!loggedIn) {
            filterChain.doFilter(request, response);
            return;
        }

        // for a JwtAuthenticationToken the name is the subject, i.e. the user id
        MDC.put(MDC_USER_ID, authentication.getName());
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_USER_ID);
        }
    }

}
