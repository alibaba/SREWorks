ALTER TABLE `am_plugin_frontend`
    CHANGE `plugin_name` `plugin_name` VARCHAR(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Plugin 唯一标识',
    CHANGE `plugin_version` `plugin_version` VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Plugin 版本 (SemVer)',
    CHANGE `name` `name` VARCHAR(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '名称',
    CHANGE `config` `config` LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '页面配置';