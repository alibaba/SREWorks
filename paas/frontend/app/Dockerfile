FROM sreworks-registry.cn-beijing.cr.aliyuncs.com/mirror/node:14.20.0-alpine3.15 AS build-source
COPY . /app
RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.aliyun.com/g' /etc/apk/repositories \
    && apk add --update --no-cache zip
RUN ls -l /app/
RUN /usr/local/bin/yarn --version
RUN export NPM_REGISTRY_URL="https://registry.npmmirror.com" && cd /app && /usr/local/bin/yarn install --registry=$NPM_REGISTRY_URL && ls -l /app/app/node_modules/antd/ && /usr/local/bin/yarn run build:sequential
RUN cd /app/app && mv ./sbin ./build/ && mv ./APP-META-PRIVATE/deploy-config ./build/ && zip -r /app/build.zip ./

FROM sreworks-registry.cn-beijing.cr.aliyuncs.com/mirror/alpine:latest AS release
RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.aliyun.com/g' /etc/apk/repositories \
    && apk add --update --no-cache nginx zip

COPY --from=build-source /app/build.zip /app/build.zip

RUN cd /app && unzip build.zip && rm -rf build.zip
ENTRYPOINT ["/app/sbin/run.sh"]
