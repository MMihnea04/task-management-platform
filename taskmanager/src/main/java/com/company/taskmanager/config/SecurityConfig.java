package com.company.taskmanager.config;

import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// Configuration, aceasta clasa contine configurari Spring (metode cu
// Bean, ce vor fi rulate la pornirea aplicatiei
// EnableWebSecurity, activeaza configurarea manuala a Spring Security
// EnableMethodSecurity, permite folosirea @PreAuthorize pe metode individuale
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    // Bean, spune lui Spring sa gestioneze acest obiect
    // SecurityFilterChain,lantul de filtre de securitate, inima configurarii
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // dezactivam CSRF pt ca nu avem nevoie de el pentru API REST cu JWT
                // CSRF e pt formulare HTML cu sesiuni
                .csrf(csrf -> csrf.disable())

                // dam permitAll la paginile de tip /api/auth/login su register
                // si rulam o bariera totala pt restul ce necesita autentificare
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers("/api/auth/**", "/error").permitAll()
                        .anyRequest().authenticated()
                )

                // STATELESS, nu folosim sesiuni HTTP
                // fiecare request trebuie sa contina token-ul JWT
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // setam provider-ul de autentificare (cum verificam userul si parola)
                .authenticationProvider(authenticationProvider())

                // adaugam filtrul nostru JWT inainte de filtrul Spring Security
                // a.i. tokenul e verificat la fiecare request
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // BCryptPasswordEncoder este algoritmul de encryptare a parolelor
    // BCrypt adauga automat "salt" pt a preveni atacurile
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // AuthenticationProvider e componenta care stie sa verifice username + parola
    // DaoAuthenticationProvider verifica credentialele folosind UserDetailsService si PasswordEncoder
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService); // de unde ia userul
        provider.setPasswordEncoder(passwordEncoder());     // cum verifica parola
        return provider;
    }

    // AuthenticationManager primeste login request si deleaga catre AuthenticationProvider
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config
    ) throws Exception {
        return config.getAuthenticationManager();
    }
}