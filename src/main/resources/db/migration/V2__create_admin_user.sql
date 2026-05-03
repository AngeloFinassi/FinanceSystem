INSERT INTO users (id, name, email, password, role, created_at)
VALUES (
           UUID(),
           'Administrador',
           'admin@financesystem.com',
           -- bcrypt de 'Admin@2026' — troque em produção
           '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.usd5u.XSq',
           'ADMIN',
           NOW()
       );