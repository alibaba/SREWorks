FROM sreworks-registry.cn-beijing.cr.aliyuncs.com/mirror/maven:3.8.3-adoptopenjdk-11 AS build

COPY . /app
RUN mkdir /root/.m2/ && curl https://sreworks.oss-cn-beijing.aliyuncs.com/resource/settings.xml -o /root/.m2/settings.xml
RUN cd /app && mvn -Dmaven.test.skip=true clean package -U

FROM sreworks-registry.cn-beijing.cr.aliyuncs.com/mirror/openjdk8:alpine-jre AS release
ARG START_MODULE=tkg-one-start
ARG TARGET_DIRECTORY=tkg-one
ARG DEPENDENCY=/app/${START_MODULE}/target/${TARGET_DIRECTORY}

COPY --from=build /app/${START_MODULE}/target/${TARGET_DIRECTORY}.jar /app/${TARGET_DIRECTORY}.jar

COPY sbin/start.sh /app/start.sh
COPY ./${START_MODULE}/src/main/resources/application.properties /app/application.properties
RUN chmod +x /app/start.sh

ENTRYPOINT ["/app/start.sh"]