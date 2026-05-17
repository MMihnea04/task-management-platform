package com.company.taskmanager.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

// Genereaza automat getteri, setteri, toString and so on
@Data
// Putem crea obiecte de format User.builder().email("x").build()
@Builder
// Constuctor fara parametri pt Hibernate cand creaza obiecte din DB
@NoArgsConstructor
// Constructor cu toti parametrii,necesar pt adnotarea Builder
@AllArgsConstructor
// Comunica lui Hibernate ca aceasta clasa e o tabela in DB
@Entity
// Specificam numele tabelei din DB (cea creata de Flyway in V1)
@Table(name = "users")
// Adnotarea de mai jos rezolva eroarea 500 si securizeaza datele sensibile
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "authorities"})
// Interfata UserDetails prin care Spring Security stie cum sa lucreze cu user-ul nostru
public class User implements UserDetails {
    // Id e primary key al tabelei
    @Id

    // GeneratedValue se incrementeaza singur de unde a ramas pt ca am am definit Id SERIAL
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Column,val username trebuie sa fie unica,sa nu fie null si sa aiba maxim 50 de caractere
    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 100)
    private String password; // parola va fi salvata encryptata cu BCrypt

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(name = "first_name", length = 50)
    private String firstName;

    @Column(name = "last_name", length = 50)
    private String lastName;

    // enabled = true pt ca daca nu e specificat tipul campului enable trebuie sa il avem true
    // ex. caz cand cream noi useri,iar in Java default pt bool e false,si nullable = false pt
    // ca nu poate exista intr-o stare ambigua
    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    // ManyToMany, un user poate avea mai multe roluri si viceversa
    // FetchType.EAGER, cand incarcam userul,incarcam si rolurile lui imediat
    // JoinTable, definim tabela de legatura user_roles creata in V1__init_schema.sql
    // Private Set ca sa nu existe dubluri si primeste un hash pt a nu fi intializata ca o lista goala
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    // primim prin roles.stream() rolurile user-ului si le transformam in SimpleGrantedAuthority
    // pt a satisface formatul Spring Security, iar apoi adaugam in set pt a evita dubluri
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toSet());
    }

    // daca user si parola sunt bune, se trece rapid la verificarea celor 4 metode
    // daca sunt false, autentificarea esueaza automat cu o exceptie de securitate
    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    // isEnabled, Spring Security verifica daca userul e activ
    // daca e false, autentificarea esueaza automat
    @Override
    public boolean isEnabled() { return enabled; }
}