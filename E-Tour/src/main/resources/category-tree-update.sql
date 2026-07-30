-- =====================================================================================
--  e-Tour  |  Category tree + Flag  (DB Documentation v1.0, Flow + Data sheets)
--
--  Run this ONCE against an existing etour_db that already has data loaded.
--  Safe to re-run: every statement is idempotent.
--
--  Order matters: Section A adds columns, B builds the tree, C re-points the tours.
-- =====================================================================================

USE etour_db;

-- =====================================================================================
--  SECTION A - new columns on CATEGORY
--
--  If you have already restarted the app with ddl-auto=update, Hibernate has added
--  these and each ALTER will fail with "Duplicate column name". That error is safe -
--  skip to Section B.
--
--  Check first:   SHOW COLUMNS FROM category;
-- =====================================================================================

ALTER TABLE category ADD COLUMN cat_code       VARCHAR(10);
ALTER TABLE category ADD COLUMN parent_id      INT;
ALTER TABLE category ADD COLUMN cat_image_path VARCHAR(255);
ALTER TABLE category ADD COLUMN flag           BIT(1);

ALTER TABLE category
  ADD CONSTRAINT fk_category_parent
  FOREIGN KEY (parent_id) REFERENCES category (category_id);

-- =====================================================================================
--  SECTION B - the tree
--
--  parent_id NULL = the sheet's '^'  (root, shown on the Home page)
--  flag = 1 (Y)   = leaf  -> clicking jumps STRAIGHT to the tour listing
--  flag = 0 (N)   = node  -> clicking drills down to the children
--
--  Home page     : SELECT * FROM category WHERE parent_id IS NULL;
--  Further pages : SELECT * FROM category WHERE parent_id = <selected>;
-- =====================================================================================

-- B1. Re-align the five rows that already exist (INSERT IGNORE will not touch them)
UPDATE category SET cat_code='DOM', parent_id=NULL, category_name='Domestic',
       cat_image_path='/img/cat/domestic.jpg',      flag=0, status='ACTIVE' WHERE category_id=1;
UPDATE category SET cat_code='INT', parent_id=NULL, category_name='International',
       cat_image_path='/img/cat/international.jpg', flag=0, status='ACTIVE' WHERE category_id=2;
UPDATE category SET cat_code='VS',  parent_id=NULL, category_name='Varsha Sahal',
       cat_image_path='/img/cat/varsha-sahal.jpg',  flag=1, status='ACTIVE' WHERE category_id=3;
UPDATE category SET cat_code='ADV', parent_id=NULL, category_name='Adventure',
       cat_image_path='/img/cat/adventure.jpg',     flag=0, status='ACTIVE' WHERE category_id=4;
UPDATE category SET cat_code='CKD', parent_id=NULL, category_name='Cricket Dhamaka',
       cat_image_path='/img/cat/cricket.jpg',       flag=1, status='ACTIVE' WHERE category_id=5;

-- B2. Roots  (parent_id NULL)
INSERT IGNORE INTO category
  (category_id, cat_code, parent_id, category_name, cat_image_path, flag, description, suitable_for, status) VALUES
  (29,'PIL', NULL, 'Pilgrimage',      '/img/cat/pilgrimage.jpg', 0, 'Faith and heritage journeys', 'Families',       'ACTIVE'),
  (31,'CPL', NULL, 'Couple Tours',    '/img/cat/couple.jpg',     1, 'Honeymoon and romantic getaways','Couples',     'ACTIVE');

