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