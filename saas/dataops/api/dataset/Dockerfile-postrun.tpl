FROM registry.cn-hangzhou.aliyuncs.com/alisre/sw-postrun:lastest
COPY ./APP-META-PRIVATE/postrun /app/postrun
RUN apk update 