#!/bin/bash
# build and run all UITests
# Precondition emulator is running
# script is started in project root
set -e
oldPwd=$(pwd)
cd ./uitest
ant build
$ANDROID_HOME/platform-tools/adb push ./bin/YAACC-UITest.jar /data/local/tmp/
$ANDROID_HOME/platform-tools/adb shell uiautomator runtest YAACC-UITest.jar -c de.yaacc.UITestCase 
cd $oldPwd