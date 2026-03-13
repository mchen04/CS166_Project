# Phase 2 — Implementation

## UCR Server

```bash
./scripts/deploy_ucr.sh <netid>
```

Uploads files, starts PostgreSQL, loads schema + data, compiles, and runs the CLI. Stops the database automatically when you exit. One password prompt.

Manual steps if needed:

```bash
ssh <netid>@cs166.cs.ucr.edu
cd ~/phase2
source ucr/startPostgreSQL.sh
source ucr/createPostgreDB.sh
source ucr/compile_ucr.sh
# when done:
source ucr/stopPostgreDB.sh
```

## Local (macOS)

```bash
./scripts/start.sh
```

Creates database, compiles, and launches the Swing GUI. Or run separately:

```bash
cd java && ./compile.sh
./run_gui.sh mechanic_shop 5432 $USER    # GUI
./run.sh mechanic_shop 5432 $USER        # CLI
```

## Structure

```
scripts/start.sh           one-click local setup + GUI
scripts/deploy_ucr.sh      one-click UCR deploy + run
generate_data.py           dummy data generator
ucr/                       UCR server helper scripts
sql/create.sql             schema + indexes (local)
sql/create_ucr.sql         schema + indexes (UCR)
data/*.csv                 dummy data (500 customers, 250 mechanics, 800 cars, etc.)
java/src/MechanicShop.java CLI client
java/src/MechanicShopGUI.java  Swing GUI (extra credit)
java/lib/                  JDBC driver
```

## Functions (1–5)

| # | Function | Description |
|---|----------|-------------|
| 1 | Add Customer | Validated insert (name, phone, address) |
| 2 | Add Mechanic | Insert with specialty and experience |
| 3 | Add Car | Insert with VIN uniqueness check |
| 4 | Service Request | Search customer → select car → enter complaint |
| 5 | Close Request | Verify open, verify mechanic, check dates, close with bill |

## Queries (6–10)

| # | Query | Description |
|---|-------|-------------|
| 6 | Bills < $100 | Closed requests with bill under $100 |
| 7 | > 20 Cars | Customers who own more than 20 cars |
| 8 | Pre-1995 | Cars before 1995 with < 50k miles |
| 9 | Top K | K cars with most pending service requests |
| 10 | Total Bills | Customer totals, descending |

## GUI (Extra Credit)

Tabbed Swing interface with PreparedStatements, sortable tables, multi-step service request workflow, inline add dialogs, SQL log panel, and Nimbus look-and-feel.