alter table am_workflow_task
    add task_outputs longtext default null COMMENT 'Workflow 任务节点输出 (JSONArray 字符串)';

alter table am_workflow_task
    add task_inputs longtext default null COMMENT 'Workflow 任务节点输入 (JSONArray 字符串)';