# Mechanic Shop — CS166 Project

A database system for tracking customers, cars, mechanics, service requests, and billing for a mechanic shop. Built for CS 166 (Database Management Systems) at UC Riverside.

## Phases

### [Phase 1](phase1/) — ER Design & Relational Schema

- ER diagram modeling all entities and relationships
- PostgreSQL schema with 6 tables, foreign keys, and constraints

### [Phase 2](phase2/) — Implementation

- Java JDBC client with 5 functions and 5 queries
- Swing GUI with tabbed interface (extra credit)
- Dummy data (500 customers, 250 mechanics, 800 cars, 1500 service requests)
- One-click deploy to UCR server: `./phase2/deploy_ucr.sh <netid>`

- [Phase 2 README](phase2/README.md)

## Quick Start

```bash
# UCR server (CLI)
cd phase2
scripts/./deploy_ucr.sh <netid>

# Local (GUI)
cd phase2
scripts/./start.sh
```

## Schema

```
Customer(id, fname, lname, phone, address)
Mechanic(id, fname, lname, experience, specialty)
Car(vin, make, model, year)
Owns(ownership_id, customer_id → Customer, car_vin → Car)
Service_Request(rid, customer_id → Customer, car_vin → Car, date, odometer, complain)
Closed_Request(wid, rid → Service_Request, mid → Mechanic, date, comment, bill)
```
