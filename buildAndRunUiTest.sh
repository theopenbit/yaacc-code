#!/bin/bash
# build and run all UITests
# Precondition emulator is running
# script is started in project root
set -e
oldPwd=$(pwd)
cd ./uitest
ant build
if [  -z $( ls /usr/local/android-sdk/tools/lib/ | grep libGL.so) ]
then
   ln -s /usr/lib/libGL.so /usr/local/android-sdk/tools/lib/libGL.so
fi
/usr/local/android-sdk/tools/emulator -avd yaacc-emu
/usr/local/android-sdk/platform-tools/adb push ./bin/YAACC-UITest.jar /data/local/tmp/
/usr/local/android-sdk/platform-tools/adb shell uiautomator runtest YAACC-UITest.jar -c de.yaacc.UITestCase 
cd $oldPwd
