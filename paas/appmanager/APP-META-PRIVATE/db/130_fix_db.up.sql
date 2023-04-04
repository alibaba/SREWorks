alter table am_rt_component_instance
    add deploy_app_id bigint default 0 null comment '关联的应用部署单 ID';

alter table am_rt_component_instance
    add deploy_component_id bigint default 0 null comment '关联的组件部署单 ID';

create index idx_deploy_app_id
    on am_rt_component_instance (deploy_app_id);

create index idx_deploy_component_id
    on am_rt_component_instance (deploy_component_id);

alter table am_rt_app_instance
    add deploy_app_id bigint default 0 null comment '关联的应用部署单 ID';

create index idx_deploy_app_id
    on am_rt_app_instance (deploy_app_id);