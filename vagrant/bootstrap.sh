###############################
# setup a dev-box for yaacc development !/usr/bin/env bash
################################
#Setup root password
echo -e "vagrant\nvagrant" | (passwd  $USER)
#Setup hostname
echo -e "yaacc-dev"  > /etc/hostname
if [ -z "$(cat /etc/hosts | grep 'yaacc-dev')" ]
then
 echo -e "127.0.1.1       yaacc-dev" >> /etc/hosts
fi
apt-get update

apt-get install ia32-libs -y

##############################################
##Install common unix tools
##############################################
apt-get install vim -y
apt-get install nano -y


#############################################
## Install xterm and roxterm in order to
## enable an ssh access using an  X tunnel
##############################################
apt-get install xterm -y
apt-get install roxterm -y

##############################################
##Install java
##############################################
if [ ! -d "/usr/lib/jvm/java-7-oracle" ];
then
  apt-get install python-software-properties -y
  add-apt-repository ppa:webupd8team/java
  apt-get update
  echo oracle-java7-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections
  apt-get install oracle-java7-installer -y
  update-java-alternatives -s java-7-oracle
  apt-get install oracle-java7-set-default -y

fi
#############################################
## Install android
## based on http://linuxundich.de/static/android_sdk_installer.sh
############################################
apt-get install ant -y

