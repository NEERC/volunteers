-- Roles
INSERT IGNORE INTO role
  (id, name) VALUES
  (1, 'ROLE_ADMIN'),
  (2, 'ROLE_USER');

-- admin: login 'lapenok.aleksej@gmail.com', password='123'
 INSERT IGNORE INTO user
  (id, badge_name, badge_name_cyr,email,first_name,first_name_cyr,last_name,last_name_cyr,password,role_id) VALUE
  (1, 'Lapenok Aleksej','Алексей Лапенок', 'lapenok.aleksej@gmail.com', 'Aleksej', 'Алексей', 'Lapenok', 'Лапенок', '$2a$10$jw5JfVPuTYmVl6WpxCT0R.NcMxHr9Z1HZf445oSebT0VmRg72Tcxq', 1);

INSERT IGNORE INTO role_users
  (role_id, users_id) VALUE
  (1,1);

-- default positions
INSERT IGNORE INTO position
  (id, name,def) VALUE
  (1,'replacement',TRUE );

-- default hall
INSERT IGNORE INTO hall
  (id, name) VALUE
  (1,'replacement');