-- B3. Level 2
INSERT IGNORE INTO category
  (category_id, cat_code, parent_id, category_name, cat_image_path, flag, description, suitable_for, status) VALUES
  (6, 'EUR', 2,  'Europe',            '/img/cat/europe.jpg',     0, 'European circuits',        'All travellers','ACTIVE'),
  (7, 'SEA', 2,  'South East Asia',   '/img/cat/sea.jpg',        0, 'South East Asian circuits','All travellers','ACTIVE'),
  (8, 'SLK', 2,  'Sri Lanka',         '/img/cat/srilanka.jpg',   0, 'Sri Lankan circuits',      'All travellers','ACTIVE'),
  (12,'CHI', 2,  'China',             '/img/cat/china.jpg',      0, 'China circuits',           'All travellers','ACTIVE'),
  (28,'MDE', 2,  'Middle East',       '/img/cat/middleeast.jpg', 1, 'Dubai and the Gulf',       'All travellers','ACTIVE'),
  (17,'HP',  1,  'Himachal Pradesh',  '/img/cat/hp.jpg',         0, 'Himachal circuits',        'All travellers','ACTIVE'),
  (18,'ASM', 1,  'Assam',             '/img/cat/assam.jpg',      1, 'North East circuits',      'All travellers','ACTIVE'),
  (19,'KSH', 1,  'Kashmir',           '/img/cat/kashmir.jpg',    0, 'Kashmir circuits',         'All travellers','ACTIVE'),
  (20,'RAJ', 1,  'Rajasthan',         '/img/cat/rajasthan.jpg',  1, 'Rajasthan circuits',       'All travellers','ACTIVE'),
  (21,'KER', 1,  'Kerala',            '/img/cat/kerala.jpg',     1, 'Kerala circuits',          'All travellers','ACTIVE'),
  (22,'GOA', 1,  'Goa',               '/img/cat/goa.jpg',        1, 'Beach breaks in Goa',      'All travellers','ACTIVE'),
  (27,'LEH', 4,  'Leh Ladakh',        '/img/cat/leh.jpg',        1, 'High altitude Ladakh',     'Young adults', 'ACTIVE'),
  (30,'CHD', 29, 'Char Dham Yatra',   '/img/cat/chardham.jpg',   1, 'The four-shrine circuit',  'Families',     'ACTIVE');

-- B4. Level 3
INSERT IGNORE INTO category
  (category_id, cat_code, parent_id, category_name, cat_image_path, flag, description, suitable_for, status) VALUES
  (9, 'GEU', 6,  'Glimps of Europe',   '/img/cat/geu.jpg',     1, 'Multi-country European tour',   'All travellers','ACTIVE'),
  (10,'AUS', 6,  'Austria Switzerland','/img/cat/austria.jpg', 1, 'Alpine Austria and Switzerland','Couples',       'ACTIVE'),
  (11,'ECD', 6,  'Cricket Dhamaka',    '/img/cat/ecd.jpg',     1, 'Europe with a Test match',      'Sports fans',   'ACTIVE'),
  (13,'CHT', 12, 'China with Tibet',   '/img/cat/tibet.jpg',   1, 'China extended into Tibet',     'All travellers','ACTIVE'),
  (14,'CHH', 12, 'China with HK',      '/img/cat/chinahk.jpg', 1, 'China with Hong Kong',          'All travellers','ACTIVE'),
  (15,'SLC', 8,  'Sri Lanka with Cricket Dhamaka','/img/cat/slc.jpg',1,'Sri Lanka around a match', 'Sports fans',   'ACTIVE'),
  (16,'LNK', 8,  'Beauty of Srilanka', '/img/cat/lnk.jpg',     1, 'Classic Sri Lanka circuit',     'All travellers','ACTIVE'),
  (23,'SHM', 17, 'Shimla',             '/img/cat/shimla.jpg',  1, 'Shimla and around',             'All travellers','ACTIVE'),
  (24,'SKM', 17, 'Sikkim',             '/img/cat/sikkim.jpg',  1, 'Sikkim circuit',                'All travellers','ACTIVE'),
  (25,'KVD', 19, 'Kashmir Vaishnodevi','/img/cat/kvd.jpg',     1, 'Kashmir with Vaishno Devi',     'Families',      'ACTIVE'),
  (26,'JKG', 19, 'Jammu-Kashmir-Gulmarg','/img/cat/jkg.jpg',   1, 'Classic Kashmir circuit',       'All travellers','ACTIVE');

-- B5. Safety net - any row Hibernate created with a NULL flag becomes a drill-down node
UPDATE category SET flag = 0 WHERE flag IS NULL;

-- =====================================================================================
--  SECTION C - re-point the tours onto LEAF nodes
--
--  This matters. The seeded tours point at category 1, 2, 3, 4 and 5, which are now
--  drill-down roots. A tour hanging off a root is unreachable, because the UI shows
--  that node's CHILDREN rather than its tours.
--
--  Category 3, 4 and 5 also changed meaning: they were Couple / Pilgrimage / Events
--  and are now Varsha Sahal / Adventure / Cricket Dhamaka per the Excel.
-- =====================================================================================

