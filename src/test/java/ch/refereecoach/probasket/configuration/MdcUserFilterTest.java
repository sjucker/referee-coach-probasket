package ch.refereecoach.probasket.configuration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static ch.refereecoach.probasket.configuration.MdcUserFilter.MDC_USER_ID;
import static org.assertj.core.api.Assertions.assertThat;

class MdcUserFilterTest {

    private final MdcUserFilter filter = new MdcUserFilter();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        MDC.clear();
    }

    @Test
    void userIdInMdcDuringRequestAndRemovedAfterwards() throws ServletException, IOException {
        var jwt = Jwt.withTokenValue("token").header("alg", "HS256").subject("4711").build();
        var authorities = List.of(new SimpleGrantedAuthority("REFEREE"));

        assertThat(userIdSeenInChain(new JwtAuthenticationToken(jwt, authorities))).isEqualTo("4711");
        assertThat(MDC.get(MDC_USER_ID)).isNull();
    }

    @Test
    void noUserIdWithoutAuthentication() throws ServletException, IOException {
        assertThat(userIdSeenInChain(null)).isNull();
    }

    @Test
    void noUserIdForAnonymous() throws ServletException, IOException {
        var anonymous = new AnonymousAuthenticationToken("key", "anonymousUser", List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));

        assertThat(userIdSeenInChain(anonymous)).isNull();
    }

    private String userIdSeenInChain(Authentication authentication) throws ServletException, IOException {
        SecurityContextHolder.getContext().setAuthentication(authentication);

        var seen = new AtomicReference<String>();
        FilterChain chain = (_, _) -> seen.set(MDC.get(MDC_USER_ID));
        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), chain);

        return seen.get();
    }

}
