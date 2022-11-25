alter table am_workflow_task
    modify deploy_app_id bigint default 0 null comment '发起的新 DeployApp ID';

alter table am_workflow_task
    modify deploy_app_unit_id varchar(64) default '' null comment '发起的新 DeployApp ID 归属单元';

alter table am_workflow_task
    modify deploy_app_namespace_id varchar(64) default '' null comment '发起的新 DeployApp ID 归属 Namespace';

alter table am_workflow_task
    modify deploy_app_stage_id varchar(64) default '' null comment '发起的新 DeployApp ID 归属 Stage';

alter table am_workflow_task
    collate = utf8mb4_general_ci;

alter table am_workflow_task
    add deploy_workflow_instance_id bigint null comment '发起的新 WorkflowInstance ID';

create index idx_deploy_workflow_instance_id
    on am_workflow_task (deploy_workflow_instance_id);
