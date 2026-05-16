package com.company.taskmanager.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

// Component spune lui Spring sa creeze automat o instanta din aceasta clasa
// si sa o poata injecta oriunde avem nevoie
@Component
public class JwtUtil {

    // Value,citeste valorile din application.yml si le injecteaza
    // automat Spring la pornirea aplicatiei
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    // transforma string secret intr-o cheie criptografica
    // Keys.hmacShaKeyFor genereaza un Key compatibil cu algoritumul de hash HMAC-SHA
    private Key getSigningKey() {
        byte[] keyBytes = secret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // generateToken, creeaza un JWT token pentru un user autentificat
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        // punem rolurile userului in token ca sa le putem citi fara sa mergem la DB
        claims.put("roles", userDetails.getAuthorities());
        return createToken(claims, userDetails.getUsername());
    }

    // createToken = construieste efectiv token-ul JWT
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)           // datele extra din token (roluri)
                .setSubject(subject)         // "subiectul" token-ului = username-ul
                .setIssuedAt(new Date())     // cand a fost generat token-ul
                .setExpiration(new Date(System.currentTimeMillis() + expiration)) // cand expira
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // semnam cu cheia secreta
                .compact();                  // genereaza string-ul final xxxxx.yyyyy.zzzzz
    }

    // extrage username-ul din token folosit in filtrul JWT pt a sti ce user face request
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // extractClaim,metoda generica pentru a extrage orice informatie din token
    // Function<Claims, T> primeste Claims si returneaza ce tip vrem
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // parseaza token-ul si returneaza toate datele din el
    // daca token-ul e falsificat sau modificat,Jwts.parserBuilder arunca exceptie
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey()) // verifica semnatura cu cheia secreta
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // verifica daca token-ul e valid:
    // - username-ul din token corespunde cu user-ul curent
    // - token-ul nu a expirat
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}