-- Crearea tabelei principale pt proiecte
CREATE TABLE projects (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    owner_id BIGINT NOT NULL,

    -- deleted e folosit pt SOFT DELETE
    -- cand stergem un proiect, nu stergem randul din DB, ci doar trecem acest camp pe true
    deleted BOOLEAN NOT NULL DEFAULT FALSE,

    -- FK catre owner-ul proiectului
    CONSTRAINT fk_project_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE
);

-- crearea tabelei de legatura pentru membri (Un proiect are mai multi membri, un utilizator poate fi in mai multe proiecte)
CREATE TABLE project_members (
    project_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (project_id, user_id),

    -- legatura catre tabela de proiecte
    CONSTRAINT fk_member_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,

    -- legatura catre tabela de useri
    CONSTRAINT fk_member_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);