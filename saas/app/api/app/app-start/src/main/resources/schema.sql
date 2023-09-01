-- sreworks_meta.app_market_endpoint definition

CREATE TABLE IF NOT EXISTS `app_market_endpoint` (
       `id` bigint NOT NULL AUTO_INCREMENT,
       `config` text COLLATE utf8mb4_general_ci,
       `creator` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
       `gmt_create` bigint DEFAULT NULL,
       `gmt_modified` bigint DEFAULT NULL,
       `last_modifier` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
       `name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
       PRIMARY KEY (`id`),
       UNIQUE KEY `UK_8mdmrm8qgtigtgvwo3oil1ko7` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `team` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `avatar` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
    `creator` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
    `description` longtext COLLATE utf8mb4_general_ci,
    `gmt_create` bigint DEFAULT NULL,
    `gmt_modified` bigint DEFAULT NULL,
    `last_modifier` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
    `name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
    `visible_scope` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `team_account` (
        `id` bigint NOT NULL AUTO_INCREMENT,
        `creator` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
        `description` text COLLATE utf8mb4_general_ci,
        `detail` text COLLATE utf8mb4_general_ci,
        `gmt_create` bigint DEFAULT NULL,
        `gmt_modified` bigint DEFAULT NULL,
        `last_modifier` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
        `name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
        `team_id` bigint DEFAULT NULL,
        `type` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
        PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `team_registry` (
     `id` bigint NOT NULL AUTO_INCREMENT,
     `auth` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
     `creator` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
     `description` longtext COLLATE utf8mb4_general_ci,
     `gmt_create` bigint DEFAULT NULL,
     `gmt_modified` bigint DEFAULT NULL,
     `last_modifier` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
     `name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
     `team_id` bigint DEFAULT NULL,
     `url` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
     PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `team_repo` (
     `id` bigint NOT NULL AUTO_INCREMENT,
     `ci_account` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
     `ci_token` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
     `creator` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
     `description` longtext COLLATE utf8mb4_general_ci,
     `gmt_create` bigint DEFAULT NULL,
     `gmt_modified` bigint DEFAULT NULL,
     `last_modifier` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
     `name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
     `team_id` bigint DEFAULT NULL,
     `url` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
     PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `team_user` (
     `id` bigint NOT NULL AUTO_INCREMENT,
     `creator` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
     `gmt_access` bigint DEFAULT NULL,
     `gmt_create` bigint DEFAULT NULL,
     `gmt_modified` bigint DEFAULT NULL,
     `is_concern` int DEFAULT NULL,
     `last_modifier` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
     `role` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
     `team_id` bigint DEFAULT NULL,
     `user` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
     PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `sreworks_file` (
     `id` bigint NOT NULL AUTO_INCREMENT,
     `alias` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
     `category` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
     `description` longtext COLLATE utf8mb4_general_ci,
     `file_id` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
     `gmt_create` bigint DEFAULT NULL,
     `gmt_modified` bigint DEFAULT NULL,
     `name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
     `operator` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
     `type` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
     PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` longtext COLLATE utf8mb4_general_ci,
  `gmt_create` bigint DEFAULT NULL,
  `gmt_modified` bigint DEFAULT NULL,
  `name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `operator` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `cluster_resource` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `account_id` bigint DEFAULT NULL,
    `cluster_id` bigint DEFAULT NULL,
    `creator` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
    `description` longtext COLLATE utf8mb4_general_ci,
    `gmt_create` bigint DEFAULT NULL,
    `gmt_modified` bigint DEFAULT NULL,
    `instance_name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
    `last_modifier` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
    `name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
    `type` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
    `usage_detail` longtext COLLATE utf8mb4_general_ci,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `cluster` (
   `id` bigint NOT NULL AUTO_INCREMENT,
   `account_id` bigint DEFAULT NULL,
   `cluster_name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
   `creator` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
   `description` longtext COLLATE utf8mb4_general_ci,
   `gmt_create` bigint DEFAULT NULL,
   `gmt_modified` bigint DEFAULT NULL,
   `kubeconfig` longtext COLLATE utf8mb4_general_ci,
   `last_modifier` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
   `name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
   `team_id` bigint DEFAULT NULL,
   PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `action` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `gmt_create` bigint DEFAULT NULL,
  `gmt_modified` bigint DEFAULT NULL,
  `operator` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `target_type` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `target_value` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `app` (
   `id` bigint NOT NULL AUTO_INCREMENT,
   `annotations` longtext COLLATE utf8mb4_general_ci,
   `creator` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
   `description` text COLLATE utf8mb4_general_ci,
   `detail` text COLLATE utf8mb4_general_ci,
   `display` bigint DEFAULT NULL,
   `gmt_create` bigint DEFAULT NULL,
   `gmt_modified` bigint DEFAULT NULL,
   `labels` longtext COLLATE utf8mb4_general_ci,
   `last_modifier` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
   `name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
   `team_id` bigint DEFAULT NULL,
   PRIMARY KEY (`id`),
   UNIQUE KEY `UK_88vfgccvckwwip06k7tpf8uk3` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;