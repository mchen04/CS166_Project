-- cs166 project phase 2 - mechanic shop schema
-- builds off phase 1, adds specialty to mechanic + indexes + data loading

-- drop in reverse dependency order so foreign keys dont complain
DROP TABLE IF EXISTS Closed_Request;
DROP TABLE IF EXISTS Service_Request;
DROP TABLE IF EXISTS Owns;
DROP TABLE IF EXISTS Car;
DROP TABLE IF EXISTS Mechanic;
DROP TABLE IF EXISTS Customer;

-- entities

CREATE TABLE Customer (
    id       INTEGER NOT NULL,
    fname    VARCHAR(32) NOT NULL,
    lname    VARCHAR(32) NOT NULL,
    phone    CHAR(13) NOT NULL,
    address  VARCHAR(256) NOT NULL,
    PRIMARY KEY (id)
);

-- id doubles as the employee id from the spec
-- added specialty for phase 2 (AddMechanic requires it as input)
CREATE TABLE Mechanic (
    id          INTEGER NOT NULL,
    fname       VARCHAR(32) NOT NULL,
    lname       VARCHAR(32) NOT NULL,
    experience  INTEGER NOT NULL,
    specialty   VARCHAR(64) NOT NULL,
    PRIMARY KEY (id),
    CHECK (experience >= 0 AND experience <= 100)
);

-- vin is the natural key here
CREATE TABLE Car (
    vin    VARCHAR(16) NOT NULL,
    make   VARCHAR(32) NOT NULL,
    model  VARCHAR(32) NOT NULL,
    year   INTEGER NOT NULL,
    PRIMARY KEY (vin),
    CHECK (year >= 1970 AND year <= 2026)
);

-- relationships

-- separate table because the spec says "ownership relation" in query 7
-- unique on car_vin because each car has exactly one owner
CREATE TABLE Owns (
    ownership_id  INTEGER NOT NULL,
    customer_id   INTEGER NOT NULL,
    car_vin       VARCHAR(16) NOT NULL,
    PRIMARY KEY (ownership_id),
    FOREIGN KEY (customer_id) REFERENCES Customer(id),
    FOREIGN KEY (car_vin) REFERENCES Car(vin),
    UNIQUE (car_vin)
);

-- customer brings a car in for service
CREATE TABLE Service_Request (
    rid          INTEGER NOT NULL,
    customer_id  INTEGER NOT NULL,
    car_vin      VARCHAR(16) NOT NULL,
    date         DATE NOT NULL,
    odometer     INTEGER NOT NULL,
    complain     TEXT NOT NULL,
    PRIMARY KEY (rid),
    FOREIGN KEY (customer_id) REFERENCES Customer(id),
    FOREIGN KEY (car_vin) REFERENCES Car(vin),
    CHECK (odometer > 0)
);

-- mechanic closes out a service request when the car is fixed
-- unique on rid so a request can only be closed once
CREATE TABLE Closed_Request (
    wid      INTEGER NOT NULL,
    rid      INTEGER NOT NULL,
    mid      INTEGER NOT NULL,
    date     DATE NOT NULL,
    comment  TEXT,
    bill     NUMERIC(10,2) NOT NULL,
    PRIMARY KEY (wid),
    FOREIGN KEY (rid) REFERENCES Service_Request(rid),
    FOREIGN KEY (mid) REFERENCES Mechanic(id),
    UNIQUE (rid),
    CHECK (bill >= 0)
);

-- indexes on foreign keys and commonly queried columns
-- pk indexes are auto-created by postgres, so these target join/where columns
CREATE INDEX idx_customer_lname ON Customer(lname);
CREATE INDEX idx_owns_customer ON Owns(customer_id);
CREATE INDEX idx_owns_car ON Owns(car_vin);
CREATE INDEX idx_sr_customer ON Service_Request(customer_id);
CREATE INDEX idx_sr_car ON Service_Request(car_vin);
CREATE INDEX idx_cr_rid ON Closed_Request(rid);
CREATE INDEX idx_cr_mid ON Closed_Request(mid);
CREATE INDEX idx_car_year ON Car(year);
CREATE INDEX idx_cr_bill ON Closed_Request(bill);

-- load csv data
-- using relative paths from the project root
COPY Customer FROM :customer_path DELIMITER ',' CSV HEADER;
COPY Mechanic FROM :mechanic_path DELIMITER ',' CSV HEADER;
COPY Car FROM :car_path DELIMITER ',' CSV HEADER;
COPY Owns FROM :owns_path DELIMITER ',' CSV HEADER;
COPY Service_Request FROM :sr_path DELIMITER ',' CSV HEADER;
COPY Closed_Request FROM :cr_path DELIMITER ',' CSV HEADER;
