CREATE TABLE `am_app_version`
(
    `id`                 bigint       NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `gmt_create`         datetime     NULL COMMENT '创建时间',
    `gmt_modified`       datetime     NULL COMMENT '最后修改时间',
    `app_id`             varchar(64)  NULL COMMENT '应用 ID (为空则全局)',
    `version`            varchar(64)  NULL COMMENT '版本号',
    `version_label`      varchar(128) NULL COMMENT '版本标签',
    `version_properties` longtext COMMENT '版本属性',
    PRIMARY KEY (`id`),
    KEY `idx_gmt_create` (`gmt_create`),
    KEY `idx_gmt_modified` (`gmt_modified`),
    KEY `idx_app_id` (`app_id`),
    KEY `version` (`version`),
    KEY `version_label` (`version_label`)
) DEFAULT CHARACTER SET = utf8mb4 COMMENT ='应用版本表';