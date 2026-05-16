package com.company.taskmanager.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "projects")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    // statusul proiectului (ACTIVE, INACTIVE)
    @Column(nullable = false)
    @Builder.Default
    private String status = "ACTIVE";

    // Spring va pune automat data si ora curenta cand salvam proiectul
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // flag pt Soft Delete
    @Column(nullable = false)
    @Builder.Default
    private boolean deleted = false;

    // Mai multe proiecte pot fi create de acelasi user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    // Un proiect are mai multi membri, un user poate fi in mai multe proiecte
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "project_members", // numele tabela de legatura
            joinColumns = @JoinColumn(name = "project_id"), // coloana pt proiectul curent
            inverseJoinColumns = @JoinColumn(name = "user_id") // coloana pt utilizatorul asociat
    )
    // Folosim Set pt a evita cazul in care am incerca sa introducem acelasi user de 2 ori din greseala
    @Builder.Default
    private Set<User> members = new HashSet<>();
}