UPDATE tour SET category_id = 26 WHERE tour_id = 1;   -- Kashmir Paradise    -> Jammu-Kashmir-Gulmarg
UPDATE tour SET category_id = 20 WHERE tour_id = 2;   -- Rajasthan Royal     -> Rajasthan
UPDATE tour SET category_id = 21 WHERE tour_id = 3;   -- Kerala Backwaters   -> Kerala
UPDATE tour SET category_id = 22 WHERE tour_id = 4;   -- Goa Beach Escape    -> Goa
UPDATE tour SET category_id = 9  WHERE tour_id = 5;   -- Paris & Vienna      -> Glimps of Europe
UPDATE tour SET category_id = 7  WHERE tour_id = 6;   -- Bali Escape         -> South East Asia
UPDATE tour SET category_id = 28 WHERE tour_id = 7;   -- Dubai Delight       -> Middle East
UPDATE tour SET category_id = 27 WHERE tour_id = 8;   -- Leh Ladakh          -> Leh Ladakh
UPDATE tour SET category_id = 30 WHERE tour_id = 9;   -- Char Dham Yatra     -> Char Dham Yatra
UPDATE tour SET category_id = 31 WHERE tour_id = 10;  -- Maldives Honeymoon  -> Couple Tours

-- South East Asia now holds a tour and has no children, so make it a leaf explicitly
UPDATE category SET flag = 1 WHERE category_id = 7;

-- =====================================================================================
--  SECTION D - verification
-- =====================================================================================

-- D1. The Home page query - expect 7 tiles
SELECT category_id, cat_code, category_name, flag,
       CASE WHEN flag = 1 THEN 'SHOW_TOURS' ELSE 'SHOW_CATEGORIES' END AS next_action
FROM category WHERE parent_id IS NULL ORDER BY category_id;

-- D2. Drill down into International - expect Europe, SEA, Sri Lanka, China, Middle East
SELECT category_id, cat_code, category_name, flag FROM category WHERE parent_id = 2;

-- D3. Third level - China with Tibet, China with HK
SELECT category_id, cat_code, category_name, flag FROM category WHERE parent_id = 12;

-- D4. Every tour must sit on a node that has NO children, or it is unreachable
SELECT t.tour_id, t.tour_name, c.cat_code, c.category_name, c.flag,
       (SELECT COUNT(*) FROM category k WHERE k.parent_id = c.category_id) AS child_count
FROM tour t JOIN category c ON c.category_id = t.category_id
ORDER BY t.tour_id;

-- D5. Orphan check - should return zero rows
SELECT t.tour_id, t.tour_name, c.category_name AS unreachable_under
FROM tour t JOIN category c ON c.category_id = t.category_id
WHERE (SELECT COUNT(*) FROM category k WHERE k.parent_id = c.category_id) > 0;

-- D6. Full tree, ordered
SELECT c.category_id, c.cat_code, c.category_name, c.flag,
       p.cat_code AS parent_code,
       (SELECT COUNT(*) FROM category k WHERE k.parent_id = c.category_id) AS children,
       (SELECT COUNT(*) FROM tour t WHERE t.category_id = c.category_id)   AS tours
FROM category c LEFT JOIN category p ON p.category_id = c.parent_id
ORDER BY COALESCE(c.parent_id, c.category_id), c.category_id;

-- =====================================================================================
--  SECTION D - EVENTS category  (added 30 July 2026)
--
--  New root with three drill-down children. Every child is a leaf (flag = 1) so a
--  click goes straight to the tour listing.
-- =====================================================================================

INSERT IGNORE INTO category
  (category_id, cat_code, parent_id, category_name, cat_image_path, flag, description, suitable_for, status) VALUES
  (32,'EVT', NULL, 'Events',                '/images/tours/goa-festival.png',  0, 'Festivals, carnivals, concerts and sporting events','All travellers','ACTIVE'),
  (33,'CAR', 32,   'Carnivals & Festivals', '/images/tours/goa-festival.png',  1, 'Street carnivals and cultural festivals','All travellers','ACTIVE'),
  (34,'SPT', 32,   'Sporting Events',       '/images/tours/dubai-skyline.png', 1, 'Match-day and tournament packages','Sports fans','ACTIVE'),
  (35,'MUS', 32,   'Music & Concerts',      '/images/tours/goa-festival.png',  1, 'Live music weekends','Young adults','ACTIVE');

UPDATE category SET flag = 0 WHERE category_id = 32;
UPDATE category SET flag = 1 WHERE category_id IN (33, 34, 35);

-- =====================================================================================
--  SECTION E - image paths move from /img/cat/ to the served /images/ folder
--
--  StaticResourceConfig maps /images/** to the backend "images" directory on disk.
-- =====================================================================================

UPDATE category SET cat_image_path = REPLACE(cat_image_path, '/img/cat/', '/images/tours/')
 WHERE cat_image_path LIKE '/img/cat/%';

-- =====================================================================================
--  SECTION F - the three Events tours
--
--  Run only if tours 11, 12 and 13 do not already exist.
-- =====================================================================================

UPDATE tour SET category_id = 33 WHERE tour_id IN (11, 13);
UPDATE tour SET category_id = 34 WHERE tour_id = 12;
