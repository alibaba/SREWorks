FROM {{ MAVEN_IMAGE }} AS build
COPY . /app
WORKDIR /app
RUN mkdir /root/.m2/ && curl {{ MAVEN_SETTINGS_XML }} -o /root/.m2/settings.xml
RUN mvn -Dmaven.test.skip=true clean package

FROM {{ JRE11_ALPINE_IMAGE }} AS release
USER root
WORKDIR /root
COPY --from=build /app/sreworks-job-master/target/sreworks-job.jar /app/sreworks-job.jar
COPY entrypoint.sh /app/entrypoint.sh
ENTRYPOINT ["/bin/sh","/app/entrypoint.sh"]
