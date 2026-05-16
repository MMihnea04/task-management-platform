package com.company.taskmanager.config;

import com.company.taskmanager.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// OncePerRequestFilter, filtrul ruleaza exact o data per request HTTP
// Component, Spring il detecteaza automat si il include in lantul de filtre
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain  // pt a trece la urmatorul filtru din chain
    ) throws ServletException, IOException {

        // citim header-ul Authorization din request
        // token-ul JWT vine in forma: "Bearer xxxxx.yyyyy.zzzzz"
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // daca nu exista header sau nu incepe cu "Bearer", lasam request-ul sa continue
        // fara autentificare, deoarece poate fi un guest care vrea sa vada pagina de login
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // extragem token-ul din header, scapam de prefixul "Bearer " (7 caractere)
        jwt = authHeader.substring(7);

        username = jwtUtil.extractUsername(jwt);

        // verificam daca avem username si userul nu e deja autentificat in sesiunea curenta
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // incarcam detaliile userului din DB(cu tot username-ul si rolurile lui)
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // comparam daca semnatura se potriveste cu secretul nostru,daca nu e
            // e expirat si daca username-ul din token corespunde cu cel din DB
            if (jwtUtil.validateToken(jwt, userDetails)) {

                // cream obiectul de autentificare cu userul si rolurile lui
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null, // null dupa autentificare, nu mai avem nevoie de parola
                                userDetails.getAuthorities() // rolurile user-ului
                        );

                // adaugam detalii despre request (IP,ID-ul sesiunii etc.)
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // setam userul ca autentificat in contextul Spring Security
                // ,iar de acum Spring stie cine face request-ul
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // continuam cu urmatorul filtru din lant indiferent daca user a fost
        // autentificat sau nu
        filterChain.doFilter(request, response);
    }
}