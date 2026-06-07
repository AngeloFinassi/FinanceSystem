INSERT INTO categories (id, name, type, color, icon, is_default, user_id)
VALUES
(UUID(), 'Salário', 'INCOME', '#4CAF50', 'salary', TRUE, NULL),
(UUID(), 'Freelance', 'INCOME', '#8BC34A', 'money', TRUE, NULL),

(UUID(), 'Alimentação', 'EXPENSE', '#FF6384', 'food', TRUE, NULL),
(UUID(), 'Transporte', 'EXPENSE', '#36A2EB', 'car', TRUE, NULL),
(UUID(), 'Moradia', 'EXPENSE', '#FFCE56', 'home', TRUE, NULL),
(UUID(), 'Lazer', 'EXPENSE', '#9C27B0', 'gamepad', TRUE, NULL),
(UUID(), 'Saúde', 'EXPENSE', '#E91E63', 'health', TRUE, NULL);