alter table am_workflow_instance
    collate = utf8mb4_general_ci;

alter table am_workflow_instance
    add category varchar(32) null comment 'Workflow 分类' after app_id;

create index idx_category
    on am_workflow_instance (category);
