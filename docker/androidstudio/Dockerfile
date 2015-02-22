# android development environment for ubuntu (14.04 LTS).
# version 0.0.1
FROM yaacc/android



MAINTAINER theOpenBit <tob@schoenesnetz.de>



############################
## Android studio
###########################
RUN apt-get install unzip -y

RUN wget https://dl.google.com/dl/android/studio/ide-zips/1.0.1/android-studio-ide-135.1641136-linux.zip -O /opt/android-studio-ide-135.1641136-linux.zip
RUN unzip /opt/android-studio-ide-135.1641136-linux.zip -d /opt
RUN rm /opt/android-studio-ide-135.1641136-linux.zip
RUN echo "export PATH=\$PATH:/opt/android-studio/bin/" >> /etc/bash.bashrc



