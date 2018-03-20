#!/bin/sh

psid=0

#Kill already existing process
psid=`/usr/local/java/jdk1.6.0_45/bin/jps -l | grep com.yiqiniu.mktserver.YiQiNiuMktRTServer | awk '{print $1}'`

    if [ $psid -ne 0 ]; then
        su - root -c "kill -9 $psid"
    fi

programdir="."
num=$#
temp=$CLASSPATH
#setting libs path
libs=./lib/*
append(){
  temp=$temp":"$1
}
for file in $libs;    do
  append $file
done
append ":YiQiNiu-MKTRT-Server.jar"
export CLASSPATH=$temp:.:$programdir
export LANG=zh_CN
nohup java -server -XX:-PrintGC -XX:-PrintGCDetails -XX:-PrintGCTimeStamps -Xloggc:logs/yiqiniu.log -classpath $CLASSPATH  com.yiqiniu.task.YiQiNiuMKTaskServer & 

psid=`/usr/local/java/jdk1.6.0_45/bin/jps -l | grep com.yiqiniu.mktserver.YiQiNiuMktRTServer | awk '{print $1}'`

   if [ $psid -ne 0 ]; then
      echo "(pid=$psid) [Start OK]"
   else
      echo "[Start Failed]"
fi
