
FROM sreworks-registry.cn-beijing.cr.aliyuncs.com/mirror/alpine:latest AS release
RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.tuna.tsinghua.edu.cn/g' /etc/apk/repositories

COPY . /app

RUN wget https://sreworks.oss-cn-beijing.aliyuncs.com/bin/mc-linux-amd64 -O /app/sbin/mc
RUN chmod +x /app/sbin/mc

# sreworks env job
RUN mkdir /app/sreworks-job-resource/
RUN wget https://sreworks.oss-cn-beijing.aliyuncs.com/resource/venv5-2.2.zip -O /app/sreworks-job-resource/venv5-2.2.zip
RUN wget https://sreworks.oss-cn-beijing.aliyuncs.com/resource/flink-ml-uber-2.2-SNAPSHOT.jar -O /app/sreworks-job-resource/flink-ml-uber-2.2-SNAPSHOT.jar
RUN wget https://sreworks.oss-cn-beijing.aliyuncs.com/resource/flink-python_2.12-1.15.2.jar -O /app/sreworks-job-resource/flink-python_2.12-1.15.2.jar

ENTRYPOINT ["/bin/sh", "/app/sbin/init.sh"]
