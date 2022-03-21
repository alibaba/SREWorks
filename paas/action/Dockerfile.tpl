FROM {{ MAVEN_IMAGE }} AS build
COPY . /app
RUN cd /app && mvn -Dmaven.test.skip=true clean package

FROM {{ JRE8_IMAGE }} AS release
ARG JAR_NAME=action.jar
ARG BUILD_JAR=/app/target/action-0.0.1-SNAPSHOT.jar

COPY --from=build ${BUILD_JAR} /app/${JAR_NAME}
COPY ./sbin/ /app/sbin/
COPY ./src/main/resources/application-docker.properties /app/application.properties

RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.aliyun.com/g' /etc/apk/repositories
RUN apk add --update --no-cache gettext \
    && chmod +x /app/sbin/*.sh
ENTRYPOINT ["/app/sbin/run.sh"]
