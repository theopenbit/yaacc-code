#!/bin/bash
source ./yaacc-secret/config

shareFolder=$(pwd)"/yaacc-container"
echo "set share folder to: "$shareFolder

sudo docker run  -t -i --name yaacc-dev  -u="$developerName" -e DISPLAY=$DISPLAY  -v /tmp/.X11-unix:/tmp/.X11-unix -v $shareFolder:/mnt/share yaacc/dev /bin/bash 
if [ $? -ne 0 ]; then
    echo "Container allready exist Restart... "
    sudo docker start -i  yaacc-dev    
fi
