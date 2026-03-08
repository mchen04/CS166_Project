#!/bin/bash
# start postgres on the ucr cs166 server
# safe to run multiple times — skips init if already exists, skips start if already running

# init only if data dir doesn't exist yet
if [ ! -f "$PGDATA/PG_VERSION" ]; then
    initdb
    cp -f /usr/csshare/src/pg_hba.conf.cs166 ${PGDATA}/pg_hba.conf
else
    echo "database already initialized, skipping initdb."
fi

# start only if not already running
if pg_ctl -o "-c unix_socket_directories=${PG_SOCKETS} -h 127.0.0.1" status 2>&1 | grep -q "server is running"; then
    echo "server already running."
else
    pg_ctl -o "-c unix_socket_directories=${PG_SOCKETS} -h 127.0.0.1" -l ${PG_LOGFILE} start
fi

pg_ctl -o "-c unix_socket_directories=${PG_SOCKETS} -h 127.0.0.1" status
