//for development purpose only, 
please do insert dummy data after running command 
shared by atharva 


-- =====================================================================
-- Dummy data for Tourism ERD
-- Notes:
--   - Written for MySQL-style syntax (explicit PK values on
--     AUTO_INCREMENT columns are allowed). If you're on Postgres,
--     replace with `OVERRIDING SYSTEM VALUE` or drop the id columns
--     and let identity generate them, then adjust FK values accordingly.
--   - Insert order respects FK dependencies.
--   - ROLE = 2 rows only (admin, user) and USER = 5 rows, as requested.
--     Every other table has 10 rows minimum.
-- =====================================================================

-- ---------------------------------------------------------------------
-- ROLE (2 rows only)
-- ---------------------------------------------------------------------
INSERT INTO role (role_id, role_name, description) VALUES
(1, 'ADMIN', 'Full access to manage tours, bookings and users'),
(2, 'USER',  'Regular customer who can browse and book tours');

-- ---------------------------------------------------------------------
-- USER (5 rows)
-- ---------------------------------------------------------------------
INSERT INTO user (user_id, username, password, email, role_id, created_at, isactive) VALUES
(1, 'admin.singh',   '$2a$10$hashA1', 'admin.singh@example.com',   1, '2025-01-05 09:00:00', 1),
(2, 'priya.sharma',  '$2a$10$hashA2', 'priya.sharma@example.com',  2, '2025-01-10 10:15:00', 1),
(3, 'rahul.mehta',   '$2a$10$hashA3', 'rahul.mehta@example.com',   2, '2025-02-02 11:30:00', 1),
(4, 'anita.kapoor',  '$2a$10$hashA4', 'anita.kapoor@example.com',  2, '2025-02-18 08:45:00', 1),
(5, 'vikram.rao',    '$2a$10$hashA5', 'vikram.rao@example.com',    2, '2025-03-01 14:20:00', 0);

-- ---------------------------------------------------------------------
-- LANGUAGE (10 rows)
-- ---------------------------------------------------------------------
INSERT INTO language (language_id, language_code, language_name) VALUES
(1,  'en', 'English'),
(2,  'hi', 'Hindi'),
(3,  'fr', 'French'),
(4,  'es', 'Spanish'),
(5,  'de', 'German'),
(6,  'ja', 'Japanese'),
(7,  'zh', 'Chinese'),
(8,  'ar', 'Arabic'),
(9,  'pt', 'Portuguese'),
(10, 'ru', 'Russian');

-- ---------------------------------------------------------------------
-- CATEGORY (10 rows)
-- ---------------------------------------------------------------------
INSERT INTO category (category_id, category_name, description, suitable_for, status) VALUES
(1,  'Adventure',   'Trekking, rafting and outdoor thrill activities', 'Youth, Groups',        'ACTIVE'),
(2,  'Beach',       'Coastal getaways and island tours',               'Couples, Family',      'ACTIVE'),
(3,  'Cultural',    'Heritage sites, museums and local traditions',    'Families, Seniors',    'ACTIVE'),
(4,  'Wildlife',    'Safaris and national park tours',                 'Nature lovers',        'ACTIVE'),
(5,  'Honeymoon',   'Romantic getaways for couples',                   'Couples',              'ACTIVE'),
(6,  'Pilgrimage',  'Religious and spiritual destinations',            'All ages',             'ACTIVE'),
(7,  'Hill Station','Mountain resorts and scenic hill towns',          'Families, Couples',    'ACTIVE'),
(8,  'City Break',  'Short trips to major cities',                     'Solo, Business',       'ACTIVE'),
(9,  'Cruise',      'Ocean and river cruise packages',                 'Family, Luxury',       'ACTIVE'),
(10, 'Luxury',      'Premium tours with 5-star stays',                 'Luxury travelers',     'ACTIVE');

