package com.company.authservice.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class JwtAuthFilter extends OncePerRequestFilter {
	
	private final JwtService jwtService;
	private final CustomUserDetailsService userDetailsService;
	
	 @Override
	 protected void doFilterInternal(HttpServletRequest request,
	                                    HttpServletResponse response,
	                                    FilterChain filterChain)
	            throws ServletException, IOException {
		 	
//		 String path = request.getRequestURI();
//		 
//		 if (path.startsWith("/auth/signup")
//			        || path.startsWith("/auth/login")
//			        || path.startsWith("/auth/forgot-password")
//			        || path.startsWith("/auth/users")
//			        || path.startsWith("/actuator")
//			        // ✅ ADD THESE
//			        || path.contains("/auth/v3/api-docs")
//			        || path.contains("/auth/swagger-ui")
//			        || path.contains("/auth/swagger-resources")
//			        || path.contains("/auth/webjars")) {
//			    filterChain.doFilter(request, response);
//			    return;
//			}



	        // 1. Read Authorization header
	        final String authHeader = request.getHeader("Authorization");

	        // 2. No header or wrong format → skip
	        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
	            filterChain.doFilter(request, response);
	            return;
	        }

	        // 3. Extract token (remove "Bearer " prefix)
	        final String jwt = authHeader.substring(7);

	        // 4. Extract email from token
	        final String userEmail;
	        try {
	            userEmail = jwtService.extractUsername(jwt);
	        } catch (Exception e) {
	            // Malformed token → skip
	            filterChain.doFilter(request, response);
	            return;
	        }

	        // 5. Only process if not already authenticated
	        if (userEmail != null &&
	            SecurityContextHolder.getContext()
	                                 .getAuthentication() == null) {

	            // 6. Load user from DB
	            UserDetails userDetails =
	                userDetailsService.loadUserByUsername(userEmail);

	            // 7. Validate token
	            if (jwtService.isTokenValid(jwt, userDetails)) {

	                // 8. Create authentication object
	                UsernamePasswordAuthenticationToken authToken =
	                    new UsernamePasswordAuthenticationToken(
	                        userDetails,
	                        null,
	                        userDetails.getAuthorities()
	                    );

	                authToken.setDetails(
	                    new WebAuthenticationDetailsSource()
	                        .buildDetails(request)
	                );

	                // 9. Set in SecurityContext → request is authenticated
	                SecurityContextHolder.getContext()
	                                     .setAuthentication(authToken);
	            }
	        }

	        // 10. Continue filter chain
	        filterChain.doFilter(request, response);
	    }
}
