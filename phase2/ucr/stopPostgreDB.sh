#!/bin/bash
# stop postgres on the ucr cs166 server
pg_ctl -o "-c unix_socket_directories=${PG_SOCKETS} -h 127.0.0.1" stop
pg_ctl -o "-c unix_socket_directories=${PG_SOCKETS} -h 127.0.0.1" status
