FROM {{ PYTHON2_IMAGE }} AS release
COPY ./build/config /app

RUN apk add --update --no-cache gcc libc-dev \
    && pip install -i http://mirrors.aliyun.com/pypi/simple --trusted-host mirrors.aliyun.com -r /app/requirements.txt

RUN chmod +x /app/*.sh \
    && apk add --update --no-cache bash \
    && apk add --update --no-cache gettext \
    && rm -rf /root/.cache

WORKDIR /app
EXPOSE 80
ENV PYTHONPATH=/app
ENTRYPOINT ["/app/start.sh"]