REPLACE INTO `app` (
  `id`, `annotations`, `creator`, `description`, `detail`, `display`, `gmt_create`, `gmt_modified`, `labels`, `last_modifier`, `name`, `team_id`
) VALUES (
  1,'{}','999999999','','{\"memory\":\"0\",\"resource\":{\"limits\":{\"cpu\":\"1\",\"memory\":\"1G\"},\"requests\":{\"cpu\":\"1\",\"memory\":\"1G\"}},\"cpu\":\"0\"}',1,1646116254,1646116254,'{}','999999999','demoApp',1
  );
REPLACE INTO `app_market_endpoint` (
    id, config, creator, gmt_create, gmt_modified, last_modifier, name
)
VALUES(
    1, '{"endpoint":"oss-cn-beijing.aliyuncs.com","endpointType":"oss","remoteBucket":"sreworks","alias":"企业公共应用市场","remoteStorePath":"/enterprise-app-markets","isUpload":false}', '999999999', 1667168168, 1667168168, '999999999', 'default'
);
