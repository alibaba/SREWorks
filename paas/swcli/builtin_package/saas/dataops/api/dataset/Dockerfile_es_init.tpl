FROM {{ MIGRATE_IMAGE }}
COPY ./sbin/es_index_init.sh /es_index_init.sh
RUN chmod +x /es_index_init.sh
ENTRYPOINT ["/es_index_init.sh"]
