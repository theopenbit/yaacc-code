#!/bin/bash
set -e
if [ ! -f "yaacc-secret/config" ] 
then
 echo "before you start make sure you got an correct setup yaacc-secret folder"
 exit 1
fi

type docker >/dev/null 2>&1 || { echo >&2 "I require  docker but it's not installed (min version 1.5.0).  Aborting."; exit 1; }




#docker build -t yaacc/android ./android
#docker build -t yaacc/androidstudio ./androidstudio
#sudo docker build -t yaacc/dev -f yaacc/Dockerfile .
sudo docker build -t yaacc/fdroid -f fdroid/Dockerfile . 