FROM {{ ALPINE_IMAGE }} AS release
ARG TAG
ARG OSSUTIL_URL
COPY ./APP-META-PRIVATE/deploy-config/config.js.tpl /app/config.js.tpl
COPY ./APP-META-PRIVATE/deploy-config/ /app/deploy-config/
COPY ./sbin/ /app/sbin/
RUN chmod -R +x /app/sbin/
RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.aliyun.com/g' /etc/apk/repositories \
    && apk add --update --no-cache curl vim bash gettext sudo wget libc6-compat nginx \
    && wget -O /ossutil ${OSSUTIL_URL} \
    && chmod +x /ossutil \
    && /app/sbin/release.sh $TAG \
    && apk del wget libc6-compat \
    && rm -f /ossutil \
    && rm -rf /app/sbin/build.sh \
    && rm -rf /app/sbin/release.sh
ENTRYPOINT ["/app/sbin/run.sh"]
