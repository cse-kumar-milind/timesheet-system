package com.company.authservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.company.authservice.security.CustomUserDetailsService;
import com.company.authservice.security.JwtAuthFilter;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
	
	private final JwtAuthFilter jwtAuthFilter;
	private final CustomUserDetailsService userDetailsService;
	
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.csrf(AbstractHttpConfigurer::disable)
			.authorizeHttpRequests(auth -> auth
					.requestMatchers(
							"/auth/signup",
							"/auth/login",
							"/actuator/**"
							).permitAll()
					.anyRequest().authenticated()
					)
			.sessionManagement(session -> session
					.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
					)
			.exceptionHandling(ex -> ex
		            .authenticationEntryPoint(
		                (request, response, authException) -> {
		                    // No token / invalid token → 401
		                    response.setStatus(
		                        HttpServletResponse.SC_UNAUTHORIZED);
		                    response.setContentType("application/json");
		                    response.getWriter().write(
		                        "{\"error\": \"Unauthorized\"," +
		                        " \"message\": \"Token is missing or invalid\"}");
		                })
		            .accessDeniedHandler(
		                (request, response, accessDeniedException) -> {
		                    // Valid token but wrong role → 403
		                    response.setStatus(
		                        HttpServletResponse.SC_FORBIDDEN);
		                    response.setContentType("application/json");
		                    response.getWriter().write(
		                        "{\"error\": \"Forbidden\"," +
		                        " \"message\": \"You don't have permission\"}");
		                })
		        )
			.authenticationProvider(authenticationProvider())
			.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
		
		return http.build();
	}
	
	@Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider =
            new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

	@Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
	
}
