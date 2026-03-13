-- mechanic shop - sql reference
-- all raw sql used in MechanicShop.java / MechanicShopGUI.java


-- function 1: add customer

-- get next available id
SELECT MAX(id) FROM Customer;

-- insert the new customer
INSERT INTO Customer (id, fname, lname, phone, address)
VALUES (<id>, '<fname>', '<lname>', '<phone>', '<address>');


-- function 2: add mechanic

-- get next available id
SELECT MAX(id) FROM Mechanic;

-- insert the new mechanic
INSERT INTO Mechanic (id, fname, lname, experience, specialty)
VALUES (<id>, '<fname>', '<lname>', <experience>, '<specialty>');


-- function 3: add car

-- check if vin already exists (must return 0 to proceed)
SELECT COUNT(*) FROM Car WHERE vin = '<vin>';

-- insert the new car
INSERT INTO Car (vin, make, model, year)
VALUES ('<vin>', '<make>', '<model>', <year>);


-- function 4: insert service request
-- multi-step, runs these queries in order

-- step 1: search customer by last name
SELECT id, fname, lname, phone
FROM Customer
WHERE lname = '<lname>';

-- step 2: get that customer's cars
SELECT c.vin, c.make, c.model, c.year
FROM Car c, Owns o
WHERE o.customer_id = <customer_id>
AND c.vin = o.car_vin;

-- step 2b (if adding a new car): get next ownership id and link car
SELECT MAX(ownership_id) FROM Owns;

INSERT INTO Owns (ownership_id, customer_id, car_vin)
VALUES (<ownership_id>, <customer_id>, '<car_vin>');

-- step 3: get next request id
SELECT MAX(rid) FROM Service_Request;

-- step 3: insert the service request (date = today)
INSERT INTO Service_Request (rid, customer_id, car_vin, date, odometer, complain)
VALUES (<rid>, <customer_id>, '<car_vin>', '<YYYY-MM-DD>', <odometer>, '<complaint>');


-- function 5: close service request
-- multi-step, runs these queries in order

-- check that the service request exists
SELECT * FROM Service_Request WHERE rid = <rid>;

-- check it is not already closed (must return 0 to proceed)
SELECT * FROM Closed_Request WHERE rid = <rid>;

-- check that the mechanic exists
SELECT * FROM Mechanic WHERE id = <mid>;

-- get the original request date (close date must be >= this)
SELECT date FROM Service_Request WHERE rid = <rid>;

-- get next work order id
SELECT MAX(wid) FROM Closed_Request;

-- insert the closed request (date = today)
INSERT INTO Closed_Request (wid, rid, mid, date, comment, bill)
VALUES (<wid>, <rid>, <mid>, '<YYYY-MM-DD>', '<comment>', <bill>);


-- query 6: closed requests with bill under $100

SELECT cr.date, cr.comment, cr.bill
FROM Closed_Request cr
WHERE cr.bill < 100;


-- query 7: customers who own more than 20 cars

SELECT c.fname, c.lname
FROM Customer c, Owns o
WHERE c.id = o.customer_id
GROUP BY c.id, c.fname, c.lname
HAVING COUNT(*) > 20;


-- query 8: pre-1995 cars with less than 50,000 miles

SELECT DISTINCT c.make, c.model, c.year
FROM Car c, Service_Request sr
WHERE c.vin = sr.car_vin
AND c.year < 1995
AND sr.odometer < 50000;


-- query 9: top k cars with the most open (pending) requests
-- replace <k> with the number the user enters

SELECT c.make, c.model, COUNT(*) AS cnt
FROM Car c, Service_Request sr
WHERE c.vin = sr.car_vin
AND sr.rid NOT IN (SELECT rid FROM Closed_Request)
GROUP BY c.vin, c.make, c.model
ORDER BY cnt DESC
LIMIT <k>;


-- query 10: total amount each customer has spent (highest first)

SELECT c.fname, c.lname, SUM(cr.bill) AS total
FROM Customer c, Service_Request sr, Closed_Request cr
WHERE c.id = sr.customer_id
AND sr.rid = cr.rid
GROUP BY c.id, c.fname, c.lname
ORDER BY total DESC;
