#!/bin/bash

JOB_ROOT=$(cd `dirname $0`; pwd)

while [[ "$(curl -s -o /dev/null -w '%{http_code}\n' http://$VVP_ENDPOINT/swagger)" != "200" ]]
do
    sleep 10
done


###### CREATE UDF ARTIFACT
echo "============CREATE UDF ARTIFACT============"
curl http://${VVP_ENDPOINT}/sql/v1beta1/namespaces/${VVP_WORK_NS}/udfartifacts \
    -X POST \
    -H 'Content-Type: application/json' \
    -d '{"name": "namespaces/'${VVP_WORK_NS}'/udfartifacts/'${UDF_ARTIFACT_NAME}'"}'


###### REGISTER UDF ARTIFACT
echo "============REGISTER UDF ARTIFACT============"
curl http://${VVP_ENDPOINT}/sql/v1beta1/namespaces/${VVP_WORK_NS}/udfartifacts/${UDF_ARTIFACT_NAME} \
    -X PUT \
    -H 'Content-Type: application/json' \
    -d '{
        "name": "namespaces/'${VVP_WORK_NS}'/udfartifacts/'${UDF_ARTIFACT_NAME}'",
        "jarUrl": "s3://vvp/artifacts/namespaces/'${VVP_WORK_NS}'/udfs/'${UDF_ARTIFACT_JAR}'"
    }'


###### REGISTER UDF FUNCTION
echo "============REGISTER UDF FUNCTION============"
curl http://${VVP_ENDPOINT}/sql/v1beta1/namespaces/${VVP_WORK_NS}/sqlscripts:execute-multi?stopOnError=false \
    -X POST \
    -H 'Content-Type: application/json' \
    -d '[
        "CREATE FUNCTION IF NOT EXISTS `NoDataAlarm` AS '\''com.elasticsearch.cloud.monitor.metric.alarm.blink.udaf.NoDataAlarm'\''",
        "CREATE FUNCTION IF NOT EXISTS `ExtractTimeUdf` AS '\''com.elasticsearch.cloud.monitor.metric.common.blink.udf.ExtractTimeUdf'\''",
        "CREATE FUNCTION IF NOT EXISTS `TimeDelay` AS '\''com.elasticsearch.cloud.monitor.metric.alarm.blink.udf.TimeDelay'\''",
        "CREATE FUNCTION IF NOT EXISTS `ParseContentUdtf` AS '\''com.elasticsearch.cloud.monitor.metric.common.blink.udtf.ParseContentUdtf'\''",
        "CREATE FUNCTION IF NOT EXISTS `DurationAlarm` AS '\''com.elasticsearch.cloud.monitor.metric.alarm.blink.udtf.DurationAlarm'\''",
        "CREATE FUNCTION IF NOT EXISTS `splitEventList` AS '\''com.elasticsearch.cloud.monitor.metric.alarm.blink.udtf.splitEventList'\''",
        "CREATE FUNCTION IF NOT EXISTS `HealthAlert` AS '\''com.elasticsearch.cloud.monitor.metric.alarm.blink.udtf.HealthAlert'\''",
        "CREATE FUNCTION IF NOT EXISTS `HealthFailure` AS '\''com.elasticsearch.cloud.monitor.metric.alarm.blink.udtf.HealthFailure'\''",
        "CREATE FUNCTION IF NOT EXISTS `PmdbMetricUidUdf` AS '\''com.elasticsearch.cloud.monitor.metric.common.blink.udf.PmdbMetricUidUdf'\''",
        "CREATE FUNCTION IF NOT EXISTS `PmdbMetricInsUidUdf` AS '\''com.elasticsearch.cloud.monitor.metric.common.blink.udf.PmdbMetricInsUidUdf'\''"
    ]'


