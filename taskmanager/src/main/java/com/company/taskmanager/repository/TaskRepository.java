package com.company.taskmanager.repository;

import com.company.taskmanager.entity.Task;
import com.company.taskmanager.entity.TaskStatus;
import com.company.taskmanager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    // Returneaza toate task-urile dintr-un proiect
    List<Task> findByProjectId(Long projectId);

    // Numara task-urile active pt un anumit assignee
    long countByAssigneeAndStatusIn(User assignee, List<TaskStatus> statuses);
}