#Download and install the Android SDK
if [ ! -d "/usr/local/android-sdk" ]; then
        for a in $( wget -qO- http://developer.android.com/sdk/index.html | egrep -o "http://dl.google.com[^\"']*linux.tgz" ); do 
                wget $a && tar --wildcards --no-anchored -xvzf android-sdk_*-linux.tgz; mv android-sdk-linux /usr/local/android-sdk;  chmod 777 -R /usr/local/android-sdk; rm android-sdk_*-linux.tgz;
        done
        
else
     echo "Android SDK already installed to /usr/local/android-sdk.  Skipping."
fi

#Check if the ADB environment is set up.

if grep -q /usr/local/android-sdk/platform-tools /etc/bash.bashrc; 
then
    echo "ADB environment already set up"
else
    echo "export PATH=$PATH:/usr/local/android-sdk/platform-tools" >> /etc/bash.bashrc
fi

#Check if the sdk tools environment is set up.

if grep -q /usr/local/android-sdk/platform-tools /etc/bash.bashrc; 
then
    echo "android sdk tools environment already set up"
else
    echo "export PATH=$PATH:/usr/local/android-sdk/tools" >> /etc/bash.bashrc
fi


#Check if the ddms symlink is set up.

if [ -f /bin/ddms ] 
then
    rm /bin/ddms; ln -s /usr/local/android-sdk/tools/ddms /bin/ddms
else
    ln -s /usr/local/android-sdk/tools/ddms /bin/ddms
fi


#Create etc/udev/rules.d/99-android.rules file
if [ ! -f "/etc/udev/rules.d/99-android.rules" ];
then  
  touch -f 99-android.rules
  echo "#Acer" >> 99-android.rules
  echo "SUBSYSTEM=="usb", SYSFS{idVendor}=="0502", MODE="0666"" >> 99-android.rules
  echo "#ASUS" >> 99-android.rules
  echo "SUBSYSTEM=="usb", SYSFS{idVendor}=="0b05", MODE="0666"" >> 99-android.rules
  echo "#Dell" >> 99-android.rules
  echo "SUBSYSTEM=="usb", SYSFS{idVendor}=="413c", MODE="0666"" >> 99-android.rules
  echo "#Foxconn" >> 99-android.rules
  echo "SUBSYSTEM=="usb", SYSFS{idVendor}=="0489", MODE="0666"" >> 99-android.rules
  echo "#Garmin-Asus" >> 99-android.rules
  echo "SUBSYSTEM=="usb", SYSFS{idVendor}=="091E", MODE="0666"" >> 99-android.rules
  echo "#Google" >> 99-android.rules
  echo "SUBSYSTEM=="usb", SYSFS{idVendor}=="18d1", MODE="0666"" >> 99-android.rules
  echo "#HTC" >> 99-android.rules
  echo "SUBSYSTEM=="usb", SYSFS{idVendor}=="0bb4", MODE="0666"" >> 99-android.rules
  echo "#Huawei" >> 99-android.rules
  echo "SUBSYSTEM=="usb", SYSFS{idVendor}=="12d1", MODE="0666"" >> 99-android.rules
  echo "#K-Touch" >> 99-android.rules
  echo "SUBSYSTEM=="usb", SYSFS{idVendor}=="24e3", MODE="0666"" >> 99-android.rules
  echo "#KT Tech" >> 99-android.rules
  echo "SUBSYSTEM=="usb", SYSFS{idVendor}=="2116", MODE="0666"" >> 99-android.rules
  echo "#Kyocera" >> 99-android.rules
  echo "SUBSYSTEM=="usb", SYSFS{idVendor}=="0482", MODE="0666"" >> 99-android.rules
  echo "#Lenevo" >> 99-android.rules
  echo "SUBSYSTEM=="usb", SYSFS{idVendor}=="17EF", MODE="0666"" >> 99-android.rules
  echo "#LG" >> 99-android.rules
  echo "SUBSYSTEM=="usb", SYSFS{idVendor}=="1004", MODE="0666"" >> 99-android.rules
  echo "#Motorola" >> 99-android.rules
  echo "SUBSYSTEM=="usb", SYSFS{idVendor}=="22b8", MODE="0666"" >> 99-android.rules
  echo "#NEC" >> 99-android.rules
  echo "SUBSYSTEM=="usb", SYSFS{idVendor}=="0409", MODE="0666"" >> 99-android.rules
  echo "#Nook" >> 99-android.rules
  echo "SUBSYSTEM=="usb", SYSFS{idVendor}=="2080", MODE="0666"" >> 99-android.rules
  echo "#Nvidia" >> 99-android.rules
  echo "SUBSYSTEM=="usb", SYSFS{idVendor}=="0955", MODE="0666"" >> 99-android.rules
  echo "#OTGV" >> 99-android.rules
  echo "SUBSYSTEM=="usb", SYSFS{idVendor}=="2257", MODE="0666"" >> 99-android.rules
  echo "#Pantech" >> 99-android.rules
  echo "SUBSYSTEM=="usb", SYSFS{idVendor}=="10A9", MODE="0666"" >> 99-android.rules
  echo "#Philips" >> 99-android.rules
  echo "SUBSYSTEM=="usb", SYSFS{idVendor}=="0471", MODE="0666"" >> 99-android.rules
  echo "#PMC-Sierra" >> 99-android.rules
  echo "SUBSYSTEM=="usb", SYSFS{idVendor}=="04da", MODE="0666"" >> 99-android.rules
  echo "#Qualcomm" >> 99-android.rules
  echo "SUBSYSTEM=="usb", SYSFS{idVendor}=="05c6", MODE="0666"" >> 99-android.rules
  echo "#SK Telesys" >> 99-android.rules
  echo "SUBSYSTEM=="usb", SYSFS{idVendor}=="1f53", MODE="0666"" >> 99-android.rules
  echo "#Samsung" >> 99-android.rules
  echo "SUBSYSTEM=="usb", SYSFS{idVendor}=="04e8", MODE="0666"" >> 99-android.rules
  echo "#Sharp" >> 99-android.rules
  echo "SUBSYSTEM=="usb", SYSFS{idVendor}=="04dd", MODE="0666"" >> 99-android.rules
  echo "#Sony Ericsson" >> 99-android.rules
  echo "SUBSYSTEM=="usb", SYSFS{idVendor}=="0fce", MODE="0666"" >> 99-android.rules
  echo "#Toshiba" >> 99-android.rules
  echo "SUBSYSTEM=="usb", SYSFS{idVendor}=="0930", MODE="0666"" >> 99-android.rules
  echo "#ZTE" >> 99-android.rules
  echo "SUBSYSTEM=="usb", SYSFS{idVendor}=="19D2", MODE="0666"" >> 99-android.rules
  mv -f 99-android.rules /etc/udev/rules.d/
  chmod a+r /etc/udev/rules.d/99-android.rules
fi

if [ ! -f "/usr/local/android-sdk/platform-tools/adb" ];
then
  echo -e "y" | (/usr/local/android-sdk/tools/android update sdk -u --all -t build-tools-18.1.1)
  echo -e "y" | (/usr/local/android-sdk/tools/android update sdk -u --all -t tool)
  echo -e "y" | (/usr/local/android-sdk/tools/android update sdk -u --all -t platform-tool)
  echo -e "y" | (/usr/local/android-sdk/tools/android update sdk -u --all -t android-15)
  echo -e "y" | (/usr/local/android-sdk/tools/android update sdk -u --all -t android-17)
  echo -e "y" |  (/usr/local/android-sdk/tools/android update sdk -u --all -t system-image)
else
echo "Android Debug Bridge already detected."
fi
############################################################
## 
## Read config file
##
############################################################
source /mnt/yaacc-secret/config

############################################################
##
## create developer
##
############################################################
if id -u $developerName >/dev/null 2>&1; then
        echo "user already exists"
else
        echo "create user"
        useradd  $developerName -m -s /bin/bash
        echo -e "vagrant\nvagrant" | (passwd  $developerName)
fi
if [ ! -d "/home/$developerName/.ssh" ];
then
   mkdir /home/$developerName/.ssh
fi
cp /mnt/yaacc-secret/$developerPrivateRSAKeyName /home/$developerName/.ssh/id_rsa
cp /mnt/yaacc-secret/$developerPublicRSAKeyName /home/$developerName/.ssh/id_rsa.pub
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

#install git
apt-get install git  -y

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

if [ ! -f "yaacc-code/main/local.properties" ];
then
   sudo -n -u $developerName echo "sdk.dir=/usr/local/android-sdk" > yaacc-code/main/local.properties
   chown -R  $developerName:$developerName yaacc-code/main/local.properties
fi

if [ ! -f "yaacc-code/main/project.properties" ];
then
  sudo -n -u $developerName echo "target=android-15" > yaacc-code/main/project.properties
  chown -R  $developerName:$developerName yaacc-code/main/project.properties
fi

if [ ! -f "yaacc-code/test/local.properties" ];
then
   sudo -n -u $developerName echo "sdk.dir=/usr/local/android-sdk" > yaacc-code/main/local.properties
   chown -R  $developerName:$developerName yaacc-code/main/local.properties
fi

if [ ! -f "yaacc-code/test/project.properties" ];
then
  sudo -n -u $developerName echo "target=android-15" > yaacc-code/main/project.properties
  chown -R  $developerName:$developerName yaacc-code/main/project.properties
fi
#############################################################
## Setup F-droid server tools
#############################################################
cd /home/$developerName

apt-get install python -y
apt-get install python-magic -y

if [ ! -d "fdroidserver.git" ];
then
   sudo -n -u $developerName git clone $fdroidServerRepo fdroidserver.git
   sudo -n -u $developerName echo "PATH=\$PATH:/home/$developerName/fdroidserver.git" >> /home/$developerName/.bashrc   
else
   cd fdroidserver.git
   sudo -n -u $developerName git pull
fi


#############################################################
## Setup F-droid repo
#############################################################
cd /home/$developerName
if [ ! -d "fdroiddata.git" ];
then
  sudo -n -u $developerName git clone $fdroidDataRepo  fdroiddata.git
else
  cd fdroiddata.git
  sudo -n -u $developerName git pull
fi
cd  /home/$developerName
if [ ! -f "fdroiddata.git/config.py" ];
then
   echo "sdk_path = \"/usr/local/android-sdk/\"" >> fdroiddata.git/config.py
   echo "ndk_path = \"/path/to/android-ndk-r8e\"" >> fdroiddata.git/config.py

   echo "aapt_path = \"/usr/local/android-sdk/platform-tools/aapt\""  >> fdroiddata.git/config.py
   echo "javacc_path = \"/usr/lib/jvm/java-7-oracle\"" >> fdroiddata.git/config.py
   echo "mvn3 = \"mvn\"" >> fdroiddata.git/config.py
   echo "gradle = \"gradle\"" >> fdroiddata.git/config.py
   echo "gradle_plugin = \"0.6.+\"" >> fdroiddata.git/config.py
   echo "icon_max_size = 72" >> fdroiddata.git/config.py
   echo "build_tools = \"18.1.1\"" >> fdroiddata.git/config.py
   echo "repo_url = \"https://f-droid.org/repo\"" >> fdroiddata.git/config.py
   echo "repo_name = \"F-Droid\"" >> fdroiddata.git/config.py
   echo "repo_icon = \"fdroid-icon.png\"" >> fdroiddata.git/config.py
   echo "repo_description = \"\"\"" >> fdroiddata.git/config.py
   echo "archive_older = 3" >> fdroiddata.git/config.py
   echo "archive_url = \"https://f-droid.org/archive\"" >> fdroiddata.git/config.py
   echo "archive_name = \"F-Droid Archive\"" >> fdroiddata.git/config.py
   echo "archive_icon = \"fdroid-icon.png\"" >> fdroiddata.git/config.py
   echo "archive_description = \"\"\"" >> fdroiddata.git/config.py
   echo "build_server_always = False" >>fdroiddata.git/config.py
   echo "repo_keyalias = \"$repo_keyalias\"" >> fdroiddata.git/config.py
   echo "keystore = \"/mnt/yaacc-secret/$keystoreName\"" >> fdroiddata.git/config.py
   echo "keystorepass = \"$keystorepass\"" >> fdroiddata.git/config.py
   echo "keypass = \"$keypass\"" >> fdroiddata.git/config.py
   chmod 0600 fdroiddata.git/config.py
   chown $developerName:$developerName fdroiddata.git/config.py 
 
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
cp /mnt/yaacc-secret/debug.keystore /home/$developerName/.android/
chmod 644 /home/$developerName/.android/debug.keystore
chown $developerName:$developerName /home/$developerName/.android/debug.keystore



############################################
# build yaacc
############################################
cd  /home/$developerName/yaacc-code/main
sudo -n -u $developerName ant debug

cd /home/$developerName/fdroiddata.git
echo test build yaacc with fdroid
sudo -n -u $developerName /home/$developerName/fdroidserver.git/fdroid build -l de.yaacc


###############################################
# create  AVD
##############################################
if [ -z $(/usr/local/android-sdk/tools/android list avd | grep yaacc-emu) ]
then
 sudo -n -u $developerName  echo "n" | (/usr/local/android-sdk/tools/android create avd -n yaacc-emu -t android-17)
fi

############################################
# start avd and install yaacc on it
###########################################
 /usr/local/android-sdk/tools/emulator -avd yaacc-emu
 /usr/local/android-sdk/platform-tools/adb install /root/yaacc-code/main/bin/YAACC-debug.apk
#
############################################
# install kdiff3
############################################
apt-get install kdiff3-qt -y

############################################
# install android studion
############################################
cd /home/$developerName/
if [ ! -d "./android-studio" ] 
then
  sudo -n -u $developerName wget http://dl.google.com/android/studio/install/0.4.2/android-studio-bundle-133.970939-linux.tgz
  sudo -n -u $developerName tar -xvzf /home/$developerName/android-studio-bundle-133.970939-linux.tgz  
  sudo -n -u $developerName echo "PATH=\$PATH:/home/$developerName/android-studio/bin/" >> /home/$developerName/.bashrc   
fi  
############################################
# install qgit
############################################
apt-get install qgit -y

echo ready enjoy developing!
