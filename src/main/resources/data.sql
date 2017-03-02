-- Roles
INSERT IGNORE INTO role VALUES
  (1, 'ROLE_ADMIN'),
  (2, 'ROLE_USER');

-- admin
INSERT IGNORE INTO user VALUE
  (1, 'Lapenok Aleksej', 'lapenok.aleksej@gmail.com', 'Алексей', '123', 'Лапенок', 1);

-- default positions
INSERT IGNORE INTO position VALUE
  (1,'replacement',TRUE );

-- default hall
INSERT IGNORE INTO hall VALUE
  (1,'replacement');