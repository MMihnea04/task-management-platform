package com.company.taskmanager.service;

import com.company.taskmanager.dto.ProjectRequest;
import com.company.taskmanager.entity.Project;
import com.company.taskmanager.entity.User;
import com.company.taskmanager.repository.ProjectRepository;
import com.company.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    // verifica daca user-ul curent e owner al proiectului
    public boolean isProjectOwner(Long projectId, String username) {
        return projectRepository.findById(projectId)
                .map(project -> project.getOwner().getUsername().equals(username))
                .orElse(false);
    }

    // Creare proiect nou
    @Transactional
    public Project createProject(ProjectRequest request, String ownerUsername) {
        // cautam user-ul care va deveni ownder in DB si afisam error msg daca nu exista
        User owner = userRepository.findByUsername(ownerUsername)
                .orElseThrow(() -> new RuntimeException("Eroare: Utilizatorul owner nu a fost gasit!"));

        // construim obiectul Project cu pattern-ul Builder
        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .status("ACTIVE")
                .owner(owner) // setam relatia ManyToOne catre owner
                .build();

        // Ne asiguram ca lista de membri este initializata și adaugam ownerul
        if (project.getMembers() == null) {
            project.setMembers(new java.util.HashSet<>());
        }
        project.getMembers().add(owner);

        // salvam in PostgreSQL, logam actiunea si returnam proiectul salvat
        Project savedProject = projectRepository.save(project);

        log.info("PROIECT CREAT: Userul '{}' a creat proiectul '{}' (ID: {})",
                ownerUsername, savedProject.getName(), savedProject.getId());

        return savedProject;
    }

    // Listare toate proiectele active (pt Admin)
    public List<Project> getAllActiveProjects() {
        return projectRepository.findByDeletedFalse();
    }

    // Listare proiecte in care userul curent este membru
    public List<Project> getProjectsForMember(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Eroare: Utilizatorul nu a fost gasit!"));
        return projectRepository.findByMembersContainingAndDeletedFalse(user);
    }

    // Adaugare membru intr-un proiect existent
    // Transactional pt ca daca una dintre operatiuni esueaza, nu se salveaza nimic in DB
    @Transactional
    public Project addMemberToProject(Long projectId, String memberUsername) {
        // cautam proiectul in baza de date/else error message daca nu exista
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Eroare: Proiectul nu a fost gasit!"));

        // ne asiguram ca proiectul nu este sters logic,adica deleted = true
        if (project.isDeleted()) {
            throw new RuntimeException("Eroare: Nu poti adauga membri intr-un proiect sters!");
        }

        // cautam userul care trebuie adaugat ca membru
        User member = userRepository.findByUsername(memberUsername)
                .orElseThrow(() -> new RuntimeException("Eroare: Utilizatorul de adaugat nu a fost gasit!"));

        // adaugam membrul in colectia Set a proiectului
        project.getMembers().add(member);

        log.info("MEMBRU ADAUGAT: Userul '{}' a fost adaugat in proiectul cu ID-ul {}",
                memberUsername, projectId);

        // Într-o metodă @Transactional, Hibernate face automat dirty-checking la finalul metodei.
        // Returnăm direct obiectul, iar salvarea pe disc se face nativ și curat, eliminând bug-urile de merge().
        return project;
    }

    // Modificare informatii proiect
    public Project updateProject(Long projectId, ProjectRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Eroare: Proiectul nu a fost gasit!"));

        if (project.isDeleted()) {
            throw new RuntimeException("Eroare: Nu poti modifica un proiect sters!");
        }

        // actualizam campurile primite
        project.setName(request.getName());
        project.setDescription(request.getDescription());

        Project updatedProject = projectRepository.save(project);

        log.info("PROIECT MODIFICAT: Detaliile proiectului cu ID-ul {} au fost actualizate", projectId);

        return updatedProject;
    }

    // Stergere proiect, SOFT DELETE
    public void softDeleteProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Eroare: Proiectul nu a fost gasit!"));

        // schimba flag de deleted in true
        project.setDeleted(true);

        // salvam starea modificata in DB
        projectRepository.save(project);

        // Folosim WARN pt ca e o actiune distructiva
        log.warn("PROIECT STERS: Proiectul cu ID-ul {} a fost sters logic (soft delete)!", projectId);
    }
}