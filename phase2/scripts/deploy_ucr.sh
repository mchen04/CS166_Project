#!/bin/bash
# deploy phase 2 to the ucr cs166 server and run the CLI
# run this LOCALLY (not while ssh'd in)
# usage: ./deploy_ucr.sh <netid>
#   e.g. ./deploy_ucr.sh mchen356

if [ -z "$1" ]; then
    echo "usage: ./deploy_ucr.sh <netid>"
    echo "  e.g. ./deploy_ucr.sh mchen356"
    exit 1
fi

NETID="$1"
SERVER="cs166.cs.ucr.edu"
DIR="$(cd "$(dirname "$0")/.." && pwd)"

echo "=== deploying phase2 to $NETID@$SERVER ==="
echo ""

# use a control socket so we only authenticate once
SOCK="/tmp/ssh-cs166-$$"
ssh -fNM -S "$SOCK" "$NETID@$SERVER"
trap "ssh -S '$SOCK' -O exit '$NETID@$SERVER' 2>/dev/null" EXIT

# clear old copy and upload fresh
ssh -S "$SOCK" "$NETID@$SERVER" "rm -rf ~/phase2"
scp -o "ControlPath=$SOCK" -r "$DIR" "$NETID@$SERVER:~/phase2"

echo ""
echo "=== uploaded! setting up database and compiling... ==="
echo ""

# run setup with login shell so PGDATA, PGPORT, PG_SOCKETS etc. are set
ssh -S "$SOCK" -t "$NETID@$SERVER" "bash --login -c '
    cd ~/phase2 &&
    source ucr/startPostgreSQL.sh &&
    source ucr/createPostgreDB.sh &&
    source ucr/compile_ucr.sh;
    echo;
    echo \"=== stopping database... ===\";
    source ucr/stopPostgreDB.sh
'"
