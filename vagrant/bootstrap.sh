###############################
# setup a dev-box for yaacc development
#!/usr/bin/env bash
#  
################################
#TODO: must be a normal user
pwd="/root"
echo -e "vagrant\nvagrant" | (passwd  $USER)

apt-get update

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
  echo -e "y" | (/usr/local/android-sdk/tools/android update sdk -u -t build-tools-18.1.1)
  echo -e "y" | (/usr/local/android-sdk/tools/android update sdk -u -t tool)
  echo -e "y" | (/usr/local/android-sdk/tools/android update sdk -u -t platform-tool)
  echo -e "y" | (/usr/local/android-sdk/tools/android update sdk -u -t android-15)
  echo -e "y" | (/usr/local/android-sdk/tools/android update sdk -u -t android-17)
else
echo "Android Debug Bridge already detected."
fi

#############################################################
##  clone yaacc
##  TODO: What about credentials?
############################################################
apt-get install git  -y
cd ~
if [ ! -d "yaacc-code" ];
then
   git clone git://git.code.sf.net/p/yaacc/code yaacc-code
else
   cd  yaacc-code
   git pull
fi

cd $pwd

if [ ! -f "yaacc-code/main/local.properties" ];
then
   echo "sdk.dir=/usr/local/android-sdk" > yaacc-code/main/local.properties
fi

if [ ! -f "yaacc-code/main/project.properties" ];
then
  echo "target=android-15" > yaacc-code/main/project.properties
fi

cd  yaacc-code/main
ant debug

#############################################################
## Setup F-droid server tools
##  TODO: What about credentials?
#############################################################
cd $pwd

apt-get install python -y
apt-get install python-magic -y

if [ ! -d "fdroidserver.git" ];
then
   git clone git://gitorious.org/f-droid/fdroidserver.git fdroidserver.git
   echo "export PATH=\$PATH:/root/fdroidserver.git" >> $pwd/.bashrc   
else
   cd fdroidserver.git
   git pull
fi


#############################################################
## Setup F-droid repo
##  TODO: What about credentials?
#############################################################
cd $pwd
if [ ! -d "fdroiddata.git" ];
then
  git clone git://gitorious.org/f-droid/fdroiddata.git  fdroiddata.git
else
  cd fdroiddata.git
  git pull
fi
cd $pwd
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
   echo "repo_keyalias = \"$(< /mnt/yaacc-keystore/repo_keyalias.txt)\"" >> fdroiddata.git/config.py
   echo "keystore = \"/mnt/yaacc-keystore/yaacc.de-release-key.keystore\"" >> fdroiddata.git/config.py
   echo "keystorepass = \"$(< /mnt/yaacc-keystore/keystorepass.txt)\"" >> fdroiddata.git/config.py
   echo "keypass = \"$(< /mnt/yaacc-keystore/keypass.txt)\"" >> fdroiddata.git/config.py

 
fi

cd $pwd
PATH=$PATH:$pwd/fdroidserver.git
cd fdroiddata.git
echo test build yaacc with fdroid
fdroid build -p de.yaacc

echo ready enjoy developing!
