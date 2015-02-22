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
   echo "javacc_path = \"/usr/lib/jvm/java-8-oracle\"" >> fdroiddata.git/config.py
   echo "mvn3 = \"mvn\"" >> fdroiddata.git/config.py
   echo "gradle = \"gradle\"" >> fdroiddata.git/config.py
   echo "gradle_plugin = \"0.6.+\"" >> fdroiddata.git/config.py
   echo "icon_max_size = 72" >> fdroiddata.git/config.py
   echo "build_tools = \"19.1.0\"" >> fdroiddata.git/config.py
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


