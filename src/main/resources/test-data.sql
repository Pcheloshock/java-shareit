-- Очистка таблиц
DELETE FROM comments;
DELETE FROM bookings;
DELETE FROM items;
DELETE FROM users;

-- Сброс последовательностей
ALTER TABLE users ALTER COLUMN id RESTART WITH 1;
ALTER TABLE items ALTER COLUMN id RESTART WITH 1;
ALTER TABLE bookings ALTER COLUMN id RESTART WITH 1;
ALTER TABLE comments ALTER COLUMN id RESTART WITH 1;

-- Добавление тестовых пользователей
INSERT INTO users (name, email) VALUES
('Test User', 'test@example.com'),
('Owner User', 'owner@example.com');

-- Добавление тестовых вещей
INSERT INTO items (name, description, is_available, owner_id) VALUES
('Test Item', 'Test Description', true, 2);

-- Добавление завершенного бронирования для теста комментария
INSERT INTO bookings (start_date, end_date, item_id, booker_id, status) VALUES
('2024-01-01 10:00:00', '2024-01-10 10:00:00', 1, 1, 'APPROVED');