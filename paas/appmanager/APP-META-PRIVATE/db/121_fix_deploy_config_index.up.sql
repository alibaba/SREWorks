alter table am_deploy_config collate = utf8mb4_general_ci;
alter table am_deploy_config_history collate = utf8mb4_general_ci;

alter table am_deploy_config modify type_id varchar(191) not null comment '类型 ID';
alter table am_deploy_config_history modify type_id varchar(191) not null comment '类型 ID';
