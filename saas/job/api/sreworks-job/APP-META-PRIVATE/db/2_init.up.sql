CREATE TABLE IF NOT EXISTS `sreworks_stream_job` (
       `id` bigint NOT NULL AUTO_INCREMENT,
       `alias` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
       `app_id` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
       `creator` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
       `description` longtext COLLATE utf8mb4_general_ci,
       `execution_pool_id` bigint DEFAULT NULL,
       `gmt_create` bigint DEFAULT NULL,
       `gmt_modified` bigint DEFAULT NULL,
       `job_type` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
       `name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
       `operator` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
       `options` longtext COLLATE utf8mb4_general_ci,
       `tags` longtext COLLATE utf8mb4_general_ci,
       `status` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
       PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;



CREATE TABLE IF NOT EXISTS `sreworks_stream_job_block` (
     `id` bigint NOT NULL AUTO_INCREMENT,
     `app_id` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
     `block_type` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
     `creator` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
     `data` longtext COLLATE utf8mb4_general_ci,
     `gmt_create` bigint DEFAULT NULL,
     `gmt_modified` bigint DEFAULT NULL,
     `name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
     `operator` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
     `stream_job_id` bigint DEFAULT NULL,
     PRIMARY KEY (`id`),
     UNIQUE KEY `stream_job_index` (`stream_job_id`,`name`,`block_type`) USING BTREE,
     KEY `stream_job_id` (`stream_job_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


CREATE TABLE IF NOT EXISTS `sreworks_stream_job_runtime` (
       `id` bigint NOT NULL AUTO_INCREMENT,
       `creator` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
       `description` longtext COLLATE utf8mb4_general_ci,
       `gmt_create` bigint DEFAULT NULL,
       `gmt_modified` bigint DEFAULT NULL,
       `name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
       `operator` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
       `settings` longtext COLLATE utf8mb4_general_ci,
       `tags` longtext COLLATE utf8mb4_general_ci,
       PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT IGNORE INTO `sreworks_stream_job_runtime` (`id`, `creator`, `description`, `gmt_create`, `gmt_modified`, `name`, `operator`, `settings`, `tags`) VALUES ('1',null,null,'1683326290582','1683326290582','flink-ml',null,'{
	"flinkImage": "sreworks-registry.cn-beijing.cr.aliyuncs.com/mirror/flink:1.15.2-stream4-scala_2.12-java11-python3.8-pyflink",
	"additionalDependencies": [{
        "filename": "flink-ml-uber-2.2-SNAPSHOT.jar"
    }],
	"entryClass": "org.apache.flink.client.python.PythonDriver",
	"jarUri": "flink-python_2.12-1.15.2.jar",
	"flinkVersion": "1.15",
	"pyArchives": "venv5-2.2.zip",
	"pyClientExecutable": "/venv/bin/python"
}',null);
INSERT IGNORE INTO `sreworks_stream_job_runtime` (`id`, `creator`, `description`, `gmt_create`, `gmt_modified`, `name`, `operator`, `settings`, `tags`) VALUES ('2',null,null,'1683327874331','1683327874331','default',null,'{
	"flinkImage": "sreworks-registry.cn-beijing.cr.aliyuncs.com/mirror/flink:1.15.2-stream4-scala_2.12-java11-python3.8-pyflink",
	"additionalDependencies": [],
	"entryClass": "org.apache.flink.client.python.PythonDriver",
	"jarUri": "flink-python_2.12-1.15.2.jar",
	"flinkVersion": "1.15",
	"pyArchives": "",
	"pyClientExecutable": ""
}',null);

