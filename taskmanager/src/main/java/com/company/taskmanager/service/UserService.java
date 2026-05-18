package com.company.taskmanager.service;

import com.company.taskmanager.entity.Project;
import com.company.taskmanager.entity.Role;
import com.company.taskmanager.entity.User;
import com.company.taskmanager.repository.ProjectRepository;
import com.company.taskmanager.repository.RoleRepository;
import com.company.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ProjectRepository projectRepository; // Injectam repository-ul de proiecte

    @Transactional
    public String modifyUserRole(Long userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Eroare: Utilizatorul nu a fost gasit!"));

        // Formatam automat rolul adaugand "ROLE_" in caz ca vine doar ca "ADMIN" din Postman
        String fullRoleName = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;

        Role newRole = roleRepository.findByName(fullRoleName)
                .orElseThrow(() -> new RuntimeException("Eroare: Rolul " + fullRoleName + " nu exista in sistem!"));

        user.getRoles().clear();
        user.getRoles().add(newRole);
        userRepository.save(user);

        log.warn("MODIFICARE ROL: Utilizatorului '{}' i s-a setat rolul {}!", user.getUsername(), fullRoleName);

        return "Succes: Rolul a fost modificat!";
    }

    @Transactional(readOnly = true)
    public User getMyProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Eroare: Utilizatorul nu a fost gasit!"));

        // Fortam initializarea lazy pentru roluri ca sa nu primim 500 LazyInitializationException in JSON
        user.getRoles().size();
        return user;
    }

    @Transactional
    public User updateMyProfile(String username, String firstName, String lastName, String email) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Eroare: Utilizatorul nu a fost gasit!"));

        if (firstName != null) user.setFirstName(firstName);
        if (lastName != null) user.setLastName(lastName);
        if (email != null) user.setEmail(email);

        user.getRoles().size();
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Object getMyProjects(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Eroare: Utilizatorul nu a fost gasit!"));

        // Interogam direct repository-ul de proiecte, eliminand complet eroarea de getProjects() din User
        List<Project> activeProjects = projectRepository.findByMembersContainingAndDeletedFalse(user);

        activeProjects.forEach(p -> {
            p.getOwner().getRoles().size();
            p.getMembers().forEach(m -> m.getRoles().size());
        });

        return activeProjects;
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        List<User> users = userRepository.findAll();
        // Evitam lazy load exception pentru toti userii din lista
        users.forEach(u -> u.getRoles().size());
        return users;
    }

    @Transactional
    public void deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Eroare: Utilizatorul nu a fost gasit!"));

        user.setEnabled(false);
        userRepository.save(user);
        log.warn("USER DEZACTIVAT: Administratorul a dezactivat contul cu ID-ul {}", userId);
    }
}