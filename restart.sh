#!/bin/bash

appName=mosquito-blog
appPort=9000
appPath=`pwd`

pidFilePath=${appPath}/target/universal/${appName}-1.0/RUNNING_PID

if [  -f "${pidFilePath}" ]; then
  cd ${appPath}/target/universal/${appName}-1.0
  pid=`cat RUNNING_PID`;
  kill -9 ${pid}

  cd ..
  rm -rf ${appName}-1.0

fi

cd ${appPath}

if [ ! -f "${appPath}/target/universal/${appName}-1.0.zip" ]
 then
    activator clean
    activator dist
fi

cd ${appPath}/target/universal
unzip ${appName}-1.0.zip

cd ${appPath}/target/universal/${appName}-1.0/bin
./${appName} -Dapplication.secret="xjpz...123" &