-- ---------------------------------------------------------------------
-- SUB_CATEGORY_MASTER (10 rows) - cat_id references category
-- ---------------------------------------------------------------------
INSERT INTO sub_category_master (subcat_id, cat_id, subcat_name, subcat_image_path, flag, isactive) VALUES
(1,  1, 'Trekking',        '/images/subcat/trekking.jpg',        1, 1),
(2,  1, 'River Rafting',   '/images/subcat/rafting.jpg',          0, 1),
(3,  2, 'Island Hopping',  '/images/subcat/island.jpg',           1, 1),
(4,  3, 'Heritage Walks',  '/images/subcat/heritage.jpg',         0, 1),
(5,  4, 'Jungle Safari',   '/images/subcat/safari.jpg',           1, 1),
(6,  5, 'Romantic Escape', '/images/subcat/honeymoon.jpg',        0, 1),
(7,  6, 'Temple Tours',    '/images/subcat/temple.jpg',           0, 1),
(8,  7, 'Mountain Resorts','/images/subcat/hillstation.jpg',      1, 1),
(9,  9, 'River Cruise',    '/images/subcat/cruise.jpg',           0, 1),
(10, 10,'Luxury Villas',   '/images/subcat/luxury.jpg',           1, 1);

-- ---------------------------------------------------------------------
-- TOUR (10 rows) - category_id references category
-- ---------------------------------------------------------------------
INSERT INTO tour (tour_id, tour_name, destination, days, nights, description, price, category_id, location) VALUES
(1,  'Himalayan Trek Adventure',   'Manali',      6, 5, 'Guided trek through the Himalayan foothills', 24999.00, 1,  'Himachal Pradesh'),
(2,  'Goa Beach Bliss',            'Goa',         4, 3, 'Relaxing beach holiday with water sports',    18999.00, 2,  'Goa'),
(3,  'Rajasthan Heritage Tour',    'Jaipur',      7, 6, 'Explore forts, palaces and local culture',    32999.00, 3,  'Rajasthan'),
(4,  'Ranthambore Wildlife Safari','Ranthambore', 3, 2, 'Tiger safari and nature walks',                21999.00, 4,  'Rajasthan'),
(5,  'Maldives Honeymoon Special', 'Maldives',    5, 4, 'Overwater villas and private beach dinners',   89999.00, 5,  'Maldives'),
(6,  'Char Dham Yatra',            'Uttarakhand', 8, 7, 'Sacred pilgrimage to the four holy shrines',   27999.00, 6,  'Uttarakhand'),
(7,  'Shimla Manali Hill Escape',  'Shimla',      6, 5, 'Scenic hill station tour with valley views',   22999.00, 7,  'Himachal Pradesh'),
(8,  'Dubai City Explorer',        'Dubai',       4, 3, 'City tour with desert safari and skyline views',45999.00, 8,  'UAE'),
(9,  'Kerala Backwater Cruise',    'Alleppey',    3, 2, 'Houseboat cruise through tranquil backwaters', 26999.00, 9,  'Kerala'),
(10, 'Swiss Alps Luxury Tour',     'Zurich',      9, 8, 'Premium alpine experience with 5-star stays',  159999.00,10, 'Switzerland');

-- ---------------------------------------------------------------------
-- SCHEDULE (10 rows)
-- ---------------------------------------------------------------------
INSERT INTO schedule (schedule_id, tour_id, available_seats, total_seats, status, start_date) VALUES
(1,  1,  12, 20, 'OPEN',      '2026-09-10'),
(2,  2,  25, 30, 'OPEN',      '2026-08-15'),
(3,  3,  8,  25, 'OPEN',      '2026-10-01'),
(4,  4,  5,  15, 'FILLING',   '2026-09-05'),
(5,  5,  4,  10, 'FILLING',   '2026-11-20'),
(6,  6,  0,  20, 'CLOSED',    '2026-08-01'),
(7,  7,  18, 20, 'OPEN',      '2026-09-25'),
(8,  8,  10, 20, 'OPEN',      '2026-10-12'),
(9,  9,  14, 18, 'OPEN',      '2026-09-18'),
(10, 10, 6,  12, 'FILLING',   '2026-12-05');

