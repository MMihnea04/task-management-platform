package com.company.taskmanager.controller;

import com.company.taskmanager.dto.AuthResponse;
import com.company.taskmanager.dto.LoginRequest;
import com.company.taskmanager.dto.RegisterRequest;
import com.company.taskmanager.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// spune lui Spring ca aceasta clasa este un controller REST
// toate metodele de aici vor returna automat date in format JSON catre client
@RestController
// definim ruta de baza pt toate endpoint-urile din aceasta clasa
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // URL complet: POST http://localhost:8080/api/auth/register
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            // interceptreaza JSON-ul trimis de Postman si il mapeaza in obiectul RegisterRequest
            @RequestBody RegisterRequest request
    ) {
        // apelam logica din serviciu si returnam raspunsul cu statusul HTTP OK
        // cu tot cu token-ul JWT generat
        return ResponseEntity.ok(authService.register(request));
    }

    // URL complet: POST http://localhost:8080/api/auth/login
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            // interceptam JSON-ul cu username si parola introduse la login
            @RequestBody LoginRequest request
    ) {
        // apelam serviciul de login si trimitem inapoi token-ul JWT generat
        return ResponseEntity.ok(authService.login(request));
    }
}