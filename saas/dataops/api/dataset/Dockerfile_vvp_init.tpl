FROM {{ MIGRATE_IMAGE }}
COPY ./sbin/vvp_init.sh /vvp_init.sh
RUN chmod +x /vvp_init.sh
ENTRYPOINT ["/vvp_init.sh"]