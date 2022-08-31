FROM {{ PYTHON3_IMAGE }}

RUN sed -i 's/dl-cdn.alpinelinux.org/{{ APK_REPO_DOMAIN }}/g' /etc/apk/repositories

RUN apk add mariadb-dev python3-dev gcc musl-dev

RUN pip config set global.index-url {{PYTHON_PIP}} && pip config set global.trusted-host {{PYTHON_PIP_DOMAIN}}

RUN pip install mysqlclient

COPY ./APP-META-PRIVATE/init /run

ENTRYPOINT ["python", "/run/init-cluster.py"]