###### CREATE TABLE
############ health alert
echo "============CREATE TABLE============"
curl http://${VVP_ENDPOINT}/sql/v1beta1/namespaces/${VVP_WORK_NS}/sqlscripts:execute \
   -X POST \
   -H 'Content-Type: application/json' \
   -d '{"statement":"CREATE TABLE `vvp`.`'${VVP_WORK_NS}'`.`source_sreworks_metric_data_kafka` (`uid` VARCHAR,`metricId` INT,`metricName` VARCHAR,`type` VARCHAR,`labels` MAP<VARCHAR, VARCHAR>,`ts` BIGINT,`value` FLOAT,`proc_time` AS PROCTIME()) COMMENT '\''指标数据源表'\'' WITH ('\''connector'\'' = '\''kafka'\'','\''properties.bootstrap.servers'\'' = '\''http://'${KAFKA_URL}''\'','\''topic'\'' = '\''sreworks-dataops-metric-data'\'','\''properties.group.id'\'' = '\''sreworks-dataops-metric-flink-group'\'','\''scan.startup.mode'\'' = '\''latest-offset'\'','\''value.fields-include'\'' = '\''ALL'\'','\''format'\'' = '\''json'\'','\''json.map-null-key.mode'\'' = '\''FAIL'\'','\''json.fail-on-missing-field'\'' = '\''true'\'','\''json.ignore-parse-errors'\'' = '\''false'\'');"}'
#   -d '{"statement":"CREATE TABLE `vvp`.`'${VVP_WORK_NS}'`.`source_sreworks_metric_data_kafka` (`uid` VARCHAR,`metricId` INT,`metricName` VARCHAR,`type` VARCHAR,`labels` MAP<VARCHAR, VARCHAR>,`ts` BIGINT,`value` FLOAT,`msg_timestamp` TIMESTAMP(3) METADATA FROM '\''timestamp'\'',WATERMARK FOR `msg_timestamp` AS `msg_timestamp` - INTERVAL '\''1'\'' MINUTE) COMMENT '\''指标数据源表'\'' WITH ('\''connector'\'' = '\''kafka'\'','\''properties.bootstrap.servers'\'' = '\''http://'${KAFKA_URL}''\'','\''topic'\'' = '\''sreworks-dataops-metric-data'\'','\''properties.group.id'\'' = '\''sreworks-dataops-metric-flink-group'\'','\''scan.startup.mode'\'' = '\''latest-offset'\'','\''value.fields-include'\'' = '\''ALL'\'','\''format'\'' = '\''json'\'','\''json.map-null-key.mode'\'' = '\''FAIL'\'','\''json.fail-on-missing-field'\'' = '\''true'\'','\''json.ignore-parse-errors'\'' = '\''false'\'');"}'

curl http://${VVP_ENDPOINT}/sql/v1beta1/namespaces/${VVP_WORK_NS}/sqlscripts:execute \
   -X POST \
   -H 'Content-Type: application/json' \
   -d '{"statement":"CREATE TABLE `vvp`.`'${VVP_WORK_NS}'`.`dim_health_def_mysql` (id INT, name VARCHAR, category VARCHAR, app_id VARCHAR, app_name VARCHAR, app_component_name VARCHAR, metric_id INT, failure_ref_incident_id INT, ex_config VARCHAR, PRIMARY KEY (metric_id) NOT ENFORCED) COMMENT '\''告警定义维度表'\''WITH ('\''connector'\'' = '\''jdbc'\'', '\''driver'\'' = '\''org.mariadb.jdbc.Driver'\'', '\''password'\'' = '\'''${DATA_DB_PASSWORD}''\'', '\''table-name'\'' = '\''common_definition'\'', '\''url'\'' = '\''jdbc:mysql://'${DATA_DB_HOST}':'${DATA_DB_PORT}'/'${DATA_DB_HEALTH_NAME}'?useUnicode=true&characterEncoding=utf-8&useSSL=false'\'', '\''username'\'' = '\'''${DATA_DB_USER}''\'', '\''lookup.cache.max-rows'\'' = '\''1000'\'', '\''lookup.cache.ttl'\'' = '\''600s'\'');"}'

curl http://${VVP_ENDPOINT}/sql/v1beta1/namespaces/${VVP_WORK_NS}/sqlscripts:execute \
   -X POST \
   -H 'Content-Type: application/json' \
   -d '{"statement":"CREATE TABLE `vvp`.`'${VVP_WORK_NS}'`.`sink_health_alert_instance_mysql` (`def_id` INT, `app_instance_id` VARCHAR, `app_component_instance_id` VARCHAR, `metric_instance_id` VARCHAR, `metric_instance_labels` VARCHAR, `gmt_occur` TIMESTAMP(3), `source` VARCHAR, `level` VARCHAR, `content` VARCHAR) COMMENT '\''告警记录表'\''WITH ('\''connector'\'' = '\''jdbc'\'', '\''driver'\'' = '\''org.mariadb.jdbc.Driver'\'', '\''password'\'' = '\'''${DATA_DB_PASSWORD}''\'', '\''table-name'\'' = '\''alert_instance'\'', '\''url'\'' = '\''jdbc:mysql://'${DATA_DB_HOST}':'${DATA_DB_PORT}'/'${DATA_DB_HEALTH_NAME}'?useUnicode=true&characterEncoding=utf-8&ueSSL=false'\'', '\''username'\'' = '\'''${DATA_DB_USER}''\'');"}'

curl http://${VVP_ENDPOINT}/sql/v1beta1/namespaces/${VVP_WORK_NS}/sqlscripts:execute \
   -X POST \
   -H 'Content-Type: application/json' \
   -d '{"statement":"CREATE TABLE `vvp`.`'${VVP_WORK_NS}'`.`print_alert_instance` (defId INT) WITH ('\''connector'\'' = '\''print'\'')"}'

curl http://${VVP_ENDPOINT}/sql/v1beta1/namespaces/${VVP_WORK_NS}/sqlscripts:execute \
   -X POST \
   -H 'Content-Type: application/json' \
   -d '{"statement":"CREATE VIEW `vvp`.`'${VVP_WORK_NS}'`.`metric_data_alert_rule_view` (`uid`,`metricId`,`metricName`,`type`,`labels`,`ts`,`value`,`def_id`,`def_name`,`app_id`,`app_name`,`app_component_name`,`ex_config`) COMMENT '\''指标数值告警定义视图'\'' AS SELECT `t1`.`uid`, `t1`.`metricId`, `t1`.`metricName`, `t1`.`type`, `t1`.`labels`, `t1`.`ts`, `t1`.`value`, `t2`.`id` AS `def_id`, `t2`.`name` AS `def_name`, `t2`.`app_id`, `t2`.`app_name`, `t2`.`app_component_name`, `t2`.`ex_config` FROM `vvp`.`'${VVP_WORK_NS}'`.`source_sreworks_metric_data_kafka` AS t1 INNER JOIN `vvp`.`'${VVP_WORK_NS}'`.`dim_health_def_mysql` FOR SYSTEM_TIME AS OF `t1`.`proc_time` AS t2 ON t2.metric_id=t1.metricId WHERE t2.category='\''alert'\'';"}'


############ health failure
curl http://${VVP_ENDPOINT}/sql/v1beta1/namespaces/${VVP_WORK_NS}/sqlscripts:execute \
   -X POST \
   -H 'Content-Type: application/json' \
   -d '{"statement":"CREATE TABLE `vvp`.`'${VVP_WORK_NS}'`.`source_sreworks_health_incident_instance_kafka` (`id` BIGINT, `defId` INT, `appInstanceId` VARCHAR, `appComponentInstanceId` VARCHAR, `gmtOccur` BIGINT, `gmtRecovery` BIGINT, `cause` VARCHAR, `proc_time` AS PROCTIME()) COMMENT '\''异常实例数据源表'\'' WITH ('\''connector'\'' = '\''kafka'\'', '\''format'\'' = '\''json'\'', '\''json.fail-on-missing-field'\'' = '\''true'\'', '\''json.ignore-parse-errors'\'' = '\''false'\'', '\''json.map-null-key.mode'\'' = '\''FAIL'\'', '\''properties.bootstrap.servers'\'' = '\''http://'${KAFKA_URL}''\'', '\''properties.group.id'\'' = '\''sreworks-health-incident-flink-group'\'', '\''scan.startup.mode'\'' = '\''latest-offset'\'', '\''topic'\'' = '\''sreworks-health-incident'\'', '\''value.fields-include'\'' = '\''ALL'\'');"}'
#   -d '{"statement":"CREATE TABLE `vvp`.`'${VVP_WORK_NS}'`.`source_sreworks_health_incident_instance_kafka` (`id` BIGINT, `defId` INT, `appInstanceId` VARCHAR, `appComponentInstanceId` VARCHAR, `gmtOccur` BIGINT, `gmtRecovery` BIGINT, `cause` VARCHAR, `msg_timestamp` TIMESTAMP(3) METADATA FROM '\''timestamp'\'', WATERMARK FOR `msg_timestamp` AS `msg_timestamp` - INTERVAL '\''1'\'' MINUTE) COMMENT '\''异常实例数据源表'\'' WITH ('\''connector'\'' = '\''kafka'\'', '\''format'\'' = '\''json'\'', '\''json.fail-on-missing-field'\'' = '\''true'\'', '\''json.ignore-parse-errors'\'' = '\''false'\'', '\''json.map-null-key.mode'\'' = '\''FAIL'\'', '\''properties.bootstrap.servers'\'' = '\''http://'${KAFKA_URL}''\'', '\''properties.group.id'\'' = '\''sreworks-health-incident-flink-group'\'', '\''scan.startup.mode'\'' = '\''latest-offset'\'', '\''topic'\'' = '\''sreworks-health-incident'\'', '\''value.fields-include'\'' = '\''ALL'\'');"}'

curl http://${VVP_ENDPOINT}/sql/v1beta1/namespaces/${VVP_WORK_NS}/sqlscripts:execute \
   -X POST \
   -H 'Content-Type: application/json' \
   -d '{"statement":"CREATE TABLE `vvp`.`'${VVP_WORK_NS}'`.`print_failure_instance` (defId INT) WITH ('\''connector'\'' = '\''print'\'')"}'

curl http://${VVP_ENDPOINT}/sql/v1beta1/namespaces/${VVP_WORK_NS}/sqlscripts:execute \
   -X POST \
   -H 'Content-Type: application/json' \
   -d '{"statement":"CREATE VIEW `vvp`.`'${VVP_WORK_NS}'`.`health_failure_incident_view` (`incidentInstanceId`, `appInstanceId`, `appComponentInstanceId`, `gmtOccur`, `gmtRecovery`, `cause`, `failure_def_id`, `app_name`, `ex_config`) COMMENT '\''故障异常关联视图'\'' AS SELECT `t1`.`id` AS `incidentInstanceId`, `t1`.`appInstanceId`, `t1`.`appComponentInstanceId`, `t1`.`gmtOccur`, `t1`.`gmtRecovery`, `t1`.cause, `t2`.`id` AS `failure_def_id`, `t2`.`app_name`, `t2`.`ex_config` FROM `vvp`.`'${VVP_WORK_NS}'`.`source_sreworks_health_incident_instance_kafka` AS `t1` INNER JOIN `vvp`.`'${VVP_WORK_NS}'`.`dim_health_def_mysql` FOR SYSTEM_TIME AS OF `t1`.`proc_time` AS `t2` ON `t2`.`failure_ref_incident_id` = `t1`.`defId` WHERE `t2`.`category` = '\''failure'\'';"}'


###### CREATE DEPLOYMENT TARGET
echo "============CREATE DEPLOYMENT TARGET============"
envsubst < ${JOB_ROOT}/vvp-resources/deployment_target.yaml.tpl > ${JOB_ROOT}/vvp-resources/deployment_target.yaml
curl http://${VVP_ENDPOINT}/api/v1/namespaces/${VVP_WORK_NS}/deployment-targets \
    -X POST \
    -H "Content-Type: application/yaml" \
    -H "Accept: application/yaml" \
    --data-binary @/app/sbin/vvp-resources/deployment_target.yaml


###### CREATE SESSION CLUSTER
echo "============CREATE SESSION CLUSTER============"
envsubst < ${JOB_ROOT}/vvp-resources/session_cluster.yaml.tpl > ${JOB_ROOT}/vvp-resources/session_cluster.yaml
curl http://${VVP_ENDPOINT}/api/v1/namespaces/${VVP_WORK_NS}/sessionclusters \
    -X POST \
    -H "Content-Type: application/yaml" \
    -H "Accept: application/yaml" \
    --data-binary @/app/sbin/vvp-resources/session_cluster.yaml


###### CHECK SESSION CLUSTER STATUS
echo "============CHECK SESSION CLUSTER STATUS============"
session_cluster_state=""
while [[ "$session_cluster_state" != "RUNNING" ]]
do
    echo "check session cluster state: "$session_cluster_state", wait..."
    sleep 10
    session_cluster_info=$(curl http://${VVP_ENDPOINT}/api/v1/namespaces/${VVP_WORK_NS}/sessionclusters/sreworks-session-cluster -X GET)
    session_cluster_state=$(echo $session_cluster_info | jq -r ".status.state")
done
echo "session cluster is "$session_cluster_state


###### CREATE DEPLOYMENT
echo "============CREATE ALERT DEPLOYMENT============"
envsubst < ${JOB_ROOT}/vvp-resources/deployment_alert.yaml.tpl > ${JOB_ROOT}/vvp-resources/deployment_alert.yaml
curl http://${VVP_ENDPOINT}/api/v1/namespaces/${VVP_WORK_NS}/deployments \
    -X POST \
    -H "Content-Type: application/yaml" \
    -H "Accept: application/yaml" \
    --data-binary @/app/sbin/vvp-resources/deployment_alert.yaml

echo "============CREATE FAILURE DEPLOYMENT============"
envsubst < ${JOB_ROOT}/vvp-resources/deployment_failure.yaml.tpl > ${JOB_ROOT}/vvp-resources/deployment_failure.yaml
curl http://${VVP_ENDPOINT}/api/v1/namespaces/${VVP_WORK_NS}/deployments \
    -X POST \
    -H "Content-Type: application/yaml" \
    -H "Accept: application/yaml" \
    --data-binary @/app/sbin/vvp-resources/deployment_failure.yaml