-- ---------------------------------------------------------------------
-- ITINERARY (10 rows)
-- ---------------------------------------------------------------------
INSERT INTO itinerary (itinerary_id, tour_id, day_number, description, location) VALUES
(1,  1,  1, 'Arrival in Manali and local acclimatization walk',   'Manali'),
(2,  2,  1, 'Arrival and beach relaxation at Calangute',          'Goa'),
(3,  3,  1, 'Arrival in Jaipur and Amber Fort visit',             'Jaipur'),
(4,  4,  1, 'Arrival and evening nature walk',                    'Ranthambore'),
(5,  5,  1, 'Arrival and welcome dinner at overwater villa',      'Maldives'),
(6,  6,  1, 'Departure to Yamunotri shrine',                      'Uttarakhand'),
(7,  7,  1, 'Arrival in Shimla and Mall Road walk',                'Shimla'),
(8,  8,  1, 'City tour covering Burj Khalifa and Dubai Mall',      'Dubai'),
(9,  9,  1, 'Boarding houseboat and backwater cruise begins',      'Alleppey'),
(10, 10, 1, 'Arrival in Zurich and city orientation tour',         'Zurich');

-- ---------------------------------------------------------------------
-- JOURNEY (10 rows)
-- ---------------------------------------------------------------------
INSERT INTO journey (journey_id, tour_id, source, destination, transport, details) VALUES
(1,  1,  'Delhi',     'Manali',    'Bus',    'Overnight Volvo bus with a comfort stop en route'),
(2,  2,  'Mumbai',    'Goa',       'Flight', 'Direct 1-hour flight to Dabolim airport'),
(3,  3,  'Delhi',     'Jaipur',    'Train',  'Superfast express train, approx 5 hours'),
(4,  4,  'Jaipur',    'Ranthambore','Car',   'Private cab transfer, approx 3.5 hours'),
(5,  5,  'Mumbai',    'Maldives',  'Flight', 'Direct international flight, approx 4 hours'),
(6,  6,  'Haridwar',  'Yamunotri', 'Bus',    'Shared tempo traveller with mountain halts'),
(7,  7,  'Chandigarh','Shimla',    'Car',    'Scenic drive through pine forests, approx 3 hours'),
(8,  8,  'Mumbai',    'Dubai',     'Flight', 'Direct international flight, approx 3.5 hours'),
(9,  9,  'Kochi',     'Alleppey',  'Car',    'Private transfer, approx 1.5 hours'),
(10, 10, 'Delhi',     'Zurich',    'Flight', 'Direct international flight, approx 8 hours');

-- ---------------------------------------------------------------------
-- TOUR_IMAGES (10 rows)
-- ---------------------------------------------------------------------
INSERT INTO tour_images (image_id, tour_id, source, image_title, is_primary, uploaded_date) VALUES
(1,  1,  '/images/tours/manali_1.jpg',      'Manali Valley View',       1, '2026-06-01 10:00:00'),
(2,  2,  '/images/tours/goa_1.jpg',         'Calangute Beach',          1, '2026-06-02 10:00:00'),
(3,  3,  '/images/tours/jaipur_1.jpg',      'Amber Fort',               1, '2026-06-03 10:00:00'),
(4,  4,  '/images/tours/ranthambore_1.jpg', 'Tiger in the Wild',        1, '2026-06-04 10:00:00'),
(5,  5,  '/images/tours/maldives_1.jpg',    'Overwater Villa',          1, '2026-06-05 10:00:00'),
(6,  6,  '/images/tours/chardham_1.jpg',    'Yamunotri Shrine',         1, '2026-06-06 10:00:00'),
(7,  7,  '/images/tours/shimla_1.jpg',      'Mall Road Shimla',         1, '2026-06-07 10:00:00'),
(8,  8,  '/images/tours/dubai_1.jpg',       'Burj Khalifa Skyline',     1, '2026-06-08 10:00:00'),
(9,  9,  '/images/tours/kerala_1.jpg',      'Backwater Houseboat',      1, '2026-06-09 10:00:00'),
(10, 10, '/images/tours/swiss_1.jpg',       'Swiss Alps Panorama',      1, '2026-06-10 10:00:00');

