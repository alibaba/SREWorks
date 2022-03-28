FROM registry.cn-hangzhou.aliyuncs.com/alisre/sreworks-base AS build
COPY . /app
WORKDIR /app
COPY settings.xml /root/.m2/settings.xml
RUN mvn -Dmaven.test.skip=true clean package

#FROM registry.cn-hangzhou.aliyuncs.com/alisre/openjdk:11.0.10-jre AS release
FROM adoptopenjdk/openjdk11:alpine-jre AS release
RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.aliyun.com/g' /etc/apk/repositories
RUN apk add --update --no-cache python3
USER root
WORKDIR /root
COPY --from=build /app/sreworks-job-worker/target/sreworks-job.jar /app/sreworks-job.jar
ENTRYPOINT ["java", "-Xmx720m", "-Xms720m", "-XX:ActiveProcessorCount=2", "-jar", "/app/sreworks-job.jar"]