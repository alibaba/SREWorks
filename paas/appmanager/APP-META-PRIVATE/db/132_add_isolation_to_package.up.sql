alter table am_app_package_task
    add namespace_id varchar(64) default 'default' null comment '隔离 Namespace ID';

alter table am_app_package_task
    add stage_id varchar(64) default 'pre' null comment '隔离 Stage ID';

create index idx_namespace_stage
    on am_app_package_task (namespace_id, stage_id);

alter table am_app_package
    add namespace_id varchar(64) default 'default' null comment '隔离 Namespace ID';

alter table am_app_package
    add stage_id varchar(64) default 'pre' null comment '隔离 Stage ID';

create index idx_namespace_stage
    on am_app_package (namespace_id, stage_id);

alter table am_component_package
    add namespace_id varchar(64) default 'default' null comment '隔离 Namespace ID';

alter table am_component_package
    add stage_id varchar(64) default 'pre' null comment '隔离 Stage ID';

create index idx_namespace_stage
    on am_component_package (namespace_id, stage_id);