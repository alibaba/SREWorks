alter table am_plugin_resource
    modify plugin_name varchar(64) not null comment 'Plugin 唯一标识';

alter table am_plugin_resource
    collate = utf8mb4_general_ci;
