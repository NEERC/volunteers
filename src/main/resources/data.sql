-- Roles
INSERT IGNORE INTO role
(id, name) VALUES
  (1, 'ROLE_ADMIN'),
  (2, 'ROLE_USER');

-- admin: login 'lapenok.aleksej@gmail.com'
INSERT IGNORE INTO user
(id, badge_name, badge_name_cyr, email, phone, first_name, first_name_cyr, last_name, last_name_cyr, password, role_id)
  VALUE
  (1, 'Aleksej Lapenok', 'Алексей Лапенок', 'lapenok.aleksej@gmail.com', '+7(952)394-36-87', 'Aleksej', 'Алексей',
      'Lapenok', 'Лапенок',
      '$2a$10$NYLS68G3Ic5rkEOVQRZ.2uUHw40NtE50ezrgM/sWYaVymRnKfe5Gi', 1);

CREATE TABLE IF NOT EXISTS oauth_access_token (
  token_id          VARCHAR(255),
  token             BLOB,
  authentication_id VARCHAR(255) PRIMARY KEY,
  user_name         VARCHAR(255),
  client_id         VARCHAR(255),
  authentication    BLOB,
  refresh_token     VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS oauth_refresh_token (
  token_id       VARCHAR(255),
  token          BLOB,
  authentication BLOB
);
