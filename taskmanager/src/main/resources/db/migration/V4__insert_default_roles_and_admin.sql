-- Inseram contul de admin on start (parola este "password")
-- Folosim ON CONFLICT  pt a evita erori daca scriptul ruleaza de mai multe ori
INSERT INTO users (id, username, password, email, first_name, last_name, enabled)
VALUES (
    9999,
    'admin_suprem',
    '$2a$10$XURPShQNCsLjp1ESc2laoObo9QZDhxz73hJPaEv7/cBha4pk0AgP.',
    'admin@taskmanager.com',
    'Super',
    'Admin',
    true
)
ON CONFLICT (username) DO NOTHING;

-- Legam contul de admin_suprem la rolul de 'ROLE_ADMIN' in tabela de legatura
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.username = 'admin_suprem' AND r.name = 'ROLE_ADMIN'
ON CONFLICT (user_id, role_id) DO NOTHING;