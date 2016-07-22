#!/bin/bash

FILEPID=$(cat aw.pid)
PSPID=$(pgrep -u root [a]rpwatch)
echo "PID is: (current)="$PSPID ", (saved)="$FILEPID
echo "Killing arpwatch ..."
#sleep 2										# wait a sec (for monitoring purposes)
kill $PSPID # 2> /dev/null	# hiding garbage, in case of invalid PID
echo "Arpwatch running processes: " $(pgrep -u root arpwatch) "."
#sleep 2

