alter table am_rt_app_instance
    modify version varchar(64) not null comment '应用实例版本号 (最近一次应用包部署)';

alter table am_rt_app_instance
    modify latest_version varchar(64) null comment '应用实例可升级最新版本号';

alter table am_rt_app_instance_history
    modify version varchar(64) not null comment '应用实例版本号';

alter table am_rt_component_instance
    modify version varchar(64) not null comment '应用实例版本号 (最近一次应用包部署)';

alter table am_rt_component_instance_history
    modify version varchar(64) not null comment '组件实例版本号';

