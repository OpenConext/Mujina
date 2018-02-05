# How to create SAML Mock services on an AWS EC2 instance
Create an EC2 instance and log in to via ssh and run the following steps:

1.  [Update your OS](#1-update-your-os)
2.  [Install java 8](#2-install-java-8)
3.  [Install & configure maven](#3-install--configure-maven)
4.  [Install git](#4-install-git)
5.  [Clone the laa-sam-mock gihub repo](#5-clone-the-laa-sam-mock-gihub-repo)
6.  [Run mvn install](#6-run-mvn-install)
7.  [Configure spring boot application.yml files](#7-configure-spring-boot-applicationyml-files)
8.  [Create startup script for spring boot apps](#8-create-startup-script-for-spring-boot-apps)
9.  [Create a service to start with the OS](#9-create-a-service-to-start-with-the-os)
10. [Reboot your EC2 instance](#10-reboot-your-ec2-instance)

### 1. Update your OS
```
sudo yum update -y
```

### 2. Install java 8
```
sudo yum remove -y java-1.7.0-openjdk
sudo yum install -y java-1.8.0-openjdk-devel.x86_64
```

### 3. Install & configure maven
```
cd ~
wget http://apache.mirrors.nublue.co.uk/maven/maven-3/3.5.2/binaries/apache-maven-3.5.2-bin.tar.gz
tar -xvf apache-maven-3.5.2-bin.tar.gz
rm apache-maven-3.5.2-bin.tar.gz
```

Add Maven to the environment
```
vim ~/.bash_profile
```

... and insert this text:
```
# .bash_profile

# Get the aliases and functions
if [ -f ~/.bashrc ]; then
        . ~/.bashrc
fi

# User specific environment and startup programs

PATH=$PATH:$HOME/.local/bin:$HOME/bin

export PATH

export M2_HOME=~/apache-maven-3.5.2
export PATH=${M2_HOME}/bin:${PATH}
```

### 4. Install git
```
sudo yum install -y git
```

### 5. Clone the laa-sam-mock gihub repo
```
git clone https://github.com/ministryofjustice/laa-saml-mock
```

### 6. Run mvn install
```
cd laa-saml-mock
mvn clean install
```

### 7. Configure spring boot application.yml files
```
vim /home/ec2-user/laa-saml-mock/mujina-idp/laa-saml-mock-idp-application.yml
```

... and insert this text:
```
idp:
  base_url: http://${EC2_PUBLIC_HOST}:8080

samlUserStore:
  samlUsers:
    - username: test-user
      password: test password
      samlAttributes:
        attribute 1: test attribute 1 value
        attribute 2: test attribute 2 value
```

```
vim /home/ec2-user/laa-saml-mock/mujina-sp/laa-saml-mock-sp-application.yml
```

... and insert this text:
```
sp:
  base_url: http://${EC2_PUBLIC_HOST}:9090
  entity_id: http://mock-sp
  single_sign_on_service_location: http://${EC2_PUBLIC_HOST}:8080/SingleSignOnService
  acs_location_path: /saml/SSO
```

### 8. Create startup script for spring boot apps
```
vim ~/start-laa-saml-mock-services.sh
```

... and insert this text:
```
#!/bin/bash
export EC2_PUBLIC_HOST=`curl http://169.254.169.254/latest/meta-data/public-ipv4`;

cd /home/ec2-user/laa-saml-mock/mujina-idp/target; sudo -u ec2-user nohup java -DEC2_PUBLIC_HOST=${EC2_PUBLIC_HOST} -jar laa-saml-mock-idp-1.0.0.jar --spring.config.location=/home/ec2-user/laa-saml-mock/mujina-idp/laa-saml-mock-idp-application.yml &

cd /home/ec2-user/laa-saml-mock/mujina-sp/target; sudo -u ec2-user nohup java -DEC2_PUBLIC_HOST=${EC2_PUBLIC_HOST} -jar laa-saml-mock-sp-1.0.0.jar --spring.config.location=/home/ec2-user/laa-saml-mock/mujina-sp/laa-saml-mock-sp-application.yml &
```

### 9. Create a service to start with the OS
```
sudo vim /etc/rc.local
```

... and insert this text:
```
#!/bin/sh
#
# This script will be executed *after* all the other init scripts.
# You can put your own initialization stuff in here if you don't
# want to do the full Sys V style init stuff.

touch /var/lock/subsys/local
sh /home/ec2-user/start-laa-saml-mock-services.sh
```

### 10. Reboot your EC2 instance
1. The Mock IdP can be contacted at http://${EC2_PUBLIC_HOST}:8080
   * e.g. http://35.178.48.233:8080
2. The Mock SP can be contacted at http://${EC2_PUBLIC_HOST}:9090
   * e.g. http://35.178.48.233:9090
