alter table am_workflow_instance
    add workflow_graph longtext null comment 'Workflow Graph 信息存储';

alter table am_workflow_task
    add task_name varchar(64) default '' not null comment '任务名称' after task_type;

create index idx_task_name
    on am_workflow_task (task_name);