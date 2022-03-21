FROM registry.cn-hangzhou.aliyuncs.com/alisre/sreworks-base AS build
COPY . /app
WORKDIR /app
COPY settings.xml /root/.m2/settings.xml
RUN mvn -Dmaven.test.skip=true clean package

FROM registry.cn-hangzhou.aliyuncs.com/alisre/openjdk:11.0.10-jre AS release
USER root
WORKDIR /root
COPY --from=build /app/clustermanage-start/target/clustermanage.jar /app/clustermanage.jar
COPY --from=build /app/helm /app/helm
COPY --from=build /app/client-deploy-packages /app/client-deploy-packages
ENTRYPOINT ["java", "-Xmx1g", "-Xms1g", "-XX:ActiveProcessorCount=2", "-jar", "/app/clustermanage.jar"]
