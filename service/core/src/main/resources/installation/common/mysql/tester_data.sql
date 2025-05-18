-- @formatter:off

-- ----------------------------
-- Table data for id_config
-- ----------------------------
DELETE FROM `id_config` WHERE `pk` IN (183001,183002,183003);
INSERT INTO `id_config` (`pk`, `biz_key`, `format`, `prefix`, `date_format`, `seq_length`, `mode`, `scope`, `tenant_id`, `max_id`, `step`, `create_date`, `last_modified_date`) VALUES ('183001', 'task', 'PREFIX_SEQ', 'AT', NULL, 8, 'DB', 'TENANT', -1, 0, 1, '2022-07-21 17:21:28', '2022-07-21 17:21:28');
INSERT INTO `id_config` (`pk`, `biz_key`, `format`, `prefix`, `date_format`, `seq_length`, `mode`, `scope`, `tenant_id`, `max_id`, `step`, `create_date`, `last_modified_date`) VALUES ('183002', 'funcCase', 'PREFIX_SEQ', 'AC', NULL, 9, 'DB', 'TENANT', -1, 0, 1, '2022-07-21 17:21:28', '2022-07-21 17:21:28');
INSERT INTO `id_config` (`pk`, `biz_key`, `format`, `prefix`, `date_format`, `seq_length`, `mode`, `scope`, `tenant_id`, `max_id`, `step`, `create_date`, `last_modified_date`) VALUES ('183003', 'nodeHostname', 'PREFIX_SEQ', 'AN', NULL, 9, 'DB', 'PLATFORM', -1, 0, 1, '2022-07-21 17:21:28', '2022-07-21 17:21:28');
