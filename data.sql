-- write users
INSERT IGNORE INTO user
(id, badge_name, badge_name_cyr, email, first_name, first_name_cyr, last_name, last_name_cyr, password, role_id) VALUES
  (2, 'user0', 'user0', 'user0', 'user0', 'user0', 'user0', 'user0', 'user0', 2),
  (3, 'user1', 'user1', 'user1', 'user1', 'user1', 'user1', 'user1', 'user1', 2),
  (4, 'user2', 'user2', 'user2', 'user2', 'user2', 'user2', 'user2', 'user2', 2),
  (5, 'user3', 'user3', 'user3', 'user3', 'user3', 'user3', 'user3', 'user3', 2),
  (6, 'user4', 'user4', 'user4', 'user4', 'user4', 'user4', 'user4', 'user4', 2),
  (7, 'user5', 'user5', 'user5', 'user5', 'user5', 'user5', 'user5', 'user5', 2),
  (8, 'user6', 'user6', 'user6', 'user6', 'user6', 'user6', 'user6', 'user6', 2),
  (9, 'user7', 'user7', 'user7', 'user7', 'user7', 'user7', 'user7', 'user7', 2),
  (10, 'user8', 'user8', 'user8', 'user8', 'user8', 'user8', 'user8', 'user8', 2),
  (11, 'user9', 'user9', 'user9', 'user9', 'user9', 'user9', 'user9', 'user9', 2),
  (12, 'user10', 'user10', 'user10', 'user10', 'user10', 'user10', 'user10', 'user10', 2),
  (13, 'user11', 'user11', 'user11', 'user11', 'user11', 'user11', 'user11', 'user11', 2),
  (14, 'user12', 'user12', 'user12', 'user12', 'user12', 'user12', 'user12', 'user12', 2),
  (15, 'user13', 'user13', 'user13', 'user13', 'user13', 'user13', 'user13', 'user13', 2),
  (16, 'user14', 'user14', 'user14', 'user14', 'user14', 'user14', 'user14', 'user14', 2),
  (17, 'user15', 'user15', 'user15', 'user15', 'user15', 'user15', 'user15', 'user15', 2),
  (18, 'user16', 'user16', 'user16', 'user16', 'user16', 'user16', 'user16', 'user16', 2),
  (19, 'user17', 'user17', 'user17', 'user17', 'user17', 'user17', 'user17', 'user17', 2),
  (20, 'user18', 'user18', 'user18', 'user18', 'user18', 'user18', 'user18', 'user18', 2),
  (21, 'user19', 'user19', 'user19', 'user19', 'user19', 'user19', 'user19', 'user19', 2);
-- write year
INSERT IGNORE INTO year
(id, name, open_for_registration) VALUE
  (1, '2016', TRUE);
-- write application form
INSERT IGNORE INTO application_form
(id, `group`, suggestions, user_id, year) VALUES
  (1, 'M3334', 'no suggestion', 2, 1),
  (2, 'M3335', 'no suggestion', 3, 1),
  (3, 'M3336', 'no suggestion', 4, 1),
  (4, 'M3337', 'no suggestion', 5, 1),
  (5, 'M3338', 'no suggestion', 6, 1),
  (6, 'M3334', 'no suggestion', 7, 1),
  (7, 'M3335', 'no suggestion', 8, 1),
  (8, 'M3336', 'no suggestion', 9, 1),
  (9, 'M3337', 'no suggestion', 10, 1),
  (10, 'M3338', 'no suggestion', 11, 1),
  (11, 'M3334', 'no suggestion', 12, 1),
  (12, 'M3335', 'no suggestion', 13, 1),
  (13, 'M3336', 'no suggestion', 14, 1),
  (14, 'M3337', 'no suggestion', 15, 1),
  (15, 'M3338', 'no suggestion', 16, 1),
  (16, 'M3334', 'no suggestion', 17, 1),
  (17, 'M3335', 'no suggestion', 18, 1),
  (18, 'M3336', 'no suggestion', 19, 1),
  (19, 'M3337', 'no suggestion', 20, 1),
  (20, 'M3338', 'no suggestion', 21, 1);
