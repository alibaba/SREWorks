FROM python:2.7.18-alpine

RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.aliyun.com/g' /etc/apk/repositories
RUN apk update

RUN apk add --update --no-cache gcc libc-dev