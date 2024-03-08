--liquibase formatted sql
--changeset Nikita:add-test-data

INSERT INTO ROLES (name)
VALUES ('ROLE_ADMINISTRATOR'),
       ('ROLE_OPERATOR'),
       ('ROLE_USER')
ON CONFLICT DO NOTHING;

INSERT INTO USERS (id, username, password, email, phone_number, name)
--     Password in the next row is BCrypted 'casualpassword',
VALUES ('47400383-1246-4604-b391-6dbf64fdaf89', 'casualuser',
        '$2a$12$c3ISpKHKcCO/Oa4AuHVk5uuKms7hcE59pZJg8ilOMNG5DTS9MNF6q',
        null, '+79818742341', 'Vasiliy Grigoriev'),
--     Password in the next row is BCrypted 'futurepassword'
       ('25571ff4-a568-45c5-9e66-38050175a26d', 'futureoperator',
        '$2a$12$vXj6fY90EW4sJDm.bknyNujAlt4VFMZKmRej1oY.eNpPX.5Smpaq6',
        null, '+798187423422', 'Nikolay Botogin'),
--     Password in the next row is BCrypted 'operatorpassword',
       ('fb69fd9f-666d-476c-a90d-cfced4eebe1d', 'operator',
        '$2a$12$ld.iFGxWw8A1ti7gljE2yO0Kt.94CKaSwCo1XP.1C4BJDpZGYj4.q',
        null, '+79818742343', 'Viktor Vernev'),
--     Password in the next row is BCrypted 'adminpassword',
       ('8b6dd6ae-b76b-4b77-a818-275e094f7228', 'admin', '$2a$12$uj17G8LjgiAvrcOQi4aqF.DGN3fXVHkA8bJzsiekEhbR1GNxac/Rq',
        null, '+79818742344', 'Roman Lichin')
ON CONFLICT DO NOTHING;

INSERT INTO USERS_ROLES (user_id, role_id)
VALUES ('47400383-1246-4604-b391-6dbf64fdaf89', 3),
       ('25571ff4-a568-45c5-9e66-38050175a26d', 3),
       ('fb69fd9f-666d-476c-a90d-cfced4eebe1d', 2),
       ('8b6dd6ae-b76b-4b77-a818-275e094f7228', 1)
ON CONFLICT DO NOTHING;