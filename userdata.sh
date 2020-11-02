#!/bin/bash

# install updates
yum update -y

# install git
yum install git -y

# install sdk
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

# install gradle with sdk
sdk install gradle 6.3

# remove java 1.7
yum remove java-1.7.0-openjdk -y

# install java 8
yum install java-1.8.0 -y

# install java 11
#wget https://corretto.aws/downloads/latest/amazon-corretto-11-x64-linux-jdk.tar.gz
#rpm --import https://yum.corretto.aws/corretto.key
#curl -L -o /etc/yum.repos.d/corretto.repo https://yum.corretto.aws/corretto.repo
#yum install -y java-11-amazon-corretto-devel
amazon-linux-extras install java-openjdk11 -y

# clone repository
#git config --global url."https://ab1871d0be9654a2a88622e9e9cdb540766f3c41:x-oauth-basic@github.com/".insteadOf "https://github.com/"
#git clone https://github.com/mlferreira/Untitled-Api.git
cd api-dataset-archive
git pull

# build and install application
gradle bootDistZip
gradle assembleBootDist
gradle installBootDist
ln -s ~/api-dataset-archive/build/install/api-dataset-archive-boot/bin/api-dataset-archive /etc/init.d/api-dataset-archive
cd ..

# create a springboot user to run the app as a service
useradd springboot
# springboot login shell disabled
chsh -s /sbin/nologin springboot
chown springboot:springboot ~/api-dataset-archive/build/install/api-dataset-archive-boot/bin/api-dataset-archive
chmod 500 ~/api-dataset-archive/build/install/api-dataset-archive-boot/bin/api-dataset-archive

# start api-dataset-archive
service api-dataset-archive start

# automatically start services if this ec2 instance reboots
chkconfig api-dataset-archive on