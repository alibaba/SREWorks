FROM sreworks-registry.cn-beijing.cr.aliyuncs.com/mirror/maven:3.8.3-adoptopenjdk-11 AS build
COPY . /app
RUN mkdir /root/.m2/ && curl https://sreworks.oss-cn-beijing.aliyuncs.com/resource/settings.xml -o /root/.m2/settings.xml
RUN cd /app && mvn -Dmaven.test.skip=true clean package

FROM sreworks-registry.cn-beijing.cr.aliyuncs.com/mirror/openjdk8:alpine-jre AS release
ARG JAR_NAME=action.jar
ARG BUILD_JAR=/app/target/action-0.0.1-SNAPSHOT.jar

COPY --from=build ${BUILD_JAR} /app/${JAR_NAME}
COPY ./sbin/ /app/sbin/
COPY ./src/main/resources/application-docker.properties /app/application.properties

RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.tuna.tsinghua.edu.cn/g' /etc/apk/repositories
RUN apk add --update --no-cache gettext \
    && chmod +x /app/sbin/*.sh
ENTRYPOINT ["/app/sbin/run.sh"]
