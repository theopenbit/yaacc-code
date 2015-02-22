#!/bin/bash
source ./yaacc-secret/config

sudo docker run  -t -i --name fdroid  -u="$developerName" -e DISPLAY=$DISPLAY  -v /tmp/.X11-unix:/tmp/.X11-unix yaacc/fdroid /bin/bash 
if [ $? -ne 0 ]; then
    echo "Container allready exist Restart... "
    sudo docker start -i  fdroid    
fi
