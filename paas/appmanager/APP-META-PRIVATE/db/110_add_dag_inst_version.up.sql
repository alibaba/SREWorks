ALTER TABLE `tc_dag_inst` ADD COLUMN `version` INT(11) NULL DEFAULT 0 AFTER `father_dag_inst_node_id`;

ALTER TABLE `tc_dag_inst` ADD INDEX `idx_dag_id_gmt_create` (`dag_id`,`gmt_create`);

ALTER TABLE `tc_dag_inst` ADD INDEX `idx_dag_id_status_gmt_create` (`dag_id`,`status`,`gmt_create`);