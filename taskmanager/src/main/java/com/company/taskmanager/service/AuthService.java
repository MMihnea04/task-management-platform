package com.company.taskmanager.service;

import lombok.extern.slf4j.Slf4j;
import com.company.taskmanager.dto.AuthResponse;
import com.company.taskmanager.dto.LoginRequest;
import com.company.taskmanager.dto.RegisterRequest;
import com.company.taskmanager.entity.Role;
import com.company.taskmanager.entity.User;
import com.company.taskmanager.repository.RoleRepository;
import com.company.taskmanager.repository.UserRepository;
import com.company.taskmanager.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
// RequiredArgsConstructor genereaza constructorul pt campurile final (dependency injection)
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        // validam daca username-ul sau email-ul sunt deja folosite
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Eroare: Username-ul este deja luat!");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Eroare: Email-ul este deja folosit!");
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Eroare: Rolul USER nu a fost gasit in DB!"));

        // cream un obiect nou de tip User folosind pattern-ul Builder
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                // cripteam parola cu BCrypt inainte de salvarea in baza de date
                .password(passwordEncoder.encode(request.getPassword()))
                // contul este activ implicit
                .enabled(true)
                .roles(Collections.singleton(userRole))
                .build();

        // salvam user in PostgreSQL
        userRepository.save(user);

        log.info("UTILIZATOR NOU: S-a inregistrat cu succes userul cu email-ul {}"
                , request.getEmail());

        // generam un token JWT pt userul nou creat ca sa se poata loga automat
        String jwtToken = jwtUtil.generateToken(user);

        // returnam token-ul ambalat in obiectul AuthResponse
        return AuthResponse.builder()
                .token(jwtToken)
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        // authenticate trimite credentialele catre DaoAuthenticationProvider
        // daca parola sau username-ul sunt gresite, da o exceptie de securitate
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // autentificarea a avut succes
        // cautam userul in baza de date pentru a-i citi rolurile si datele complete
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Userul nu a fost gasit dupa autentificare"));

        log.info("LOGIN SUCCESS: Userul {} s-a autentificat in sistem", request.getUsername());

        // generam token-ul JWT securizat cu secret key
        String jwtToken = jwtUtil.generateToken(user);

        // returnam raspunsul oficial cu token-ul inauntru
        return AuthResponse.builder()
                .token(jwtToken)
                .build();
    }
}