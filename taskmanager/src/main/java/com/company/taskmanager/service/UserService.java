package com.company.taskmanager.service;

import com.company.taskmanager.entity.Role;
import com.company.taskmanager.entity.User;
import com.company.taskmanager.repository.RoleRepository;
import com.company.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository; // Avem nevoie de acest repository acum

    @Transactional
    public String promoteUserToAdmin(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Eroare: Utilizatorul nu a fost gasit!"));

        // Cautam rolul de admin in baza de date
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new RuntimeException("Eroare: Rolul de ADMIN nu exista in sistem!"));

        // Verificam daca userul are deja acest rol in Set-ul lui
        boolean isAlreadyAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getName().equals(adminRole.getName()));

        if (isAlreadyAdmin) {
            throw new RuntimeException("Eroare: Utilizatorul este deja ADMIN!");
        }

        // Adaugam rolul si salvam
        user.getRoles().add(adminRole);
        userRepository.save(user);

        log.warn("PROMOVARE ADMIN: Utilizatorul '{}' a fost promovat la gradul de ADMIN!", username);

        return "Succes: Utilizatorul " + username + " a fost promovat la gradul de ADMIN!";
    }
}