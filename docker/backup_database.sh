#!/bin/bash

if [ -z "$1" ]; then
  echo "Usage: backup_database.sh <docker-container-id>"
  exit 1
fi

SRC=/usr/local/tomcat/target/jpaserver_derby_files
BKDIR=/tmp/derby_backup
BKFILE=/tmp/derby_backup.tar.gz

# create the derby backup
docker exec -t $1 bash -c "rm -rf $BKDIR \
&& rm -f $BKFILE \
&& cp -r $SRC $BKDIR \
&& tar czf $BKFILE -C $BKDIR ."
#&& rm $BKDIR/*.lck \

# copy the backup from the container
docker cp $1:$BKFILE ./

