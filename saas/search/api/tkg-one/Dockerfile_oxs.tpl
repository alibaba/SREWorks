#FROM reg.docker.alibaba-inc.com/abm-aone/maven:alios7u2min-ajdk-8-3.6.3 AS build
#
#COPY . /app
#RUN cd /app && mvn -Dmaven.test.skip=true clean package -U
#
#FROM reg.docker.alibaba-inc.com/abm-aone/jdk:alios7u2min-ajdk-8 AS release
#ARG START_MODULE=tkg-one-start
#ARG TARGET_DIRECTORY=tkg-one
#ARG DEPENDENCY=/app/${START_MODULE}/target/${TARGET_DIRECTORY}
#
#COPY --from=build /app/${START_MODULE}/target/${TARGET_DIRECTORY}.jar /app/${TARGET_DIRECTORY}.jar
#
#COPY APP-META-OXS/sbin/start.sh /app/start.sh
#RUN chmod +x /app/start.sh
#
#ENTRYPOINT ["/app/start.sh"]
