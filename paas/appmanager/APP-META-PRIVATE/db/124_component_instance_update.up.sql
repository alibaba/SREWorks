alter table am_rt_component_instance
    add component_schema longtext null comment '当前组件的 ComponentSchema YAML 存储' after conditions;
