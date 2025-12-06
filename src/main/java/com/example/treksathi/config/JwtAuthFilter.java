package com.example.treksathi.config;

import com.example.treksathi.exception.TokenExpireException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.implementation.bytecode.Throw;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final UserDetailsService userDetailsService;
    private final JWTService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, TokenExpireException, IOException {
        log.info("incoming request: {}", request.getRequestURI());

        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = authHeader.substring(7).trim();

        try{
        String email = jwtService.getUsernameFormToken(token);
        if (email != null && SecurityContextHolder.getContext().getAuthentication()== null){
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(email);

            if (jwtService.isTokenValid(token, userDetails)){
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

        }
        }
        catch (TokenExpireException e){
            new TokenExpireException("Requested token is Expired");
            log.warn("Invalid JWT: {}", e.getMessage());
        }
        filterChain.doFilter(request, response);

    }

//@Override
//    protected boolean shouldNotFilter(HttpServletRequest request) {
//        String path = request.getRequestURI();
//
//        // Add all public paths here
//        return path.startsWith("/api/auth/") ||
//                path.startsWith("/oauth2/") ||
//                path.startsWith("/login/oauth2/") ||
//                path.startsWith("/login") ||
//                path.equals("/error");
//    }
}
