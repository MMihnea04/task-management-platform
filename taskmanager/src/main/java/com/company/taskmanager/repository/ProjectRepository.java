package com.company.taskmanager.repository;

import com.company.taskmanager.entity.Project;
import com.company.taskmanager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// Codul din Query Methods e generat automat de SpringDataJPA
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    // Gaseste toate proiectele active din sistem
    // si ignora-le pe cele soft-deleted
    List<Project> findByDeletedFalse();

    // Gaseste proiectele active detinute de un anumit user
    List<Project> findByOwnerAndDeletedFalse(User owner);

    // Gaseste proiectele active in care un user e membru
    List<Project> findByMembersContainingAndDeletedFalse(User member);
}