#!/bin/bash

appName=mosquito-blog
appPort=9000
appPath=`pwd`

pidFilePath=${appPath}/target/universal/${appName}-1.0/RUNNING_PID

if [  -f "${pidFilePath}" ]; then
  cd ${appPath}/target/universal/${appName}-1.0
  pid=`cat RUNNING_PID`;
  kill -9 ${pid}

fi