package com.company.taskmanager.repository;

import com.company.taskmanager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// Repository spune lui Spring ca aceasta e o clasa de acces la date
@Repository
// JpaRepository<User, Long> = lucram cu entitatea User, iar cheia primara e de tip Long
// SpringDataJPA genereaza automat metode pt operatii CRUD: findAll(), findById(), save(), etc.
public interface UserRepository extends JpaRepository<User, Long> {

    // SpringDataJPA genereaza automat SQL-ul pt metodele ce incep cu "findBy"
    // ,iar Optional ne va ajuta ca in loc de null sa fie returnat un mesaj de eroare
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    // cu existsBy... verif strict daca exista deja cineva cu username-ul sau email-ul acela
    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}