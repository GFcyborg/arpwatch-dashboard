#!/bin/bash

# ls . nonexisting >stdout.dump 2>stderr.dump
# >&2 echo	" Test msg to stderr"
# echo		" Test msg merging stderr to stdout" 2>&1
echo " ." # just in case we are removing the 1st line of output (e.g.: sudo clean-up)

if [ "$(whoami)" != 'root' ]; then
        echo "I detected non-root user: arpwatch will NOT run"
        #exit 1;
fi
RUNAS=$(whoami)
echo " Script running as:" pid=$$ , user=$RUNAS
#echo " Command-line was:" $0 $1 $2
#echo " Drop..."
sleep 2
#echo " Drop..."

##################
arpwatch -d &			# forking a bg process and returning immediately
sleep 1					# wait a sec for the subprocess to start ...
PSINFO=$(ps auxww|grep root|grep -i [a]rpwatch)		# square brackets to avoid self-ps
PID=$(pgrep -u root arpwatch)
echo " OK: arpwatch went in bg with pid="$PID
echo $PID >aw.pid
##################

sleep 2
#echo " Drop..."
#sleep 100
#echo " Drop..."
#echo " Last drop"
echo " ."

