package com.company.taskmanager.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "Username-ul este obligatoriu!")
    private String username;

    @NotBlank(message = "Email-ul este obligatoriu!")
    @Email(message = "Format email invalid!")
    private String email;

    @NotBlank(message = "Parola este obligatorie!")
    private String password;
}