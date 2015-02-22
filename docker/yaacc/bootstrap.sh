###############################
# setup a dev-box for yaacc development !/usr/bin/env bash
################################
#Setup root password
echo -e "yaacc\nyaacc" | (passwd  $USER)
#Setup hostname

############################################################
## 
## Read config file
##
############################################################
source /tmp/yaacc-secret/config

############################################################
##
## create developer
##
############################################################
if id -u $developerName >/dev/null 2>&1; then
        echo "user already exists"
else
        echo "create user "$developerName
        useradd  $developerName -m -s /bin/bash
        echo "set user password to 'yaacc'"
        echo -e "yaacc\nyaacc" | (passwd  $developerName)
fi
if [ ! -d "/home/$developerName/.ssh" ];
then
   mkdir /home/$developerName/.ssh
fi
cp /tmp/yaacc-secret/$developerPrivateRSAKeyName /home/$developerName/.ssh/id_rsa
cp /tmp/yaacc-secret/$developerPublicRSAKeyName /home/$developerName/.ssh/id_rsa.pub
chown -R  $developerName:$developerName /home/$developerName/.ssh
chmod 600 /home/$developerName/.ssh/id_rsa
chmod 600 /home/$developerName/.ssh/id_rsa.pub
#############################################################
##  clone yaacc
############################################################
# setup ssh with  sf.net server
if [ ! -f "/home/$developerName/.ssh/known_hosts" ] 
then 
  touch /home/$developerName/.ssh/known_hosts
  chown -R  $developerName:$developerName /home/$developerName/.ssh/known_hosts
fi
sudo -n -u $developerName ssh-keygen -R git.code.sf.net
sudo -n -u $developerName ssh-keyscan -H git.code.sf.net >>  /home/$developerName/.ssh/known_hosts


sudo -n -u $developerName echo "[user]
  email = $developerEmail
  name = $developerCommitName" >> /home/$developerName/.gitconfig

#clone git repo

cd /home/$developerName
if [ ! -d "yaacc-code" ];
then
   sudo -n -u $developerName git clone $yaaccRepo yaacc-code
else
   cd  yaacc-code
   sudo -n -u $developerName git pull
fi

cd /home/$developerName

if [ ! -f "yaacc-code/yaacc/local.properties" ];
then
   sudo -n -u $developerName echo "sdk.dir=/usr/local/android-sdk" > yaacc-code/yaacc/local.properties
   chown -R  $developerName:$developerName yaacc-code/yaacc/local.properties
fi

if [ ! -f "yaacc-code/yaacc/project.properties" ];
then
  sudo -n -u $developerName echo "target=android-15" > yaacc-code/yaacc/project.properties
  chown -R  $developerName:$developerName yaacc-code/yaacc/project.properties
fi

if [ ! -f "yaacc-code/test/local.properties" ];
then
   sudo -n -u $developerName echo "sdk.dir=/usr/local/android-sdk" > yaacc-code/test/local.properties
   chown -R  $developerName:$developerName yaacc-code/test/local.properties
fi

if [ ! -f "yaacc-code/test/project.properties" ];
then
  sudo -n -u $developerName echo "target=android-15" > yaacc-code/test/project.properties
  chown -R  $developerName:$developerName yaacc-code/test/project.properties
fi


#####################################################################
## Setup debug keystore 
#####################################################################
if [ ! -d "/home/$developerName/.android" ] 
then 
   mkdir /home/$developerName/.android
   chmod 755 /home/$developerName/.android
   chown $developerName:$developerName /home/$developerName/.android
fi
cp /tmp/yaacc-secret/$debugKeyStoreName /home/$developerName/.android/
chmod 644 /home/$developerName/.android/debug.keystore
chown $developerName:$developerName /home/$developerName/.android/debug.keystore
