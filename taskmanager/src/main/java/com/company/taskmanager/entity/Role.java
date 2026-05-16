package com.company.taskmanager.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
// fortam Hibernate sa lege clasa de taelul "roles"
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Spring Security asteapta prefixul "ROLE_" pt roluri(ex. ROLE_ADMIN)
    @Column(nullable = false, unique = true, length = 20)
    private String name;
}