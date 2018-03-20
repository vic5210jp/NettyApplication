#!/bin/sh

psid=0
psid=`/usr/local/java/jdk1.6.0_45/bin/jps -l | grep com.yiqiniu.mktserver.YiQiNiuMktRTServer | awk '{print $1}'`
stop() {

   if [ $psid -ne 0 ]; then
      echo -n "Stopping com.yiqiniu.mktserver.YiQiNiuMktRTServer ...(pid=$psid) "
      su - root -c "kill -9 $psid"
      if [ $? -eq 0 ]; then
         echo "[OK]"
      else
         echo "[Failed]"
      fi
   else
      echo "================================"
      echo "warn: com.yiqiniu.mktserver.YiQiNiuMktRTServer not running"
      echo "================================"
   fi
}

stop

exit 0
