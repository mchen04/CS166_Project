#!/bin/bash
# start.sh - One-click setup: creates DB, loads data, compiles Java, launches GUI
# Usage: ./start.sh [dbname] [port] [user]

set -e

DB="${1:-mechanic_shop}"
PORT="${2:-5432}"
USER="${3:-$(whoami)}"

DIR="$(cd "$(dirname "$0")" && pwd)"
JAVA_DIR="$DIR/java"

if [ -d "/opt/homebrew/opt/openjdk" ]; then
    export JAVA_HOME=/opt/homebrew/opt/openjdk
    export PATH="$JAVA_HOME/bin:$PATH"
fi

# --- Check prerequisites ---
echo "=== Checking prerequisites ==="

if ! command -v psql &>/dev/null; then
    echo "ERROR: PostgreSQL (psql) not found. Install with: brew install postgresql"
    exit 1
fi

if ! command -v javac &>/dev/null; then
    echo "ERROR: Java compiler (javac) not found. Install with: brew install openjdk"
    exit 1
fi

# Check if PostgreSQL is running
if ! pg_isready -p "$PORT" -q 2>/dev/null; then
    echo "PostgreSQL is not running on port $PORT. Attempting to start..."
    brew services start postgresql@14 2>/dev/null || brew services start postgresql 2>/dev/null || {
        echo "ERROR: Could not start PostgreSQL. Start it manually and retry."
        exit 1
    }
    # Wait for it to be ready
    for i in {1..10}; do
        if pg_isready -p "$PORT" -q 2>/dev/null; then
            break
        fi
        sleep 1
    done
    if ! pg_isready -p "$PORT" -q 2>/dev/null; then
        echo "ERROR: PostgreSQL did not start in time."
        exit 1
    fi
    echo "PostgreSQL started."
fi

echo "All prerequisites OK."

# --- Set up database ---
echo ""
echo "=== Setting up database: $DB ==="

dropdb -p "$PORT" -U "$USER" --if-exists "$DB" --maintenance-db=postgres
createdb -p "$PORT" -U "$USER" "$DB" --maintenance-db=postgres
echo "Database created."

DATA_DIR="$DIR/data"

psql -p "$PORT" -U "$USER" -d "$DB" \
    -v customer_path="'$DATA_DIR/customer.csv'" \
    -v mechanic_path="'$DATA_DIR/mechanic.csv'" \
    -v car_path="'$DATA_DIR/car.csv'" \
    -v owns_path="'$DATA_DIR/owns.csv'" \
    -v sr_path="'$DATA_DIR/service_request.csv'" \
    -v cr_path="'$DATA_DIR/closed_request.csv'" \
    -f "$DIR/sql/create.sql"

echo ""
echo "Schema and data loaded. Row counts:"
psql -p "$PORT" -U "$USER" -d "$DB" -c "
SELECT 'Customer' AS table_name, COUNT(*) AS rows FROM Customer
UNION ALL SELECT 'Mechanic', COUNT(*) FROM Mechanic
UNION ALL SELECT 'Car', COUNT(*) FROM Car
UNION ALL SELECT 'Owns', COUNT(*) FROM Owns
UNION ALL SELECT 'Service_Request', COUNT(*) FROM Service_Request
UNION ALL SELECT 'Closed_Request', COUNT(*) FROM Closed_Request
ORDER BY table_name;"

# --- Compile Java ---
echo ""
echo "=== Compiling Java ==="
cd "$JAVA_DIR"
rm -rf bin/*.class
javac -cp "lib/*" src/MechanicShop.java src/MechanicShopGUI.java src/TestGUI.java -d bin/
echo "Compiled successfully."

# --- Launch GUI ---
echo ""
echo "=== Launching Mechanic Shop GUI ==="
echo "    DB=$DB  PORT=$PORT  USER=$USER"
echo ""
java -cp "lib/*:bin/" MechanicShopGUI "$DB" "$PORT" "$USER"