-- ---------------------------------------------------------------------
-- COST (10 rows)
-- ---------------------------------------------------------------------
INSERT INTO cost (cost_id, tour_id, adult_price, single_person_price, extra_person_price, child_with_bed_price, child_without_bed_price, valid_from, valid_to, is_active) VALUES
(1,  1,  24999.00, 29999.00, 15999.00, 18999.00, 12999.00, '2026-01-01', '2026-12-31', 1),
(2,  2,  18999.00, 22999.00, 11999.00, 14999.00, 9999.00,  '2026-01-01', '2026-12-31', 1),
(3,  3,  32999.00, 38999.00, 19999.00, 22999.00, 15999.00, '2026-01-01', '2026-12-31', 1),
(4,  4,  21999.00, 25999.00, 13999.00, 16999.00, 10999.00, '2026-01-01', '2026-12-31', 1),
(5,  5,  89999.00, 99999.00, 55999.00, 60999.00, 40999.00, '2026-01-01', '2026-12-31', 1),
(6,  6,  27999.00, 31999.00, 16999.00, 19999.00, 13999.00, '2026-01-01', '2026-12-31', 1),
(7,  7,  22999.00, 26999.00, 14999.00, 17999.00, 11999.00, '2026-01-01', '2026-12-31', 1),
(8,  8,  45999.00, 52999.00, 28999.00, 32999.00, 21999.00, '2026-01-01', '2026-12-31', 1),
(9,  9,  26999.00, 30999.00, 15999.00, 18999.00, 12999.00, '2026-01-01', '2026-12-31', 1),
(10, 10, 159999.00,179999.00,99999.00, 109999.00,74999.00, '2026-01-01', '2026-12-31', 1);

-- ---------------------------------------------------------------------
-- BOOKING (10 rows) - user_id references user, tour_id references tour
-- ---------------------------------------------------------------------
INSERT INTO booking (booking_id, user_id, tour_id, booking_date, total_amount, booking_status) VALUES
(1,  2, 1,  '2026-07-01', 49998.00,  'CONFIRMED'),
(2,  3, 2,  '2026-07-03', 37998.00,  'CONFIRMED'),
(3,  4, 3,  '2026-07-05', 32999.00,  'CONFIRMED'),
(4,  5, 4,  '2026-07-06', 21999.00,  'PENDING'),
(5,  2, 5,  '2026-07-10', 179998.00, 'CONFIRMED'),
(6,  3, 6,  '2026-07-12', 27999.00,  'CANCELLED'),
(7,  4, 7,  '2026-07-15', 45998.00,  'CONFIRMED'),
(8,  5, 8,  '2026-07-18', 45999.00,  'PENDING'),
(9,  2, 9,  '2026-07-20', 53998.00,  'CONFIRMED'),
(10, 3, 10, '2026-07-22', 159999.00, 'CANCELLED');

-- ---------------------------------------------------------------------
-- REVIEW (10 rows)
-- ---------------------------------------------------------------------
INSERT INTO review (review_id, booking_id, customer_id, tour_id, rating, review_title, review_description, review_date, verification_status) VALUES
(1,  1, 2, 1,  5, 'Amazing trek!',         'Well organized trip with great guides.',        '2026-09-15', 'VERIFIED'),
(2,  2, 3, 2,  4, 'Relaxing beach trip',   'Loved the resort, food could be better.',        '2026-08-20', 'VERIFIED'),
(3,  3, 4, 3,  5, 'Rich cultural experience','Forts and palaces were breathtaking.',         '2026-10-05', 'VERIFIED'),
(4,  4, 5, 4,  4, 'Great safari',          'Spotted two tigers, unforgettable.',             '2026-09-10', 'PENDING'),
(5,  5, 2, 5,  5, 'Perfect honeymoon',     'Overwater villa was dreamy and private.',        '2026-11-25', 'VERIFIED'),
(6,  7, 4, 7,  4, 'Scenic hill escape',    'Weather was cold but views were stunning.',      '2026-09-30', 'VERIFIED'),
(7,  8, 5, 8,  3, 'Decent city tour',      'Itinerary felt rushed on the last day.',         '2026-10-15', 'PENDING'),
(8,  9, 2, 9,  5, 'Serene backwaters',     'Houseboat stay was peaceful and well maintained.', '2026-09-22', 'VERIFIED'),
(9,  1, 2, 1,  4, 'Would recommend',       'Good value for the price paid.',                 '2026-09-16', 'VERIFIED'),
(10, 3, 4, 3,  5, 'Loved every moment',    'Guide was very knowledgeable about history.',    '2026-10-06', 'VERIFIED');

