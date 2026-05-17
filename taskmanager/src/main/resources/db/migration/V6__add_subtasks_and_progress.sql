-- Adaugam coloana pt progresul task-ului
ALTER TABLE tasks ADD COLUMN progress INT DEFAULT 0 NOT NULL;

-- Cream tabela pt subtask-uri
-- am folosit ON DELETE CASCADE in cazul in care stergem un task, se sterg si toate subtask-urile
CREATE TABLE sub_tasks (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    completed BOOLEAN DEFAULT FALSE NOT NULL,
    task_id BIGINT NOT NULL,
    CONSTRAINT fk_sub_tasks_task FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE
);