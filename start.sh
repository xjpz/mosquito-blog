#!/bin/bash

appName=mosquito-blog
appVersion=1.0
appPort=9000

appPath=`pwd`
pidFilePath=${appPath}/target/universal/${appName}-${appVersion}/RUNNING_PID

if [  -f "${pidFilePath}" ]; then
  cd ${appPath}/target/universal/${appName}-${appVersion}
  pid=`cat RUNNING_PID`;
  kill -9 ${pid}
  rm -rf RUNNING_PID
fi

cd ${appPath}
./activator clean
./activator dist
cd ${appPath}/target/universal
unzip ${appName}-${appVersion}.zip

cd ${appPath}/target/universal/${appName}-${appVersion}/bin
./${appName}  &

