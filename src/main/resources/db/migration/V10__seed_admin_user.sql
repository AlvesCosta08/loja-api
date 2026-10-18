INSERT INTO users (name, email, hashed_password, role)
VALUES (
    'Administrador', 
    'admin@loja.com', 
    '$2a$10$.GCzcJvStRQ85LketjvdU.5qDW9MbKyCq0AWpX9zKKB44Pvtx6dPy', 
    'ADMIN'
) ON CONFLICT (email) DO NOTHING;