FROM sreworks-registry.cn-beijing.cr.aliyuncs.com/mirror/maven:3.8.3-adoptopenjdk-11 AS build
COPY . /app
RUN mkdir /root/.m2/ && curl https://sreworks.oss-cn-beijing.aliyuncs.com/resource/settings.xml -o /root/.m2/settings.xml
RUN cd /app && mvn -Dmaven.test.skip=true clean package

FROM sreworks-registry.cn-beijing.cr.aliyuncs.com/mirror/openjdk8:alpine-jre AS release
ARG START_MODULE=tesla-authproxy-start
ARG TARGET_DIRECTORY=tesla-authproxy-service
ARG JAR_NAME=tesla-authproxy.jar
ARG BUILD_JAR=/app/${START_MODULE}/target/tesla-authproxy.jar

COPY --from=build ${BUILD_JAR} /app/${JAR_NAME}
COPY ./sbin/ /app/sbin/
COPY ./${START_MODULE}/src/main/resources/application-docker.properties /app/application.properties
RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.tuna.tsinghua.edu.cn/g' /etc/apk/repositories
RUN apk add --update --no-cache gettext
ENTRYPOINT ["/app/sbin/run.sh"]