#!/bin/bash
PATH=/usr/local/bin:/usr/local/sbin:/usr/bin:/usr/sbin:/bin:/sbin
cd /backup/app/udpq
nohup ./udpq.sh|logger -t udpq 2>&1 &

