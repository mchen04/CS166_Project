#!/bin/bash
# create the mechanic shop database on ucr cs166 server
DB_NAME=$USER"_project_DB"

echo "creating db named ... $DB_NAME"
createdb -h 127.0.0.1 "$DB_NAME" 2>/dev/null || echo "database already exists, reusing."

# copy csv data files into $PGDATA so COPY can find them
cp -a data/*.csv $PGDATA/

# run schema + load data
psql -h 127.0.0.1 "$DB_NAME" < sql/create_ucr.sql

echo ""
echo "=== database $DB_NAME is ready ==="
echo "row counts:"
echo "SELECT 'Customer' as tbl, COUNT(*) as rows FROM Customer UNION ALL SELECT 'Mechanic', COUNT(*) FROM Mechanic UNION ALL SELECT 'Car', COUNT(*) FROM Car UNION ALL SELECT 'Owns', COUNT(*) FROM Owns UNION ALL SELECT 'Service_Request', COUNT(*) FROM Service_Request UNION ALL SELECT 'Closed_Request', COUNT(*) FROM Closed_Request ORDER BY tbl;" | psql -h 127.0.0.1 "$DB_NAME"
