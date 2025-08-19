-- Sample Indian Dealer Data
INSERT INTO dealers (id, name, address, phone, email, gst_number) VALUES 
(1, 'Mahindra Auto Sales Pvt Ltd', '123 MG Road, Indore, Madhya Pradesh 452001', '7311234567', 'sales@mahindraauto.com', '23AABCM1234A1Z5'),
(2, 'Tata Motors Showroom Ltd', '456 Anna Salai, Chennai, Tamil Nadu 600002', '4423456789', 'info@tatamotors.com', '33AABCT5678B2Z6'),
(3, 'Maruti Suzuki Dealership Pvt Ltd', '789 Brigade Road, Bangalore, Karnataka 560001', '8034567890', 'contact@marutisuzuki.com', '29AABCM9012C3Z7')
ON CONFLICT (id) DO NOTHING;

-- Sample Indian Vehicle Data
INSERT INTO vehicles (id, make, model, vehicle_year, price, registration_number, dealer_id) VALUES 
(1, 'Mahindra', 'XUV700', 2024, 1500000.00, 'MP-01-AB-1234', 1),
(2, 'Tata', 'Nexon EV', 2024, 1800000.00, 'TN-02-CD-5678', 2),
(3, 'Maruti Suzuki', 'Swift', 2024, 800000.00, 'KA-03-EF-9012', 3),
(4, 'Tata', 'Harrier', 2024, 2200000.00, 'TN-04-GH-3456', 2),
(5, 'Maruti Suzuki', 'Brezza', 2024, 1200000.00, 'KA-05-IJ-7890', 3),
(6, 'Mahindra', 'Thar', 2024, 1800000.00, 'MP-06-KL-2345', 1)
ON CONFLICT (id) DO NOTHING;
