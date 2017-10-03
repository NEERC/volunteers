#!/bin/sh

SRC=build/libs/volunteers.war

USER=volunteers
HOST=neerc.ifmo.ru
DEST=volunteers/ci

CMD=volunteers/ci/update.sh

echo "Uploading artifacts..."
scp $SRC $USER@$HOST:$DEST

echo "Updating server..."
ssh $USER@$HOST $CMD
