CREATE TABLE tasks (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    project_id BIGINT NOT NULL,
    creator_id BIGINT NOT NULL,
    assignee_id BIGINT,

    -- Constrangeri de tip FK cu celelalte tabele
    CONSTRAINT fk_task_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_task_creator FOREIGN KEY (creator_id) REFERENCES users(id),
    CONSTRAINT fk_task_assignee FOREIGN KEY (assignee_id) REFERENCES users(id)
);