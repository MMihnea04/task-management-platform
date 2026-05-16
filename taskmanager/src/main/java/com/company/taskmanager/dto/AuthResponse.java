package com.company.taskmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    // pachetul pe care Postman il trimite inapoi daca login/register au mers
    // contine token-ul JWT generat de server format din xxxxx.yyyyy.zzzzz
    private String token;
}