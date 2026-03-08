-- cs166 project phase 1 - mechanic shop schema

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
CREATE TABLE Mechanic (
    id          INTEGER NOT NULL,
    fname       VARCHAR(32) NOT NULL,
    lname       VARCHAR(32) NOT NULL,
    experience  INTEGER NOT NULL,
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
