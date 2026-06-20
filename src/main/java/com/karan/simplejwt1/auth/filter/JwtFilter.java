package com.karan.simplejwt1.auth.filter;

import com.karan.simplejwt1.auth.jwt.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@AllArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    /**
     * Same contract as for {@code doFilter}, but guaranteed to be
     * just invoked once per request within a single request thread.
     * See {@link #shouldNotFilterAsyncDispatch()} for details.
     * <p>Provides HttpServletRequest and HttpServletResponse arguments instead of the
     * default ServletRequest and ServletResponse ones.
     *
     * @param request
     * @param response
     * @param filterChain
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            // 1. Validate and extract ALL claims ONCE (1 CPU cycle)
            if (jwtService.isTokenValid(token)) {

                // 2. Read from the in-memory claims object
                String tokenType = jwtService.getTokenType(token);
                if (!"ACCESS".equals(tokenType)) {
                    log.warn("Blocked API request: Client attempted to use a {} token.", tokenType);
                    sendUnauthorized(response);
                    return;
                }

                String username = jwtService.extractUserName(token);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                    // Note: SuppressWarnings or manual mapping as we discussed earlier
                    List<String> rawRoles = jwtService.extractRoles(token);
                    List<SimpleGrantedAuthority> authorities = rawRoles.stream()
                            .map(Object::toString)
                            .map(SimpleGrantedAuthority::new)
                            .toList();

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    username,
                                    null,
                                    authorities
                            );

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
            filterChain.doFilter(request, response);

        } catch (JwtException e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            sendUnauthorized(response);
        }
    }

    private void sendUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("""
            {
              "error": "Unauthorized",
              "message": "Invalid or expired token"
            }
        """);
    }
}
