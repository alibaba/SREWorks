CREATE TABLE IF NOT EXISTS `am_app_component`
(
    `id`             bigint      NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `gmt_create`     datetime DEFAULT NULL COMMENT '创建时间',
    `gmt_modified`   datetime DEFAULT NULL COMMENT '最后修改时间',
    `app_id`         varchar(64) NOT NULL COMMENT '应用 ID',
    `component_type` varchar(32) NOT NULL COMMENT '组件类型',
    `component_name` varchar(64) NOT NULL COMMENT '组件名称',
    `config`         longtext    NOT NULL COMMENT '配置内容',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_app_id_component_type_name` (`app_id`, `component_type`, `component_name`) USING BTREE,
    KEY `idx_gmt_create` (`gmt_create`),
    KEY `idx_gmt_modified` (`gmt_modified`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='应用组件绑定表';