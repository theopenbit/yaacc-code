# android development environment for ubuntu (14.04 LTS).
# version 0.0.1
FROM ubuntu:14.04



MAINTAINER theOpenBit <tob@schoenesnetz.de>

RUN apt-get update --fix-missing
RUN apt-get install wget -y
RUN apt-get install libswt-gtk-3-jni libswt-gtk-3-java -y
# needed for android build-tools
RUN apt-get install libc6-i386 lib32stdc++6 lib32gcc1 lib32ncurses5 lib32z1 -y 

##############################################
##Install common unix tools
##############################################
RUN apt-get install vim -y
RUN apt-get install nano -y


##############################################
##Install java
############################################
RUN apt-get install software-properties-common -y
RUN apt-add-repository ppa:webupd8team/java
RUN apt-get update
RUN echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections
RUN apt-get install oracle-java8-installer -y
RUN update-java-alternatives -s java-8-oracle
RUN apt-get install oracle-java8-set-default -y


# Define commonly used JAVA_HOME variable
RUN ln -s /usr/lib/jvm/java-8-oracle /usr/lib/jvm/default-java
ENV JAVA_HOME /usr/lib/jvm/default-java


############################################
##Install git
############################################
RUN apt-get install git -y

############################################
##Install ant
############################################
RUN apt-get install ant -y

############################################
## Install Gradle
############################################
RUN add-apt-repository ppa:cwchien/gradle
RUN apt-get update
RUN apt-get install gradle -y

############################################
# install qgit
############################################
RUN apt-get install qgit -y

############################################
# install kdiff3
############################################
RUN apt-get install kdiff3-qt -y

##############################################
##Install android-sdk
##############################################


RUN for a in $( wget -qO- http://developer.android.com/sdk/index.html | egrep -o "http://dl.google.com[^\"']*linux.tgz" ); do \
                wget $a && tar --wildcards --no-anchored -xvzf android-sdk_*-linux.tgz; mv android-sdk-linux /usr/local/android-sdk; \
                chmod 777 -R /usr/local/android-sdk; rm android-sdk_*-linux.tgz;\
    done
RUN echo "export PATH=$PATH:/usr/local/android-sdk/platform-tools" >> /etc/bash.bashrc
RUN echo "export PATH=$PATH:/usr/local/android-sdk/tools" >> /etc/bash.bashrc

RUN if [ -f /bin/ddms ]; then \
  rm /bin/ddms; \
  fi
RUN ln -s /usr/local/android-sdk/tools/ddms /bin/ddms




############################
# additional target platforms
############################
RUN echo "y" | (/usr/local/android-sdk/tools/android update sdk -u --all -t build-tools-18.1.1)
RUN echo "y" | (/usr/local/android-sdk/tools/android update sdk -u --all -t build-tools-19.1.0)
RUN echo "y" | (/usr/local/android-sdk/tools/android update sdk -u --all -t build-tools-21.1.2)
RUN echo "y" | (/usr/local/android-sdk/tools/android update sdk -u --all -t tool)
RUN echo "y" | (/usr/local/android-sdk/tools/android update sdk -u --all -t platform-tool)
RUN echo "y" | (/usr/local/android-sdk/tools/android update sdk -u --all -t android-15)
RUN echo "y" | (/usr/local/android-sdk/tools/android update sdk -u --all -t android-17)
RUN echo "y" | (/usr/local/android-sdk/tools/android update sdk -u --all -t android-19)
RUN echo "y" | (/usr/local/android-sdk/tools/android update sdk -u --all -t android-21)
RUN echo "y" | (/usr/local/android-sdk/tools/android update sdk -u --all -t system-image) 


#########################
# setup permissions
#########################

RUN chmod a+w -R /usr/local/android-sdk

