alter table am_dynamic_script
    add env_id varchar(32) null comment '环境 ID';

alter table am_dynamic_script
    drop key uk_kind_name;

alter table am_dynamic_script
    add constraint uk_kind_name
        unique (kind, name, env_id);

alter table am_dynamic_script_history
    add env_id varchar(32) null comment '环境 ID';