-- ---------------------------------------------------------------------
-- PAYMENT (10 rows)
-- ---------------------------------------------------------------------
INSERT INTO payment (payment_id, booking_id, amount, payment_date, payment_status, payment_method) VALUES
(1,  1,  49998.00,  '2026-07-01', 'SUCCESS', 'CREDIT_CARD'),
(2,  2,  37998.00,  '2026-07-03', 'SUCCESS', 'UPI'),
(3,  3,  32999.00,  '2026-07-05', 'SUCCESS', 'DEBIT_CARD'),
(4,  4,  21999.00,  '2026-07-06', 'PENDING', 'NET_BANKING'),
(5,  5,  179998.00, '2026-07-10', 'SUCCESS', 'CREDIT_CARD'),
(6,  6,  27999.00,  '2026-07-12', 'REFUNDED', 'UPI'),
(7,  7,  45998.00,  '2026-07-15', 'SUCCESS', 'CREDIT_CARD'),
(8,  8,  45999.00,  '2026-07-18', 'PENDING', 'UPI'),
(9,  9,  53998.00,  '2026-07-20', 'SUCCESS', 'DEBIT_CARD'),
(10, 10, 159999.00, '2026-07-22', 'REFUNDED', 'NET_BANKING');

-- ---------------------------------------------------------------------
-- PASSENGER_DETAILS (10 rows)
-- ---------------------------------------------------------------------
INSERT INTO passenger_details (pax_id, booking_id, full_name, email, gender, birth_date, age, passport_number) VALUES
(1,  1,  'Priya Sharma',   'priya.sharma@example.com',  'Female', '1994-05-12', 32, 100001),
(2,  2,  'Rahul Mehta',    'rahul.mehta@example.com',   'Male',   '1990-03-22', 36, 100002),
(3,  3,  'Anita Kapoor',   'anita.kapoor@example.com',  'Female', '1988-11-02', 37, 100003),
(4,  4,  'Vikram Rao',     'vikram.rao@example.com',    'Male',   '1992-07-19', 33, 100004),
(5,  5,  'Priya Sharma',   'priya.sharma@example.com',  'Female', '1994-05-12', 32, 100005),
(6,  6,  'Rahul Mehta',    'rahul.mehta@example.com',   'Male',   '1990-03-22', 36, 100006),
(7,  7,  'Anita Kapoor',   'anita.kapoor@example.com',  'Female', '1988-11-02', 37, 100007),
(8,  8,  'Vikram Rao',     'vikram.rao@example.com',    'Male',   '1992-07-19', 33, 100008),
(9,  9,  'Priya Sharma',   'priya.sharma@example.com',  'Female', '1994-05-12', 32, 100009),
(10, 10, 'Rahul Mehta',    'rahul.mehta@example.com',   'Male',   '1990-03-22', 36, 100010);

-- ---------------------------------------------------------------------
-- CANCELLATION (10 rows) - references booking
-- ---------------------------------------------------------------------
INSERT INTO cancellation (cancellation_id, booking_id, cancellation_date, reason, refund_amount, refund_status, remarks) VALUES
(1,  6,  '2026-07-14 09:00:00', 'Change of travel plans',        25199.00,  'PROCESSED', 'Refunded after deducting cancellation fee'),
(2,  10, '2026-07-25 16:30:00', 'Medical emergency',              151999.05, 'PROCESSED', 'Partial refund as per policy'),
(3,  4,  '2026-07-20 11:15:00', 'Found a cheaper alternative',    0.00,      'REJECTED',  'Cancellation window had already closed'),
(4,  8,  '2026-07-30 08:00:00', 'Visa rejection',                 41399.00,  'PROCESSED', 'Refund processed within 5 business days'),
(5,  2,  '2026-08-01 12:00:00', 'Personal reasons',                34198.00,  'PROCESSED', 'Standard cancellation policy applied'),
(6,  7,  '2026-08-05 10:45:00', 'Weather concerns',                41398.00,  'PENDING',   'Awaiting approval from finance team'),
(7,  1,  '2026-08-10 09:30:00', 'Duplicate booking',               49998.00,  'PROCESSED', 'Full refund - system error confirmed'),
(8,  3,  '2026-08-12 14:20:00', 'Illness in family',               29699.00,  'PROCESSED', 'Refund minus processing charge'),
(9,  5,  '2026-08-15 17:00:00', 'Flight cancelled by airline',     179998.00, 'PROCESSED', 'Full refund issued'),
(10, 9,  '2026-08-18 13:10:00', 'Better package found elsewhere',  0.00,      'REJECTED',  'Non-refundable promotional booking');

