-- Roles
INSERT IGNORE INTO role VALUES
  (1, 'ROLE_ADMIN'),
  (2, 'ROLE_USER');

-- admin: login 'lapenok.aleksej@gmail.com', password='123'
 INSERT IGNORE INTO user VALUE
   (1, 'Lapenok Aleksej','Алексей Лапенок', 'lapenok.aleksej@gmail.com', 'Aleksej', 'Алексей', 'Lapenok', 'Лапенок', '$2a$10$jw5JfVPuTYmVl6WpxCT0R.NcMxHr9Z1HZf445oSebT0VmRg72Tcxq', 1);


-- default positions
INSERT IGNORE INTO position VALUE
  (1,'replacement',TRUE );

-- default hall
INSERT IGNORE INTO hall VALUE
  (1,'replacement');
