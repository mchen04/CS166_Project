# Phase 1 — ER Design & Relational Schema

## Deliverables

- `MechanicShop_Phase1.erdplus` — ER diagram (open with [ERDPlus](https://erdplus.com/))
- `create_tables.sql` — PostgreSQL schema (6 tables with keys, constraints, and foreign keys)

## Tables

| Table | Primary Key | Description |
|---|---|---|
| Customer | id | First/last name, phone, address |
| Mechanic | id | First/last name, experience |
| Car | vin | Make, model, year |
| Owns | ownership_id | Links customer to car (unique on car_vin) |
| Service_Request | rid | Customer brings car in with complaint + odometer |
| Closed_Request | wid | Mechanic closes request with comment + bill |

## Running the Schema

```bash
psql -d <dbname> -f create_tables.sql
```