-- ---------------------------------------------------------------------
-- NOTIFICATION (10 rows) - user_id references user
-- ---------------------------------------------------------------------
INSERT INTO notification (notification_id, user_id, title, message, is_read, created_date) VALUES
(1,  2, 'Booking Confirmed',    'Your booking for Himalayan Trek Adventure is confirmed.', 1, '2026-07-01 09:05:00'),
(2,  3, 'Booking Confirmed',    'Your booking for Goa Beach Bliss is confirmed.',           1, '2026-07-03 10:10:00'),
(3,  4, 'Booking Confirmed',    'Your booking for Rajasthan Heritage Tour is confirmed.',    0, '2026-07-05 11:00:00'),
(4,  5, 'Payment Pending',      'Please complete payment for your Ranthambore Safari.',      0, '2026-07-06 12:00:00'),
(5,  2, 'Booking Confirmed',    'Your Maldives Honeymoon Special is confirmed.',              1, '2026-07-10 08:30:00'),
(6,  3, 'Booking Cancelled',    'Your Char Dham Yatra booking has been cancelled.',           1, '2026-07-12 15:00:00'),
(7,  4, 'Booking Confirmed',    'Your Shimla Manali Hill Escape is confirmed.',               0, '2026-07-15 09:45:00'),
(8,  5, 'Payment Pending',      'Please complete payment for your Dubai City Explorer.',      0, '2026-07-18 10:20:00'),
(9,  2, 'Booking Confirmed',    'Your Kerala Backwater Cruise is confirmed.',                 1, '2026-07-20 14:00:00'),
(10, 3, 'Booking Cancelled',    'Your Swiss Alps Luxury Tour booking has been cancelled.',    0, '2026-07-22 16:40:00');

-- ---------------------------------------------------------------------
-- ADS (10 rows)
-- ---------------------------------------------------------------------
INSERT INTO ads (ads_id, title, image_path, link_url, start_date, end_date, display_order, active) VALUES
(1,  'Summer Beach Sale',      '/images/ads/beach_sale.jpg',    'https://example.com/promo/beach-sale',    '2026-06-01', '2026-08-31', 1, 1),
(2,  'Honeymoon Specials',     '/images/ads/honeymoon.jpg',     'https://example.com/promo/honeymoon',      '2026-01-01', '2026-12-31', 2, 1),
(3,  'Wildlife Safari Deals',  '/images/ads/safari.jpg',        'https://example.com/promo/safari',         '2026-05-01', '2026-09-30', 3, 1),
(4,  'Luxury Europe Tours',    '/images/ads/europe_luxury.jpg', 'https://example.com/promo/europe',         '2026-03-01', '2026-12-31', 4, 1),
(5,  'Pilgrimage Packages',    '/images/ads/pilgrimage.jpg',    'https://example.com/promo/pilgrimage',     '2026-01-01', '2026-12-31', 5, 1),
(6,  'Hill Station Getaways',  '/images/ads/hillstation.jpg',   'https://example.com/promo/hillstation',    '2026-04-01', '2026-10-31', 6, 1),
(7,  'Dubai Weekend Trips',    '/images/ads/dubai.jpg',         'https://example.com/promo/dubai',          '2026-02-01', '2026-12-31', 7, 1),
(8,  'Kerala Cruise Offers',   '/images/ads/kerala.jpg',        'https://example.com/promo/kerala',         '2026-06-01', '2026-11-30', 8, 1),
(9,  'Heritage India Tours',   '/images/ads/heritage.jpg',      'https://example.com/promo/heritage',       '2026-01-01', '2026-12-31', 9, 0),
(10, 'Year-End Mega Sale',     '/images/ads/yearend.jpg',       'https://example.com/promo/yearend',        '2026-11-01', '2026-12-31', 10,1);