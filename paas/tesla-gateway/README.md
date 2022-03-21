Tesla Gateway
------
一、集团内
---
代码编译、打包、启动
    
    mvn -f pom.xml clean install -Dmaven.test.skip=true pandora-boot:run -e
    
二、专有云
---
代码编译、打包、启动

    mvn -f pom_private.xml clean install -Dmaven.test.skip=true -Dspring.profiles.active=private-local spring-boot:run -e
 
