#export JAVA_HOME=/usr/lib/jvm/jre-11-openjdk-11.0.7.10-4.amzn2.0.1.x86_64
export JAVA_HOME=/usr/lib/jvm/java-11-amazon-corretto
sudo killall java
sudo rm /etc/init.d/api-dataset-archive
source *.env
gradle clean
sudo killall java
./gradlew installBootDist
sudo killall java
sudo ln -s ~/api-dataset-archive/build/install/api-dataset-archive-boot/bin/api-dataset-archive /etc/init.d/api-dataset-archive