# YAACC Android development environment for ubuntu (14.04 LTS).
# version 0.0.1
FROM yaacc/android
MAINTAINER theOpenBit <tob@schoenesnetz.de>


COPY ./yaacc/bootstrap.sh /tmp/
COPY ./yaacc-secret /tmp/yaacc-secret
RUN /bin/bash /tmp/bootstrap.sh
VOLUME ["/mnt/share"]




