alter table am_plugin_definition
    modify plugin_version varchar(32) not null comment 'Plugin 版本';

alter table am_plugin_definition
    change plugin_extra plugin_schema longtext null comment 'Plugin Schema';

alter table am_plugin_definition
    add plugin_kind varchar(32) null comment 'Plugin 类型' after gmt_modified;

create index idx_plugin_kind
    on am_plugin_definition (plugin_kind);
