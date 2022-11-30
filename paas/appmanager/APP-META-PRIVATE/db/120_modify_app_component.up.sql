alter table am_app_component
    modify component_type varchar(96) not null comment '组件类型';

alter table am_component_package_task
    modify component_type varchar(96) null comment '组件类型';

alter table am_component_package
    modify component_type varchar(96) null comment '组件类型';

alter table am_helm_meta
    modify component_type varchar(96) null comment '组件类型';

alter table am_k8s_micro_service_meta
    modify component_type varchar(96) null comment '组件类型';

alter table am_rt_component_instance
    modify component_type varchar(96) null comment '组件类型';
