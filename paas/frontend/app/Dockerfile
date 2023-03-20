FROM sreworks-registry.cn-beijing.cr.aliyuncs.com/mirror/node:16.17-alpine AS build1
COPY . /app
RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.aliyun.com/g' /etc/apk/repositories \
    && apk add --update --no-cache zip
RUN export NPM_REGISTRY_URL="https://registry.npmmirror.com" && cd /app/docs/ && yarn install --registry=$NPM_REGISTRY_URL && yarn run build:doc
RUN cd /app/docs/build && zip -r /app/docs.zip ./

FROM sreworks-registry.cn-beijing.cr.aliyuncs.com/mirror/node:14.20.0-alpine3.15 AS build2
COPY . /app
RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.aliyun.com/g' /etc/apk/repositories && apk add --update --no-cache zip

RUN export NPM_REGISTRY_URL="https://registry.npmmirror.com" && cd /app && yarn install --registry=$NPM_REGISTRY_URL && yarn run build:sequential
RUN cd /app/app && mv ./sbin ./build/ && mv ./APP-META-PRIVATE/deploy-config ./build/ && cd ./build/ &&  zip -r /app/build.zip ./

FROM sreworks-registry.cn-beijing.cr.aliyuncs.com/mirror/alpine:latest AS release
RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.aliyun.com/g' /etc/apk/repositories \
    && apk add --update --no-cache bash gettext sudo nginx zip

COPY --from=build2 /app/build.zip /app/build.zip
COPY --from=build1 /app/docs.zip /app/docs.zip

RUN cd /app && unzip build.zip && rm -rf build.zip
RUN mkdir /app/docs && mv /app/docs.zip /app/docs/docs.zip && cd /app/docs && unzip docs.zip && rm -rf docs.zip

ENTRYPOINT ["/app/sbin/run.sh"]
