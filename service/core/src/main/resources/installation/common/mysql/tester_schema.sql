-- @formatter:off

-- ----------------------------
-- Table structure for id_config
-- ----------------------------
-- Note: Create the table if it does not exist; it needs to be created for independent deployment,
-- while for non-independent deployment, the GM application will create the table.
-- DROP TABLE IF EXISTS `id_config`;
CREATE TABLE IF NOT EXISTS `id_config` (
  `pk` varchar(40) COLLATE utf8mb4_bin NOT NULL,
  `biz_key` varchar(80) COLLATE utf8mb4_bin NOT NULL,
  `format` varchar(16) COLLATE utf8mb4_bin NOT NULL,
  `prefix` varchar(4) COLLATE utf8mb4_bin NOT NULL,
  `date_format` varchar(8) COLLATE utf8mb4_bin DEFAULT NULL,
  `seq_length` int(11) NOT NULL,
  `mode` varchar(8) COLLATE utf8mb4_bin NOT NULL,
  `scope` varchar(16) COLLATE utf8mb4_bin NOT NULL,
  `tenant_id` bigint(20) NOT NULL DEFAULT '-1',
  `max_id` bigint(20) NOT NULL DEFAULT '0',
  `step` bigint(20) NOT NULL,
  `create_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00',
  `last_modified_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00',
  PRIMARY KEY (`pk`) USING BTREE,
  UNIQUE KEY `uidx_biz_key_tenant_id` (`biz_key`,`tenant_id`) USING BTREE,
  KEY `uidx_tenant_id` (`tenant_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for activity
-- ----------------------------
DROP TABLE IF EXISTS `activity`;
CREATE TABLE `activity` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `project_id` bigint(20) DEFAULT NULL COMMENT '项目ID',
  `target_id` bigint(20) NOT NULL COMMENT '活动资源ID',
  `parent_target_id` bigint(20) NOT NULL COMMENT '活动资源父ID',
  `target_type` varchar(20) COLLATE utf8mb4_bin NOT NULL COMMENT '活动资源类型',
  `target_name` varchar(400) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '活动资源名称',
  `main_target_id` bigint(20) DEFAULT NULL COMMENT '活动主资源ID',
  `tenant_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '租户ID',
  `user_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '用户ID',
  `type` varchar(50) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '活动类型',
  `opt_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '活动时间',
  `description` varchar(1600) COLLATE utf8mb4_bin NOT NULL COMMENT '活动描述',
  `detail` varchar(1600) COLLATE utf8mb4_bin NOT NULL COMMENT '活动详细描述',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_target_id` (`target_id`),
  KEY `idx_opt_date` (`opt_date`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_target_type` (`target_type`) USING BTREE,
  KEY `idx_main_target_id` (`main_target_id`) USING BTREE,
  KEY `idx_parent_target_id` (`parent_target_id`) USING BTREE,
  KEY `idx_project_id` (`project_id`),
  FULLTEXT KEY `fx_target_name_detail` (`target_name`,`detail`) /*!50100 WITH PARSER `ngram` */
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='操作日志';

-- ----------------------------
-- Table structure for apis
-- ----------------------------
DROP TABLE IF EXISTS `apis`;
CREATE TABLE `apis` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `project_id` bigint(20) NOT NULL COMMENT '项目ID',
  `source` varchar(10) COLLATE utf8mb4_bin NOT NULL COMMENT '接口来源：添加；导入；自动同步',
  `import_source` varchar(16) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '导入/同步来源类型：Swagger；Yapi',
  `service_id` bigint(20) NOT NULL COMMENT '服务ID',
  `protocol` varchar(16) COLLATE utf8mb4_bin NOT NULL COMMENT '协议',
  `method` varchar(16) COLLATE utf8mb4_bin NOT NULL DEFAULT '0' COMMENT '请求方式 GET|POST|...',
  `endpoint` varchar(800) COLLATE utf8mb4_bin DEFAULT '' COMMENT '接口路径',
  `tags` json DEFAULT NULL COMMENT '标签组',
  `summary` varchar(400) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '名称',
  `description` text COLLATE utf8mb4_bin COMMENT '描述',
  `external_docs` json DEFAULT NULL COMMENT '扩展外部文档',
  `operation_id` varchar(400) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '操作ID',
  `parameters` json DEFAULT NULL COMMENT '请求参数',
  `request_body` json DEFAULT NULL COMMENT '请求体',
  `responses` json DEFAULT NULL COMMENT '响应数据',
  `deprecated` int(11) NOT NULL DEFAULT '0' COMMENT '是否过期',
  `security` json DEFAULT NULL COMMENT '安全需求',
  `current_server` json DEFAULT NULL COMMENT '当前请求服务器',
  `current_server_id` bigint(20) DEFAULT NULL COMMENT '当前服务器ID',
  `servers` json DEFAULT NULL COMMENT '接口配置服务器',
  `extensions` json DEFAULT NULL COMMENT '接口扩展',
  `authentication` json DEFAULT NULL COMMENT '安全信息',
  `assertions` json DEFAULT NULL COMMENT '断言',
  `owner_id` bigint(20) DEFAULT NULL COMMENT '负责人',
  `status` varchar(20) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '接口状态',
  `auth` int(1) NOT NULL DEFAULT '0' COMMENT '授权控制',
  `service_auth` int(1) NOT NULL COMMENT '服务授权控制',
  `secured` int(1) NOT NULL DEFAULT '0' COMMENT '是否有认证头控制',
  `dataset_action_on_eof` varchar(20) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '数据集读到结束时处理策略',
  `dataset_sharing_mode` varchar(20) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '数据集线程共享模式',
  `test_func` int(1) NOT NULL COMMENT '开启功能测试标志',
  `test_func_passed` int(1) DEFAULT NULL COMMENT '功能测试通过标志',
  `test_func_failure_message` varchar(1024) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '功能测试未通过原因',
  `test_perf` int(1) NOT NULL COMMENT '开启性能测试标志',
  `test_perf_passed` int(1) DEFAULT NULL COMMENT '性能测试通过标志',
  `test_perf_failure_message` varchar(1024) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '性能测试未通过原因',
  `test_stability` int(1) NOT NULL COMMENT '开启稳定性测试标志',
  `test_stability_passed` int(1) DEFAULT NULL COMMENT '稳定性测试通过标志',
  `test_stability_failure_message` varchar(1024) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '稳定性测试未通过原因',
  `sync_name` varchar(100) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '同步配置名称',
  `schema_hash` int(11) NOT NULL DEFAULT '0' COMMENT '原模型哈希版本',
  `ext_search_merge` varchar(20) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '扩展搜索河合并列',
  `service_deleted` int(1) NOT NULL COMMENT '服务删除标志',
  `deleted` int(1) NOT NULL DEFAULT '0' COMMENT '删除状态：0-未删除；1-已删除',
  `deleted_by` bigint(20) DEFAULT NULL COMMENT '删除人',
  `deleted_date` datetime DEFAULT NULL COMMENT '删除时间',
  `tenant_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '租户ID',
  `created_by` bigint(20) NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  `last_modified_by` bigint(20) NOT NULL COMMENT '最后修改人',
  `last_modified_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '最后修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_owner_id` (`owner_id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_created_by` (`created_by`) USING BTREE,
  KEY `idx_created_date` (`created_date`) USING BTREE,
  KEY `idx_method` (`method`) USING BTREE,
  KEY `idx_source` (`source`) USING BTREE,
  KEY `idx_sync_name` (`sync_name`) USING BTREE,
  KEY `idx_protocol` (`protocol`) USING BTREE,
  KEY `idx_endpoint` (`endpoint`(32)) USING BTREE,
  KEY `idx_current_server_id` (`current_server_id`) USING BTREE,
  KEY `idx_service_id` (`service_id`) USING BTREE,
  KEY `idx_project_id` (`project_id`),
  KEY `idx_auth` (`auth`) USING BTREE,
  KEY `idx_deleted` (`deleted`) USING BTREE,
  KEY `idx_publish` (`status`) USING BTREE,
  KEY `idx_project_auth` (`service_auth`) USING BTREE,
  KEY `idx_project_deleted` (`service_deleted`) USING BTREE,
  FULLTEXT KEY `fx_summary_endpoint_ext_search_merge` (`summary`,`endpoint`,`ext_search_merge`) /*!50100 WITH PARSER `ngram` */
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='接口';

-- ----------------------------
-- Table structure for apis_auth
-- ----------------------------
DROP TABLE IF EXISTS `apis_auth`;
CREATE TABLE `apis_auth` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `apis_id` bigint(20) NOT NULL COMMENT 'API，ID',
  `auth_object_type` varchar(20) COLLATE utf8mb4_bin NOT NULL COMMENT '授权对象：用户|部门|组',
  `auth_object_id` bigint(20) NOT NULL COMMENT '授权对象ID',
  `auths` json NOT NULL COMMENT '权限：查看|编辑|删除|发送请求|测试|授权|分享',
  `creator` int(1) NOT NULL COMMENT '创建人授权标识',
  `tenant_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '租户ID',
  `created_by` bigint(20) NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uidx_apis_id_auth_object_id_type_creator` (`apis_id`,`auth_object_id`,`auth_object_type`,`creator`) USING BTREE,
  KEY `idx_created_by` (`created_by`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_auth_object_id` (`auth_object_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='接口授权';

-- ----------------------------
-- Table structure for apis_case
-- ----------------------------
DROP TABLE IF EXISTS `apis_case`;
CREATE TABLE `apis_case` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `project_id` bigint(20) NOT NULL COMMENT '项目ID',
  `services_id` bigint(20) NOT NULL COMMENT '服务ID',
  `name` varchar(400) COLLATE utf8mb4_bin NOT NULL COMMENT '名称',
  `description` varchar(800) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '描述',
  `apis_id` bigint(20) DEFAULT NULL COMMENT '接口ID',
  `apis_deleted` int(1) DEFAULT '0' COMMENT '接口删除标志',
  `enabled` int(1) NOT NULL COMMENT '是否启用',
  `type` varchar(40) COLLATE utf8mb4_bin NOT NULL COMMENT '用例类型',
  `test_method` varchar(40) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '测试方法',
  `protocol` varchar(16) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '协议',
  `method` varchar(16) COLLATE utf8mb4_bin DEFAULT '0' COMMENT '请求方式 GET|POST|...',
  `endpoint` varchar(800) COLLATE utf8mb4_bin DEFAULT '' COMMENT '接口路径',
  `current_server` json DEFAULT NULL COMMENT '当前请求服务器URL',
  `parameters` json DEFAULT NULL COMMENT '请求参数：名称、类型、参数值、参数描述、是否启用',
  `request_body` json DEFAULT NULL COMMENT '请求体：类型、参数(名称、类型、值、描述、是否启用)',
  `authentication` json DEFAULT NULL COMMENT '认证头',
  `assertions` json DEFAULT NULL COMMENT '断言：名称、类型、条件、值、是否启用',
  `dataset_action_on_eof` varchar(20) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '数据集读到结束时处理策略',
  `dataset_sharing_mode` varchar(20) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '数据集线程共享模式',
  `exec_result` varchar(10) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '执行测试结果',
  `exec_failure_message` varchar(200) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '执行失败原因',
  `exec_test_num` int(11) DEFAULT '0' COMMENT '执行次数',
  `exec_test_failure_num` int(11) DEFAULT '0' COMMENT '执行失败次数',
  `exec_id` bigint(20) DEFAULT NULL COMMENT '执行ID',
  `exec_name` varchar(200) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '执行名称',
  `exec_by` bigint(20) DEFAULT NULL COMMENT '执行人ID',
  `exec_date` datetime DEFAULT NULL COMMENT '执行时间',
  `tenant_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '租户ID',
  `created_by` bigint(20) NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  `last_modified_by` bigint(20) NOT NULL COMMENT '最后修改人',
  `last_modified_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '最后修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uidx_name_apis_id` (`name`,`apis_id`) USING BTREE,
  KEY `idx_protocol` (`protocol`) USING BTREE,
  KEY `idx_method` (`method`) USING BTREE,
  KEY `idx_created_date` (`created_date`) USING BTREE,
  KEY `idx_created_by` (`created_by`) USING BTREE,
  KEY `idx_apis_id` (`apis_id`) USING BTREE,
  KEY `idx_last_modified_by` (`last_modified_date`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_project_id` (`project_id`) USING BTREE,
  KEY `idx_endpoint` (`endpoint`(50)) USING BTREE,
  KEY `idx_type` (`type`),
  KEY `idx_enabled` (`enabled`),
  KEY `idx_test_method` (`test_method`),
  FULLTEXT KEY `fx_name_description` (`name`,`description`) /*!50100 WITH PARSER `ngram` */
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='接口功能用例';

-- ----------------------------
-- Table structure for apis_design
-- ----------------------------
DROP TABLE IF EXISTS `apis_design`;
CREATE TABLE `apis_design` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `project_id` bigint(20) NOT NULL COMMENT '项目ID',
  `name` varchar(100) COLLATE utf8mb4_bin NOT NULL COMMENT '设计名',
  `released` int(1) NOT NULL COMMENT '发布标志',
  `openapi_spec_version` varchar(20) COLLATE utf8mb4_bin NOT NULL COMMENT 'OAS版本',
  `openapi` longtext COLLATE utf8mb4_bin COMMENT 'OAS内容',
  `design_source` varchar(20) COLLATE utf8mb4_bin NOT NULL COMMENT '设计来源',
  `design_source_id` bigint(20) DEFAULT NULL COMMENT '设计来源ID',
  `tenant_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '租户ID',
  `created_by` bigint(20) NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  `last_modified_by` bigint(20) NOT NULL COMMENT '最后修改人',
  `last_modified_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '最后修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_project_id` (`project_id`) USING BTREE,
  KEY `idx_design_source` (`design_source`) USING BTREE,
  KEY `idx_design_source_id` (`design_source_id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_created_by` (`created_by`) USING BTREE,
  KEY `idx_created_date` (`created_date`) USING BTREE,
  KEY `idx_last_modified_by` (`last_modified_by`) USING BTREE,
  KEY `idx_last_modified_date` (`last_modified_date`) USING BTREE,
  KEY `idx_name` (`name`),
  FULLTEXT KEY `fx_name` (`name`) /*!50100 WITH PARSER `ngram` */
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='接口设计';

-- ----------------------------
-- Table structure for apis_favourite
-- ----------------------------
DROP TABLE IF EXISTS `apis_favourite`;
CREATE TABLE `apis_favourite` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `project_id` bigint(20) NOT NULL COMMENT '项目ID',
  `apis_id` bigint(20) NOT NULL COMMENT '接口ID',
  `tenant_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '租户ID',
  `created_by` bigint(20) NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uidx_created_by_apis_id` (`created_by`,`apis_id`) USING BTREE,
  KEY `idx_apis_id` (`apis_id`),
  KEY `idx_created_by` (`created_by`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_project_id` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='接口收藏';

-- ----------------------------
-- Table structure for apis_follow
-- ----------------------------
DROP TABLE IF EXISTS `apis_follow`;
CREATE TABLE `apis_follow` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `project_id` bigint(20) NOT NULL COMMENT '项目ID',
  `apis_id` bigint(20) NOT NULL COMMENT '接口ID',
  `tenant_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '租户ID',
  `created_by` bigint(20) NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uidx_created_by_apis_id` (`created_by`,`apis_id`) USING BTREE,
  KEY `idx_apis_id` (`apis_id`),
  KEY `idx_created_by` (`created_by`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_project_id` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='接口关注';

-- ----------------------------
-- Table structure for apis_share
-- ----------------------------
DROP TABLE IF EXISTS `apis_share`;
CREATE TABLE `apis_share` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `project_id` bigint(20) NOT NULL COMMENT '项目ID',
  `name` varchar(100) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '名称',
  `remark` varchar(400) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '备注',
  `expired_date` datetime DEFAULT NULL COMMENT '过期时间',
  `display_options` json DEFAULT NULL COMMENT '显示选项',
  `share_scope` varchar(20) COLLATE utf8mb4_bin NOT NULL COMMENT '分享范围',
  `services_id` bigint(20) NOT NULL COMMENT '分享服务ID',
  `apis_ids` json DEFAULT NULL COMMENT '分享接口IDs',
  `base_url` varchar(400) COLLATE utf8mb4_bin NOT NULL COMMENT '分享页面前缀',
  `pat` varchar(40) COLLATE utf8mb4_bin NOT NULL COMMENT '公开访问令牌',
  `view_num` int(11) DEFAULT '0' COMMENT '查看次数',
  `tenant_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '租户ID',
  `created_by` bigint(20) NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL COMMENT '创建时间',
  `last_modified_by` bigint(20) NOT NULL DEFAULT '-1' COMMENT '最后修改人',
  `last_modified_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '最后修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_project_id` (`project_id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_services_id` (`services_id`) USING BTREE,
  KEY `idx_created_by` (`created_by`) USING BTREE,
  KEY `idx_created_date` (`created_date`) USING BTREE,
  FULLTEXT KEY `fx_name_remark` (`name`,`remark`) /*!50100 WITH PARSER `ngram` */
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='接口分享';

-- ----------------------------
-- Table structure for apis_trash
-- ----------------------------
DROP TABLE IF EXISTS `apis_trash`;
CREATE TABLE `apis_trash` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `project_id` bigint(20) NOT NULL COMMENT '项目ID',
  `services_id` bigint(20) DEFAULT NULL COMMENT '服务ID',
  `target_type` varchar(16) COLLATE utf8mb4_bin NOT NULL COMMENT '类型：SERVICE、APIS',
  `target_id` bigint(20) NOT NULL COMMENT '服务或接口ID',
  `target_name` varchar(100) COLLATE utf8mb4_bin NOT NULL COMMENT '名称',
  `tenant_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '租户ID',
  `created_by` bigint(20) NOT NULL DEFAULT '0' COMMENT '创建人',
  `deleted_by` bigint(20) NOT NULL COMMENT '删除人',
  `deleted_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uidx_target_type_id` (`target_id`,`target_type`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_created_by` (`created_by`) USING BTREE,
  KEY `idx_project_id` (`project_id`),
  KEY `idx_deleted_date` (`deleted_date`),
  KEY `idx_service_id` (`services_id`) USING BTREE,
  FULLTEXT KEY `fx_target_name` (`target_name`) /*!50100 WITH PARSER `ngram` */
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC COMMENT='接口回收站';

-- ----------------------------
-- Table structure for apis_unarchived
-- ----------------------------
DROP TABLE IF EXISTS `apis_unarchived`;
CREATE TABLE `apis_unarchived` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `project_id` bigint(20) NOT NULL COMMENT '项目ID',
  `protocol` varchar(16) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '协议',
  `method` varchar(16) COLLATE utf8mb4_bin NOT NULL DEFAULT '0' COMMENT '请求方式 GET|POST|...',
  `endpoint` varchar(800) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '接口路径',
  `summary` varchar(400) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '名称',
  `description` text COLLATE utf8mb4_bin COMMENT '描述',
  `parameters` json DEFAULT NULL COMMENT '请求参数',
  `request_body` json DEFAULT NULL COMMENT '请求体',
  `responses` json DEFAULT NULL COMMENT '响应数据',
  `security` json DEFAULT NULL COMMENT '安全需求',
  `extensions` json DEFAULT NULL COMMENT '接口扩展',
  `current_server` json DEFAULT NULL COMMENT '当前请求服务器URL',
  `authentication` json DEFAULT NULL COMMENT '安全信息',
  `assertions` json DEFAULT NULL COMMENT '断言',
  `secured` int(1) NOT NULL DEFAULT '0' COMMENT '是否有认证头控制',
  `tenant_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '租户ID',
  `created_by` bigint(20) NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  `last_modified_by` bigint(20) NOT NULL COMMENT '最后修改人',
  `last_modified_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '最后修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_created_by` (`created_by`) USING BTREE,
  KEY `idx_created_date` (`created_date`) USING BTREE,
  KEY `idx_method` (`method`) USING BTREE,
  KEY `idx_summary` (`summary`) USING BTREE,
  KEY `idx_project_id` (`project_id`),
  FULLTEXT KEY `fx_summary_endpoint` (`summary`,`endpoint`) /*!50100 WITH PARSER `ngram` */
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='未归档接口';

-- ----------------------------
-- Table structure for comment
-- ----------------------------
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `pid` bigint(20) NOT NULL COMMENT '评论父ID',
  `level` int(5) NOT NULL COMMENT '评论层级',
  `content` varchar(800) COLLATE utf8mb4_bin NOT NULL COMMENT '评论内容',
  `target_id` bigint(20) NOT NULL COMMENT '被评论的ID（博客或视频）',
  `target_type` varchar(20) COLLATE utf8mb4_bin NOT NULL COMMENT '评论内容类型(博客或者视频)',
  `user_id` bigint(20) NOT NULL COMMENT '评论人ID',
  `created_date` datetime NOT NULL COMMENT '评论时间',
  PRIMARY KEY (`id`),
  KEY `idx_pid` (`pid`) USING BTREE,
  KEY `idx_target_id` (`target_id`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE,
  KEY `idx_target_type` (`target_type`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='评论';

-- ----------------------------
-- Table structure for data_dataset
-- ----------------------------
DROP TABLE IF EXISTS `data_dataset`;
CREATE TABLE `data_dataset` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `project_id` bigint(20) NOT NULL COMMENT '项目ID',
  `name` varchar(100) COLLATE utf8mb4_bin NOT NULL COMMENT '名称',
  `description` varchar(200) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '描述',
  `extracted` int(11) NOT NULL COMMENT '是否提取变量值',
  `parameters` json NOT NULL COMMENT '参数化',
  `extraction` json DEFAULT NULL COMMENT '提取规则',
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `created_by` bigint(20) NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  `last_modified_by` bigint(20) NOT NULL COMMENT '最后修改人',
  `last_modified_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '最后修改时间',
  UNIQUE KEY `idx_project_id_name` (`project_id`,`name`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_created_by` (`created_by`),
  KEY `idx_created_date` (`created_date`),
  KEY `idx_last_modified_by` (`last_modified_by`),
  KEY `idx_last_modified_date` (`last_modified_date`),
  KEY `idx_name` (`name`) USING BTREE,
  FULLTEXT KEY `fx_name` (`name`) /*!50100 WITH PARSER `ngram` */
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='数据集';

-- ----------------------------
-- Table structure for data_dataset_target
-- ----------------------------
DROP TABLE IF EXISTS `data_dataset_target`;
CREATE TABLE `data_dataset_target` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `project_id` bigint(20) NOT NULL COMMENT '项目ID',
  `dataset_id` bigint(20) NOT NULL COMMENT '数据集ID',
  `target_id` bigint(20) NOT NULL COMMENT '使用资源ID',
  `target_type` varchar(40) COLLATE utf8mb4_bin NOT NULL COMMENT '使用资源类型',
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `created_by` bigint(20) NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  UNIQUE KEY `idx_dataset_target` (`dataset_id`,`target_id`,`target_type`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_target_id_type` (`tenant_id`,`target_type`),
  KEY `idx_target_type` (`target_type`),
  KEY `idx_project_id` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='数据集使用资源';

-- ----------------------------
-- Table structure for data_datasource
-- ----------------------------
DROP TABLE IF EXISTS `data_datasource`;
CREATE TABLE `data_datasource` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `project_id` bigint(20) NOT NULL COMMENT '项目ID',
  `name` varchar(100) COLLATE utf8mb4_bin NOT NULL COMMENT '名称',
  `database0` varchar(40) COLLATE utf8mb4_bin NOT NULL COMMENT '数据库',
  `driver_class_name` varchar(200) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '驱动类名',
  `username` varchar(200) COLLATE utf8mb4_bin NOT NULL COMMENT '用户名',
  `password` varchar(1600) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '密码',
  `jdbc_url` varchar(2048) COLLATE utf8mb4_bin NOT NULL COMMENT 'JDBC URL',
  `tenant_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '租户ID',
  `created_by` bigint(20) NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  `last_modified_by` bigint(20) NOT NULL COMMENT '最后修改人',
  `last_modified_date` datetime NOT NULL COMMENT '最后修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `udx_tenant_id_name` (`tenant_id`,`name`) USING BTREE,
  KEY `idx_name` (`name`) USING BTREE,
  KEY `idx_database0` (`database0`) USING BTREE,
  KEY `idx_last_modified_date` (`last_modified_date`) USING BTREE,
  KEY `idx_last_modified_by` (`last_modified_by`),
  KEY `idx_project_id` (`project_id`),
  FULLTEXT KEY `fx_name` (`name`) /*!50100 WITH PARSER `ngram` */
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='Mock数据源';

-- ----------------------------
-- Table structure for data_variable
-- ----------------------------
DROP TABLE IF EXISTS `data_variable`;
CREATE TABLE `data_variable` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `project_id` bigint(20) NOT NULL COMMENT '项目ID',
  `name` varchar(100) COLLATE utf8mb4_bin NOT NULL COMMENT '名称',
  `description` varchar(200) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '描述',
  `extracted` int(11) NOT NULL COMMENT '是否提取变量值',
  `value` varchar(4096) COLLATE utf8mb4_bin DEFAULT '' COMMENT '值',
  `password_value` int(11) NOT NULL DEFAULT '0' COMMENT '是否密码值',
  `extraction` json DEFAULT NULL COMMENT '提取规则',
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `created_by` bigint(20) NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  `last_modified_by` bigint(20) NOT NULL COMMENT '最后修改人',
  `last_modified_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '最后修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `idx_project_id_name` (`project_id`,`name`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_created_by` (`created_by`) USING BTREE,
  KEY `idx_created_date` (`created_date`) USING BTREE,
  KEY `idx_last_modified_by` (`last_modified_by`),
  KEY `idx_last_modified_date` (`last_modified_date`),
  KEY `idx_name` (`name`) USING BTREE,
  FULLTEXT KEY `fx_name` (`name`) /*!50100 WITH PARSER `ngram` */
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='环境变量';

-- ----------------------------
-- Table structure for data_variable_target
-- ----------------------------
DROP TABLE IF EXISTS `data_variable_target`;
CREATE TABLE `data_variable_target` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `project_id` bigint(20) NOT NULL COMMENT '项目ID',
  `variable_id` bigint(20) NOT NULL COMMENT '变量ID',
  `target_id` bigint(20) NOT NULL COMMENT '使用资源ID',
  `target_type` varchar(40) COLLATE utf8mb4_bin NOT NULL COMMENT '使用资源类型',
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `created_by` bigint(20) NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  UNIQUE KEY `idx_variable_target` (`variable_id`,`target_id`,`target_type`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_target_id_type` (`tenant_id`,`target_type`),
  KEY `idx_target_type` (`target_type`),
  KEY `idx_project_id` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='变量使用资源';

-- ----------------------------
-- Table structure for exec
-- ----------------------------
DROP TABLE IF EXISTS `exec`;
CREATE TABLE `exec` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `project_id` bigint(20) DEFAULT NULL COMMENT '项目ID',
  `service_id` bigint(20) DEFAULT NULL COMMENT '接口服务ID',
  `name` varchar(200) COLLATE utf8mb4_bin NOT NULL COMMENT '名称',
  `plugin` varchar(40) COLLATE utf8mb4_bin NOT NULL COMMENT '插件名称',
  `script_type` varchar(40) COLLATE utf8mb4_bin NOT NULL COMMENT '脚本类型',
  `script_id` bigint(20) DEFAULT NULL COMMENT '脚本ID',
  `script` mediumtext COLLATE utf8mb4_bin COMMENT '脚本内容-YAML格式',
  `script_created_by` bigint(20) DEFAULT NULL COMMENT '脚本创建人',
  `script_source` varchar(40) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '脚本来源',
  `script_source_id` bigint(20) DEFAULT NULL COMMENT '脚本来源资源ID',
  `status` varchar(20) COLLATE utf8mb4_bin NOT NULL COMMENT '执行状态',
  `iterations` bigint(20) DEFAULT NULL COMMENT '迭代次数',
  `duration` varchar(40) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '迭代时长',
  `thread` int(11) NOT NULL COMMENT '线程数',
  `priority` int(11) DEFAULT NULL COMMENT '优先级',
  `ignore_assertions` int(1) NOT NULL DEFAULT '1' COMMENT '忽略断言',
  `start_mode` varchar(16) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '启动模式',
  `start_at_date` datetime DEFAULT NULL COMMENT '计划启动时间',
  `startup_timeout` varchar(40) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '启动超时',
  `report_interval` varchar(40) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '报告间隔',
  `update_test_result` int(1) DEFAULT '1' COMMENT '更新测试结果状态',
  `sync_test_result` int(1) DEFAULT '0' COMMENT '同步测试结果',
  `sync_test_result_failure` varchar(1024) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '同步测试结果失败原因',
  `assoc_api_case_ids` varchar(2000) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '关联接口用例ID',
  `target_parent_id` bigint(20) DEFAULT NULL COMMENT '目标父ID',
  `trial` int(11) NOT NULL DEFAULT '0' COMMENT '是否体验执行',
  `available_node_ids` json DEFAULT NULL COMMENT '可用节点IDs',
  `app_node_ids` json DEFAULT NULL COMMENT '应用节点IDs',
  `exec_node_ids` json DEFAULT NULL COMMENT '执行节点IDs',
  `actual_start_date` datetime DEFAULT NULL COMMENT '实际开始时间',
  `end_date` datetime DEFAULT NULL COMMENT '结束时间',
  `exec_by` bigint(20) NOT NULL COMMENT '执行人',
  `scheduling_num` int(11) DEFAULT NULL COMMENT '调度次数',
  `last_scheduling_date` datetime DEFAULT NULL COMMENT '最后调度时间',
  `last_scheduling_result` mediumtext COLLATE utf8mb4_bin COMMENT '最后调度结果',
  `meter_status` varchar(40) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '采样状态',
  `meter_message` varchar(1024) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '采样状态信息',
  `single_target_pipeline` int(1) DEFAULT NULL COMMENT '是否单测试任务执行',
  `assemble_and_send_event` int(1) DEFAULT NULL COMMENT '组装发送事件标志',
  `ext_search_merge` varchar(20) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '扩展搜索河合并列',
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `created_by` bigint(20) NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL COMMENT '创建时间',
  `last_modified_by` bigint(20) NOT NULL COMMENT '最后修改人',
  `last_modified_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '最后修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `idx_script_id_type` (`script_id`,`script_type`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_priority` (`priority`) USING BTREE,
  KEY `idx_name` (`name`) USING BTREE,
  KEY `idx_created_by` (`created_by`) USING BTREE,
  KEY `idx_created_date` (`created_date`) USING BTREE,
  KEY `idx_plugin` (`plugin`) USING BTREE,
  KEY `idx_target_parent_id` (`target_parent_id`) USING BTREE,
  KEY `idx_script_type` (`script_type`) USING BTREE,
  KEY `idx_start_date` (`start_at_date`) USING BTREE,
  KEY `idx_end_date` (`end_date`) USING BTREE,
  KEY `idx_actual_start_date` (`actual_start_date`) USING BTREE,
  KEY `idx_start_mode` (`start_mode`) USING BTREE,
  KEY `idx_exec_by` (`exec_by`) USING BTREE,
  KEY `idx_last_modified_by` (`last_modified_by`) USING BTREE,
  KEY `idx_last_modified_date` (`last_modified_date`) USING BTREE,
  KEY `idx_sync_test_result` (`sync_test_result`) USING BTREE,
  KEY `idx_update_test_result` (`update_test_result`) USING BTREE,
  KEY `idx_trial` (`trial`) USING BTREE,
  KEY `idx_script_source_id` (`script_source_id`) USING BTREE,
  KEY `idx_script_source` (`script_source`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_service_id` (`service_id`),
  KEY `idx_assemble_and_send_event` (`assemble_and_send_event`) USING BTREE,
  FULLTEXT KEY `fx_name_plugin_ext` (`name`,`plugin`,`ext_search_merge`) /*!50100 WITH PARSER `ngram` */
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='执行记录';

-- ----------------------------
-- Table structure for exec_debug
-- ----------------------------
DROP TABLE IF EXISTS `exec_debug`;
CREATE TABLE `exec_debug` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `name` varchar(200) COLLATE utf8mb4_bin NOT NULL COMMENT '名称',
  `plugin` varchar(40) COLLATE utf8mb4_bin NOT NULL COMMENT '插件名称',
  `source` varchar(40) COLLATE utf8mb4_bin NOT NULL DEFAULT 'SCRIPT',
  `script_type` varchar(40) COLLATE utf8mb4_bin NOT NULL COMMENT '脚本类型',
  `script_id` bigint(20) DEFAULT NULL COMMENT '脚本ID',
  `script` mediumtext COLLATE utf8mb4_bin COMMENT '脚本内容-YAML格式',
  `scenario_id` bigint(20) DEFAULT NULL COMMENT '关联场景ID',
  `monitor_id` bigint(20) DEFAULT NULL COMMENT '关联监控ID',
  `status` varchar(20) COLLATE utf8mb4_bin NOT NULL COMMENT '执行状态',
  `message` varchar(1024) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '执行状态信息',
  `available_node_ids` json DEFAULT NULL COMMENT '可用节点IDs',
  `exec_node_id` bigint(20) DEFAULT NULL COMMENT '执行节点ID',
  `end_date` datetime DEFAULT NULL COMMENT '结束时间',
  `scheduling_result` mediumtext COLLATE utf8mb4_bin COMMENT '最后调度结果',
  `meter_status` varchar(40) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '采样状态',
  `meter_message` varchar(1024) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '采样状态信息',
  `single_target_pipeline` int(1) DEFAULT NULL COMMENT '是否单测试任务执行',
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `created_by` bigint(20) NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_scenario_id` (`tenant_id`) USING BTREE,
  KEY `idx_script_id` (`name`) USING BTREE,
  KEY `idx_created_date` (`created_date`) USING BTREE,
  KEY `idx_script_type` (`script_type`) USING BTREE,
  KEY `idx_created_by` (`created_by`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_source` (`source`),
  KEY `idx_monitor_id` (`monitor_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='执行调试';

-- ----------------------------
-- Table structure for exec_node
-- ----------------------------
DROP TABLE IF EXISTS `exec_node`;
CREATE TABLE `exec_node` (
  `id` bigint(20) NOT NULL,
  `exec_id` bigint(20) NOT NULL COMMENT '执行ID',
  `node_id` bigint(20) NOT NULL COMMENT '执行节点ID',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_exec_id` (`exec_id`) USING BTREE,
  KEY `idx_target_id` (`node_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='执行节点结果';

-- ----------------------------
-- Table structure for exec_test_case_result
-- ----------------------------
DROP TABLE IF EXISTS `exec_test_case_result`;
CREATE TABLE `exec_test_case_result` (
  `id` bigint(20) NOT NULL,
  `exec_id` bigint(20) NOT NULL,
  `exec_status` varchar(20) COLLATE utf8mb4_bin NOT NULL,
  `plugin` varchar(40) COLLATE utf8mb4_bin NOT NULL,
  `script_type` varchar(40) COLLATE utf8mb4_bin NOT NULL,
  `script_id` bigint(20) DEFAULT NULL,
  `apis_id` bigint(20) DEFAULT NULL,
  `case_id` bigint(20) NOT NULL,
  `case_name` varchar(400) COLLATE utf8mb4_bin NOT NULL,
  `enabled` int(1) NOT NULL,
  `case_type` varchar(40) COLLATE utf8mb4_bin DEFAULT NULL,
  `passed` int(1) DEFAULT NULL,
  `failure_message` varchar(1024) COLLATE utf8mb4_bin DEFAULT NULL,
  `test_num` int(11) NOT NULL,
  `test_failure_num` int(11) NOT NULL,
  `assertion_summary` json DEFAULT NULL,
  `sample_content` json DEFAULT NULL,
  `tenant_id` bigint(20) NOT NULL DEFAULT '-1',
  `exec_by` bigint(20) NOT NULL,
  `last_exec_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `udx_case_id` (`case_id`) USING BTREE,
  KEY `idx_exec_id` (`exec_id`) USING BTREE,
  KEY `idx_last_exec_id` (`script_type`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_apis_id` (`apis_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='执行用例结果';

-- ----------------------------
-- Table structure for exec_test_result
-- ----------------------------
DROP TABLE IF EXISTS `exec_test_result`;
CREATE TABLE `exec_test_result` (
  `id` bigint(20) NOT NULL,
  `exec_id` bigint(20) NOT NULL,
  `exec_status` varchar(20) COLLATE utf8mb4_bin NOT NULL,
  `plugin` varchar(40) COLLATE utf8mb4_bin NOT NULL,
  `script_type` varchar(40) COLLATE utf8mb4_bin NOT NULL,
  `script_id` bigint(20) DEFAULT NULL,
  `script_source` varchar(40) COLLATE utf8mb4_bin DEFAULT NULL,
  `script_source_id` bigint(20) DEFAULT NULL,
  `indicator_func` json DEFAULT NULL,
  `indicator_perf` json DEFAULT NULL,
  `indicator_stability` json DEFAULT NULL,
  `passed` int(1) NOT NULL,
  `failure_message` varchar(1024) COLLATE utf8mb4_bin DEFAULT NULL,
  `test_num` int(11) NOT NULL,
  `test_failure_num` int(11) NOT NULL,
  `usage_failed_node_id` bigint(20) DEFAULT NULL,
  `sample_summary` json DEFAULT NULL,
  `target_summary` json DEFAULT NULL,
  `case_summary` json DEFAULT NULL,
  `node_usage_summary` json DEFAULT NULL,
  `sample_content` json DEFAULT NULL,
  `tenant_id` bigint(20) NOT NULL DEFAULT '-1',
  `exec_by` bigint(20) NOT NULL,
  `last_exec_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uidx_exec_id` (`exec_id`),
  UNIQUE KEY `uidx_script_source_id` (`script_source_id`,`script_type`),
  UNIQUE KEY `uidx_script_id_type` (`script_id`,`script_type`),
  KEY `idx_script_type` (`script_type`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_script_source` (`script_source`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='执行接口结果';

-- ----------------------------
-- Table structure for func_baseline
-- ----------------------------
DROP TABLE IF EXISTS `func_baseline`;
CREATE TABLE `func_baseline` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `name` varchar(200) COLLATE utf8mb4_bin NOT NULL COMMENT '计划名称',
  `project_id` bigint(20) NOT NULL COMMENT '项目ID',
  `plan_id` bigint(20) NOT NULL COMMENT '计划ID',
  `description` text COLLATE utf8mb4_bin COMMENT '描述',
  `established` int(1) NOT NULL DEFAULT '0' COMMENT '已建立基准标志',
  `case_ids` json DEFAULT NULL COMMENT '基线用例ID',
  `tenant_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '租户ID',
  `created_by` bigint(20) NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  `last_modified_by` bigint(20) NOT NULL COMMENT '最后修改人',
  `last_modified_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '最后修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_name` (`name`) USING BTREE,
  KEY `idx_owner_id` (`plan_id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_created_by` (`created_by`) USING BTREE,
  KEY `idx_created_date` (`created_date`) USING BTREE,
  KEY `idx_project_id` (`project_id`),
  KEY `idx_plan_id` (`plan_id`) USING BTREE,
  KEY `idx_last_modified_by` (`last_modified_by`) USING BTREE,
  KEY `idx_established` (`established`) USING BTREE,
  FULLTEXT KEY `fx_name_description` (`name`,`description`) /*!50100 WITH PARSER `ngram` */
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='功能基线';

-- ----------------------------
-- Table structure for func_baseline_case
-- ----------------------------
DROP TABLE IF EXISTS `func_baseline_case`;
CREATE TABLE `func_baseline_case` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `baseline_id` bigint(20) NOT NULL COMMENT '基线ID',
  `project_id` bigint(20) NOT NULL COMMENT '项目ID',
  `plan_id` bigint(20) NOT NULL COMMENT '计划ID',
  `module_id` bigint(20) DEFAULT '-1' COMMENT '模块ID',
  `case_id` bigint(20) NOT NULL COMMENT '用例ID',
  `name` varchar(400) COLLATE utf8mb4_bin NOT NULL COMMENT '名称',
  `code` varchar(40) COLLATE utf8mb4_bin NOT NULL COMMENT '编码',
  `version` int(11) NOT NULL DEFAULT '1' COMMENT '版本',
  `priority` varchar(16) COLLATE utf8mb4_bin NOT NULL DEFAULT '0' COMMENT '优先级等级',
  `deadline_date` datetime NOT NULL COMMENT '截止时间',
  `overdue` int(1) NOT NULL DEFAULT '0' COMMENT '逾期标志',
  `eval_workload_method` varchar(16) COLLATE utf8mb4_bin NOT NULL DEFAULT '0.0' COMMENT '预估工作量方式',
  `eval_workload` double(8,2) DEFAULT '0.00' COMMENT '评估工作量',
  `actual_workload` double(8,2) DEFAULT '0.00' COMMENT '实际工作量',
  `precondition` text COLLATE utf8mb4_bin COMMENT '前置条件',
  `steps` json DEFAULT NULL COMMENT '步骤',
  `description` text COLLATE utf8mb4_bin COMMENT '描述',
  `review` int(1) NOT NULL COMMENT '评审标记',
  `reviewer_id` bigint(20) DEFAULT NULL COMMENT '评审人ID',
  `review_date` datetime DEFAULT NULL COMMENT '评审时间',
  `review_status` varchar(20) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '评审状态',
  `review_remark` varchar(400) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '评审备注',
  `review_num` int(11) NOT NULL DEFAULT '0' COMMENT '评审次数',
  `review_fail_num` int(11) NOT NULL DEFAULT '0' COMMENT '评审失败次数',
  `tester_id` bigint(20) NOT NULL COMMENT '测试人ID',
  `developer_id` bigint(20) NOT NULL COMMENT '开发人ID',
  `test_num` int(11) NOT NULL DEFAULT '0' COMMENT '测试次数',
  `test_fail_num` int(11) NOT NULL DEFAULT '0' COMMENT '测试失败次数',
  `test_result` varchar(16) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '测试结果',
  `test_remark` varchar(400) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '测试结果备注',
  `test_result_handle_date` datetime DEFAULT NULL COMMENT '测试结果处理时间',
  `attachments` json DEFAULT NULL COMMENT '附件',
  `tenant_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '租户ID',
  `created_by` bigint(20) NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  `last_modified_by` bigint(20) NOT NULL COMMENT '最后修改人',
  `last_modified_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '最后修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_plan_id` (`plan_id`) USING BTREE,
  KEY `idx_module` (`module_id`) USING BTREE,
  KEY `idx_created_date` (`created_date`) USING BTREE,
  KEY `uidx_name_plan_id` (`name`,`plan_id`) USING BTREE,
  KEY `idx_priority` (`priority`) USING BTREE,
  KEY `idx_tester_id` (`tester_id`) USING BTREE,
  KEY `idx_created_by` (`created_by`) USING BTREE,
  KEY `idx_test_result` (`test_result`) USING BTREE,
  KEY `idx_deadline_date` (`deadline_date`) USING BTREE,
  KEY `idx_test_fail_num` (`test_fail_num`) USING BTREE,
  KEY `idx_testw_num` (`test_num`) USING BTREE,
  KEY `idx_test_num` (`last_modified_by`) USING BTREE,
  KEY `idx_last_modified_by` (`last_modified_date`) USING BTREE,
  KEY `idx_review_status` (`review_status`) USING BTREE,
  KEY `idx_review_num` (`review_num`) USING BTREE,
  KEY `idx_reviewer_id` (`reviewer_id`) USING BTREE,
  KEY `idx_review_date` (`review_date`) USING BTREE,
  KEY `idx_test_result_handle_date` (`test_result_handle_date`) USING BTREE,
  KEY `idx_code` (`code`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_review_fail_num` (`review_fail_num`) USING BTREE,
  KEY `idx_project_id` (`project_id`) USING BTREE,
  KEY `idx_developer_id` (`developer_id`),
  KEY `idx_baseline_id` (`baseline_id`) USING BTREE,
  KEY `idx_case_id` (`case_id`) USING BTREE,
  KEY `idx_overdue` (`overdue`),
  FULLTEXT KEY `fx_name_description_apis` (`name`,`description`,`code`) /*!50100 WITH PARSER `ngram` */
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='功能基线用例';

-- ----------------------------
-- Table structure for func_case
-- ----------------------------
DROP TABLE IF EXISTS `func_case`;
CREATE TABLE `func_case` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `project_id` bigint(20) NOT NULL COMMENT '项目ID',
  `name` varchar(400) COLLATE utf8mb4_bin NOT NULL COMMENT '名称',
  `code` varchar(40) COLLATE utf8mb4_bin NOT NULL COMMENT '编码',
  `version` int(11) NOT NULL DEFAULT '1' COMMENT '版本',
  `plan_id` bigint(20) NOT NULL COMMENT '计划ID',
  `module_id` bigint(20) DEFAULT '-1' COMMENT '模块ID',
  `software_version` varchar(40) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '软件版本',
  `priority` varchar(16) COLLATE utf8mb4_bin NOT NULL DEFAULT '0' COMMENT '优先级等级',
  `deadline_date` datetime NOT NULL COMMENT '截止时间',
  `overdue` int(1) NOT NULL DEFAULT '0' COMMENT '逾期标志',
  `eval_workload_method` varchar(16) COLLATE utf8mb4_bin NOT NULL DEFAULT '0.0' COMMENT '预估工作量方式',
  `eval_workload` double(8,2) DEFAULT '0.00' COMMENT '评估工作量',
  `actual_workload` double(8,2) DEFAULT '0.00' COMMENT '实际工作量',
  `plan_auth` int(1) NOT NULL COMMENT '计划授权控制标志',
  `precondition` text COLLATE utf8mb4_bin COMMENT '前置条件',
  `step_view` varchar(8) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '测试步骤视图',
  `steps` json DEFAULT NULL COMMENT '测试步骤',
  `description` text COLLATE utf8mb4_bin COMMENT '描述',
  `review` int(1) NOT NULL COMMENT '是否评审标志',
  `reviewer_id` bigint(20) DEFAULT NULL COMMENT '评审人ID',
  `review_date` datetime DEFAULT NULL COMMENT '评审时间',
  `review_status` varchar(20) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '评审状态',
  `review_remark` varchar(400) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '评审备注',
  `review_num` int(11) NOT NULL DEFAULT '0' COMMENT '评审次数',
  `review_fail_num` int(11) NOT NULL DEFAULT '0' COMMENT '评审失败次数',
  `tester_id` bigint(20) NOT NULL COMMENT '测试人ID',
  `developer_id` bigint(20) NOT NULL COMMENT '开发人ID',
  `unplanned` int(1) NOT NULL DEFAULT '0' COMMENT '计划外标志',
  `test_num` int(11) NOT NULL DEFAULT '0' COMMENT '测试次数',
  `test_fail_num` int(11) NOT NULL DEFAULT '0' COMMENT '测试失败次数',
  `test_result` varchar(16) COLLATE utf8mb4_bin NOT NULL COMMENT '测试结果',
  `test_remark` varchar(400) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '测试结果备注',
  `test_result_handle_date` datetime DEFAULT NULL COMMENT '测试结果处理时间',
  `attachments` json DEFAULT NULL COMMENT '附件',
  `tenant_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '租户ID',
  `plan_deleted` int(1) NOT NULL DEFAULT '0' COMMENT '计划删除标志',
  `deleted` int(1) NOT NULL DEFAULT '0' COMMENT '删除状态：0-未删除；1-已删除',
  `deleted_by` bigint(20) DEFAULT NULL COMMENT '删除人',
  `deleted_date` datetime DEFAULT NULL COMMENT '删除时间',
  `created_by` bigint(20) NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  `last_modified_by` bigint(20) NOT NULL COMMENT '最后修改人',
  `last_modified_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '最后修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_plan_id` (`plan_id`) USING BTREE,
  KEY `idx_module` (`module_id`) USING BTREE,
  KEY `idx_created_date` (`created_date`) USING BTREE,
  KEY `uidx_name_plan_id` (`name`,`plan_id`) USING BTREE,
  KEY `idx_priority` (`priority`) USING BTREE,
  KEY `idx_tester_id` (`tester_id`) USING BTREE,
  KEY `idx_created_by` (`created_by`) USING BTREE,
  KEY `idx_test_result` (`test_result`) USING BTREE,
  KEY `idx_deadline_date` (`deadline_date`) USING BTREE,
  KEY `idx_test_fail_num` (`test_fail_num`) USING BTREE,
  KEY `idx_testw_num` (`test_num`) USING BTREE,
  KEY `idx_test_num` (`last_modified_by`) USING BTREE,
  KEY `idx_last_modified_by` (`last_modified_date`) USING BTREE,
  KEY `idx_review_status` (`review_status`) USING BTREE,
  KEY `idx_review_num` (`review_num`) USING BTREE,
  KEY `idx_reviewer_id` (`reviewer_id`) USING BTREE,
  KEY `idx_review_date` (`review_date`) USING BTREE,
  KEY `idx_test_result_handle_date` (`test_result_handle_date`) USING BTREE,
  KEY `idx_code` (`code`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_review_fail_num` (`review_fail_num`) USING BTREE,
  KEY `idx_project_id` (`project_id`) USING BTREE,
  KEY `idx_developer_id` (`developer_id`),
  KEY `idx_software_version` (`software_version`) USING BTREE,
  KEY `idx_plan_deleted` (`plan_deleted`) USING BTREE,
  KEY `idx_overdue` (`overdue`),
  KEY `unplanned` (`unplanned`),
  FULLTEXT KEY `fx_name_description_apis` (`name`,`description`,`code`) /*!50100 WITH PARSER `ngram` */
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='功能用例';

-- ----------------------------
-- Table structure for func_case_favourite
-- ----------------------------
DROP TABLE IF EXISTS `func_case_favourite`;
CREATE TABLE `func_case_favourite` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `project_id` bigint(20) NOT NULL COMMENT '项目ID',
  `case_id` bigint(20) NOT NULL COMMENT '用例ID',
  `tenant_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '租户ID',
  `created_by` bigint(20) NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uidx_created_by_apis_id` (`created_by`,`case_id`) USING BTREE,
  KEY `idx_case_id` (`case_id`),
  KEY `idx_created_by` (`created_by`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_project_id` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='接口收藏';

-- ----------------------------
-- Table structure for func_case_follow
-- ----------------------------
DROP TABLE IF EXISTS `func_case_follow`;
CREATE TABLE `func_case_follow` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `project_id` bigint(20) NOT NULL COMMENT '项目ID',
  `case_id` bigint(20) NOT NULL COMMENT '用例ID',
  `tenant_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '租户ID',
  `created_by` bigint(20) NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uidx_created_by_apis_id` (`created_by`,`case_id`) USING BTREE,
  KEY `idx_case_id` (`case_id`),
  KEY `idx_created_by` (`created_by`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_project_id` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='接口关注';

-- ----------------------------
-- Table structure for func_plan
-- ----------------------------
DROP TABLE IF EXISTS `func_plan`;
CREATE TABLE `func_plan` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `project_id` bigint(20) NOT NULL COMMENT '项目ID',
  `auth` int(1) NOT NULL COMMENT '是否权限控制',
  `name` varchar(200) COLLATE utf8mb4_bin NOT NULL COMMENT '计划名称',
  `status` varchar(20) COLLATE utf8mb4_bin NOT NULL COMMENT '状态',
  `start_date` datetime NOT NULL COMMENT '计划开始时间',
  `deadline_date` datetime NOT NULL COMMENT '计划截止时间',
  `owner_id` bigint(20) NOT NULL COMMENT '负责人',
  `tester_responsibilities` json DEFAULT NULL COMMENT '测试人员与职责',
  `testing_scope` text COLLATE utf8mb4_bin COMMENT '测试范围',
  `testing_objectives` text COLLATE utf8mb4_bin COMMENT '测试目标',
  `acceptance_criteria` text COLLATE utf8mb4_bin COMMENT '验收标准',
  `other_information` text COLLATE utf8mb4_bin COMMENT '描述',
  `attachments` json DEFAULT NULL COMMENT '附件',
  `case_prefix` varchar(40) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '用例前缀',
  `review` int(1) NOT NULL COMMENT '评审标记',
  `eval_workload_method` varchar(16) COLLATE utf8mb4_bin NOT NULL COMMENT '工作量评估方式：故事点、工时',
  `deleted` int(1) NOT NULL DEFAULT '0' COMMENT '删除状态：0-未删除；1-已删除',
  `deleted_by` bigint(20) DEFAULT NULL COMMENT '删除人',
  `deleted_date` datetime DEFAULT NULL COMMENT '删除时间',
  `tenant_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '租户ID',
  `created_by` bigint(20) NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  `last_modified_by` bigint(20) NOT NULL COMMENT '最后修改人',
  `last_modified_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '最后修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_name` (`name`) USING BTREE,
  KEY `idx_start_date` (`start_date`) USING BTREE,
  KEY `idx_deadline_date` (`deadline_date`) USING BTREE,
  KEY `idx_owner_id` (`owner_id`) USING BTREE,
  KEY `idx_deleted` (`deleted`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_created_by` (`created_by`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_created_date` (`created_date`) USING BTREE,
  KEY `idx_project_id` (`project_id`),
  KEY `idx_last_modified_by` (`last_modified_by`) USING BTREE,
  KEY `idx_review` (`review`) USING BTREE,
  KEY `idx_auth` (`auth`) USING BTREE,
  FULLTEXT KEY `fx_name_description` (`name`,`other_information`) /*!50100 WITH PARSER `ngram` */
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='功能测试计划';

-- ----------------------------
-- Table structure for func_plan_auth
-- ----------------------------
DROP TABLE IF EXISTS `func_plan_auth`;
CREATE TABLE `func_plan_auth` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `plan_id` bigint(20) NOT NULL COMMENT '计划ID',
  `auth_object_type` varchar(20) COLLATE utf8mb4_bin NOT NULL COMMENT '授权对象：用户|部门|组',
  `auth_object_id` bigint(20) NOT NULL COMMENT '授权对象ID',
  `auths` json NOT NULL COMMENT '权限：查看|编辑|删除|发送请求|测试|授权|分享',
  `creator` int(1) NOT NULL COMMENT '创建人授权标识',
  `tenant_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '租户ID',
  `created_by` bigint(20) NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uidx_plan_id_auth_creator` (`plan_id`,`auth_object_id`,`auth_object_type`,`creator`) USING BTREE,
  KEY `idx_created_by` (`created_by`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_auth_object_id` (`auth_object_id`) USING BTREE,
  KEY `idx_created_date` (`created_date`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC COMMENT='功能计划授权表';

-- ----------------------------
-- Table structure for func_review
-- ----------------------------
DROP TABLE IF EXISTS `func_review`;
CREATE TABLE `func_review` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `name` varchar(200) COLLATE utf8mb4_bin NOT NULL COMMENT '名称',
  `project_id` bigint(20) NOT NULL COMMENT '项目ID',
  `plan_id` bigint(20) NOT NULL COMMENT '计划ID',
  `status` varchar(20) COLLATE utf8mb4_bin NOT NULL COMMENT '状态',
  `owner_id` bigint(20) NOT NULL COMMENT '负责人ID',
  `participant_ids` json NOT NULL COMMENT '参与人ID',
  `attachments` json DEFAULT NULL COMMENT '附件',
  `description` text COLLATE utf8mb4_bin COMMENT '说明',
  `tenant_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '租户ID',
  `created_by` bigint(20) NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  `last_modified_by` bigint(20) NOT NULL COMMENT '最后修改人',
  `last_modified_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '最后修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_plan_id` (`plan_id`) USING BTREE,
  KEY `idx_owner_id` (`owner_id`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_created_by` (`created_by`) USING BTREE,
  KEY `idx_created_date` (`created_date`) USING BTREE,
  KEY `idx_project_id` (`project_id`) USING BTREE,
  KEY `idx_last_modified_by` (`last_modified_by`) USING BTREE,
  FULLTEXT KEY `fx_name_description` (`name`,`description`) /*!50100 WITH PARSER `ngram` */
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='功能评审';

-- ----------------------------
-- Table structure for func_review_case
-- ----------------------------
DROP TABLE IF EXISTS `func_review_case`;
CREATE TABLE `func_review_case` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `plan_id` bigint(20) NOT NULL COMMENT '计划ID',
  `review_id` bigint(20) NOT NULL COMMENT '评审ID',
  `case_id` bigint(20) NOT NULL COMMENT '评审用例ID',
  `case_name` varchar(400) COLLATE utf8mb4_bin NOT NULL COMMENT '评审用例名称',
  `case_code` varchar(40) COLLATE utf8mb4_bin NOT NULL COMMENT '评审用例编码',
  `reviewed_case` json DEFAULT NULL COMMENT '评审用例信息',
  `reviewer_id` bigint(20) DEFAULT NULL COMMENT '评审人ID',
  `review_date` datetime DEFAULT NULL COMMENT '评审时间',
  `review_status` varchar(20) COLLATE utf8mb4_bin NOT NULL COMMENT '评审状态',
  `review_remark` varchar(400) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '评审备注',
  `created_by` bigint(20) NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_created_by` (`created_by`) USING BTREE,
  KEY `idx_created_date` (`created_date`) USING BTREE,
  KEY `idx_case_id` (`case_id`) USING BTREE,
  KEY `idx_plan_id` (`plan_id`) USING BTREE,
  KEY `idx_review_id` (`review_id`) USING BTREE,
  KEY `idx_review_date` (`review_date`) USING BTREE,
  KEY `idx_review_status` (`review_status`) USING BTREE,
  FULLTEXT KEY `fx_case_name_code` (`case_name`,`case_code`) /*!50100 WITH PARSER `ngram` */
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='功能评审用例';

-- ----------------------------
-- Table structure for func_review_case_record
-- ----------------------------
DROP TABLE IF EXISTS `func_review_case_record`;
CREATE TABLE `func_review_case_record` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `plan_id` bigint(20) NOT NULL COMMENT '计划ID',
  `review_case_id` bigint(20) NOT NULL COMMENT '评审用例ID',
  `review_id` bigint(20) NOT NULL COMMENT '评审ID',
  `case_code` varchar(40) COLLATE utf8mb4_bin NOT NULL COMMENT '用例编码',
  `case_id` bigint(20) NOT NULL COMMENT '用例ID',
  `case_name` varchar(400) COLLATE utf8mb4_bin NOT NULL COMMENT '用例名称',
  `reviewed_case` json DEFAULT NULL COMMENT '用例信息',
  `reviewer_id` bigint(20) DEFAULT NULL COMMENT '评审人ID',
  `review_date` datetime DEFAULT NULL COMMENT '评审时间',
  `review_status` varchar(20) COLLATE utf8mb4_bin NOT NULL COMMENT '评审状态',
  `review_remark` varchar(400) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '评审备注',
  `created_by` bigint(20) NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_created_by` (`created_by`) USING BTREE,
  KEY `idx_created_date` (`created_date`) USING BTREE,
  KEY `idx_case_id` (`case_id`) USING BTREE,
  KEY `idx_plan_id` (`plan_id`) USING BTREE,
  KEY `idx_review_id` (`review_id`) USING BTREE,
  KEY `idx_review_date` (`review_date`) USING BTREE,
  KEY `idx_review_status` (`review_status`) USING BTREE,
  KEY `idx_reviewed_case_id` (`review_case_id`) USING BTREE,
  FULLTEXT KEY `fx_case_name_code` (`case_name`,`case_code`) /*!50100 WITH PARSER `ngram` */
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='功能评审用例记录';

-- ----------------------------
-- Table structure for func_trash
-- ----------------------------
DROP TABLE IF EXISTS `func_trash`;
CREATE TABLE `func_trash` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `project_id` bigint(20) NOT NULL COMMENT '项目ID',
  `plan_id` bigint(20) DEFAULT NULL COMMENT '计划ID',
  `target_type` varchar(16) COLLATE utf8mb4_bin NOT NULL COMMENT '计划或用例',
  `target_id` bigint(20) NOT NULL COMMENT '计划或用例ID',
  `target_name` varchar(100) COLLATE utf8mb4_bin NOT NULL COMMENT '名称',
  `case_type` varchar(16) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '用例类型',
  `tenant_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '租户ID',
  `created_by` bigint(20) NOT NULL DEFAULT '0' COMMENT '创建人',
  `deleted_by` bigint(20) NOT NULL COMMENT '删除人',
  `deleted_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uidx_target_type_id` (`target_id`,`target_type`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_created_by` (`created_by`) USING BTREE,
  KEY `idx_project_id` (`project_id`),
  KEY `idx_deleted_date` (`deleted_date`),
  KEY `idx_plan_id` (`plan_id`) USING BTREE,
  FULLTEXT KEY `fx_target_name` (`target_name`) /*!50100 WITH PARSER `ngram` */
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC COMMENT='功能回收站';

-- ----------------------------
-- Table structure for indicator_func
-- ----------------------------
DROP TABLE IF EXISTS `indicator_func`;
CREATE TABLE `indicator_func` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `tenant_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '租户ID',
  `target_id` bigint(20) NOT NULL COMMENT '服务ID/接口ID',
  `target_type` varchar(40) COLLATE utf8mb4_bin NOT NULL COMMENT '服务/接口',
  `smoke` int(1) DEFAULT NULL COMMENT '是否开启冒烟测试配置',
  `smoke_check_setting` varchar(32) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '冒烟测试配置',
  `user_defined_smoke_assertion` json DEFAULT NULL COMMENT '自定义冒烟测试配置',
  `security` int(1) DEFAULT NULL COMMENT '是否开启安全测试配置',
  `security_check_setting` varchar(32) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '安全测试配置',
  `user_defined_security_assertion` json DEFAULT NULL COMMENT '自定义安全测试配置',
  `created_by` bigint(20) NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uidx_target_id` (`target_id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_target_type` (`target_type`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='功能指标';

-- ----------------------------
-- Table structure for indicator_perf
-- ----------------------------
DROP TABLE IF EXISTS `indicator_perf`;
CREATE TABLE `indicator_perf` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `tenant_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '租户ID',
  `target_id` bigint(20) NOT NULL COMMENT '服务/场景/接口ID',
  `target_type` varchar(40) COLLATE utf8mb4_bin NOT NULL COMMENT '接口/场景',
  `threads` int(11) DEFAULT NULL COMMENT '并发数',
  `duration` varchar(32) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '测试时长',
  `ramp_up_threads` int(11) DEFAULT NULL COMMENT '增压并发数',
  `ramp_up_interval` varchar(32) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '增压测试时长',
  `art` bigint(20) DEFAULT NULL COMMENT '平均响应时长',
  `percentile` varchar(16) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '百分位',
  `tps` bigint(11) DEFAULT NULL COMMENT '每秒事务数',
  `error_rate` double(13,10) DEFAULT NULL COMMENT '错误率',
  `created_by` bigint(20) NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uidx_target_id` (`target_id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_target_type` (`target_type`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='性能指标';

-- ----------------------------
-- Table structure for indicator_stability
-- ----------------------------
DROP TABLE IF EXISTS `indicator_stability`;
CREATE TABLE `indicator_stability` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `tenant_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '租户ID',
  `target_id` bigint(20) NOT NULL COMMENT '场景/接口ID',
  `target_type` varchar(40) COLLATE utf8mb4_bin NOT NULL COMMENT '接口/场景',
  `threads` int(11) DEFAULT NULL COMMENT '并发数',
  `duration` varchar(32) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '负载时间',
  `art` bigint(20) DEFAULT NULL COMMENT '平均响应时长',
  `percentile` varchar(16) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '百分位',
  `tps` bigint(11) DEFAULT NULL COMMENT '每秒事务数',
  `error_rate` double(13,10) DEFAULT NULL COMMENT '错误率',
  `cpu` double(8,2) DEFAULT NULL COMMENT 'cpu',
  `memory` double(8,2) DEFAULT NULL COMMENT '内存',
  `disk` double(8,2) DEFAULT NULL COMMENT '磁盘',
  `network` double(8,2) DEFAULT NULL COMMENT '网络',
  `created_by` bigint(20) NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uidx_target_id` (`target_id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_target_type` (`target_type`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='稳定性指标';

-- ----------------------------
-- Table structure for instance
-- ----------------------------
DROP TABLE IF EXISTS `instance`;
CREATE TABLE `instance` (
  `pk` varchar(40) COLLATE utf8mb4_bin NOT NULL,
  `id` bigint(21) NOT NULL,
  `host` varchar(160) COLLATE utf8mb4_bin NOT NULL DEFAULT '',
  `port` varchar(40) COLLATE utf8mb4_bin NOT NULL DEFAULT '',
  `instance_type` varchar(40) COLLATE utf8mb4_bin NOT NULL DEFAULT '',
  `create_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00',
  `last_modified_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00',
  PRIMARY KEY (`pk`) USING BTREE,
  UNIQUE KEY `uidx_host_port` (`host`,`port`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for mock_apis
-- ----------------------------
DROP TABLE IF EXISTS `mock_apis`;
CREATE TABLE `mock_apis` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `project_id` bigint(20) NOT NULL COMMENT '项目ID',
  `summary` varchar(400) COLLATE utf8mb4_bin NOT NULL COMMENT '名称',
  `description` text COLLATE utf8mb4_bin COMMENT '描述',
  `source` varchar(16) COLLATE utf8mb4_bin NOT NULL COMMENT '接口来源',
  `import_source` varchar(16) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '导入来源',
  `method` varchar(16) COLLATE utf8mb4_bin NOT NULL DEFAULT '0' COMMENT '请求方式',
  `endpoint` varchar(800) COLLATE utf8mb4_bin NOT NULL COMMENT '接口路径',
  `mock_service_id` bigint(20) NOT NULL COMMENT 'Mock服务ID',
  `assoc_service_id` bigint(20) DEFAULT NULL COMMENT '关联服务ID',
  `assoc_apis_id` bigint(20) DEFAULT NULL COMMENT '关联接口ID',
  `request_num` bigint(20) NOT NULL DEFAULT '0' COMMENT '调用次数',
  `pushback_num` bigint(20) NOT NULL DEFAULT '0' COMMENT '回推数',
  `simulate_error_num` bigint(20) NOT NULL DEFAULT '0' COMMENT '模拟错误数',
  `success_num` bigint(20) NOT NULL COMMENT '成功数',
  `exception_num` bigint(20) NOT NULL COMMENT '异常数',
  `last_exception_cause` varchar(400) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '最后异常原因',
  `tenant_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '租户ID',
  `created_by` bigint(20) NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  `last_modified_by` bigint(20) NOT NULL COMMENT '最后修改人',
  `last_modified_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '最后修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uidx_assoc_apis_id` (`assoc_apis_id`) USING BTREE,
  KEY `idx_mock_service_id` (`mock_service_id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_created_by` (`created_by`) USING BTREE,
  KEY `idx_created_date` (`created_date`) USING BTREE,
  KEY `idx_simulation_error_num` (`simulate_error_num`) USING BTREE,
  KEY `idx_success_num` (`success_num`) USING BTREE,
  KEY `idx_exception_num` (`exception_num`) USING BTREE,
  KEY `idx_assoc_project_id` (`assoc_service_id`) USING BTREE,
  KEY `idx_endpoint` (`endpoint`(32)) USING BTREE,
  KEY `idx_summary` (`summary`) USING BTREE,
  KEY `idx_pushback_num` (`pushback_num`) USING BTREE,
  KEY `idx_request_num` (`request_num`) USING BTREE,
  KEY `idx_project_id` (`project_id`),
  FULLTEXT KEY `fx_summary_endpoint` (`summary`,`endpoint`) /*!50100 WITH PARSER `ngram` */
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='Mock接口';

-- ----------------------------
-- Table structure for mock_apis_log
-- ----------------------------
DROP TABLE IF EXISTS `mock_apis_log`;
CREATE TABLE `mock_apis_log` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `request_id` varchar(40) COLLATE utf8mb4_bin NOT NULL COMMENT '请求ID',
  `exception_message` varchar(200) COLLATE utf8mb4_bin DEFAULT NULL COMMENT 'Mock异常信息',
  `remote` varchar(100) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '客户端地址',
  `mock_service_id` bigint(20) NOT NULL COMMENT 'Mock服务ID',
  `mock_apis_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT 'Mock接口ID',
  `summary` varchar(400) COLLATE utf8mb4_bin DEFAULT '' COMMENT '接口名称',
  `method` varchar(16) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '请求方法',
  `endpoint` varchar(800) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '请求路径',
  `pushback` int(1) NOT NULL COMMENT '是否回退请求',
  `pushback_request_id` varchar(32) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '回退请求ID',
  `query_parameters` text COLLATE utf8mb4_bin COMMENT '查询参数',
  `request_headers` text COLLATE utf8mb4_bin COMMENT '请求头',
  `request_content_encoding` varchar(40) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '请求体编码',
  `request_body` text COLLATE utf8mb4_bin COMMENT '请求体',
  `request_date` datetime NOT NULL COMMENT '服务接收到请求时间',
  `response_status` int(11) NOT NULL COMMENT '服务发送响应时间',
  `response_headers` text COLLATE utf8mb4_bin COMMENT '响应头',
  `response_body` text COLLATE utf8mb4_bin COMMENT '响应体',
  `response_size` int(11) DEFAULT '0' COMMENT '响应报文大小',
  `response_date` datetime DEFAULT NULL COMMENT '服务响应返回时间',
  `tenant_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '访问租户ID',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_created_date` (`created_date`) USING BTREE,
  KEY `idx_request_date` (`request_date`) USING BTREE,
  KEY `idx_mock_service_id` (`mock_service_id`) USING BTREE,
  KEY `idx_mock_apis_id` (`mock_apis_id`) USING BTREE,
  KEY `idx_method` (`method`) USING BTREE,
  KEY `idx_response_status` (`response_status`) USING BTREE,
  KEY `idx_endpoint` (`endpoint`(32)) USING BTREE,
  FULLTEXT KEY `fx_summary_uri` (`summary`,`endpoint`) /*!50100 WITH PARSER `ngram` */
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='API日志';

-- ----------------------------
-- Table structure for mock_apis_response
-- ----------------------------
DROP TABLE IF EXISTS `mock_apis_response`;
CREATE TABLE `mock_apis_response` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `project_id` bigint(20) NOT NULL COMMENT '项目ID',
  `mock_service_id` bigint(20) NOT NULL COMMENT 'Mock服务ID',
  `mock_apis_id` bigint(20) NOT NULL COMMENT 'Mock接口ID',
  `name` varchar(400) COLLATE utf8mb4_bin NOT NULL COMMENT '响应名称',
  `match` json DEFAULT NULL COMMENT '响应匹配',
  `content` json DEFAULT NULL COMMENT '响应',
  `enable_pushback` int(1) NOT NULL DEFAULT '0',
  `pushback` json DEFAULT NULL COMMENT '响应回推',
  `tenant_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '租户ID',
  `created_by` bigint(20) NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  `last_modified_by` bigint(20) NOT NULL COMMENT '最后修改人',
  `last_modified_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '最后修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_mock_apis_id` (`mock_apis_id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_mock_service_id` (`mock_service_id`) USING BTREE,
  KEY `idx_name` (`name`) USING BTREE,
  KEY `idx_project_id` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='Mock接口';

-- ----------------------------
-- Table structure for mock_service
-- ----------------------------
DROP TABLE IF EXISTS `mock_service`;
CREATE TABLE `mock_service` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `project_id` bigint(20) NOT NULL COMMENT '项目ID',
  `name` varchar(200) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '名称',
  `source` varchar(16) COLLATE utf8mb4_bin NOT NULL COMMENT '服务来源',
  `import_source` varchar(16) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '导入来源',
  `auth` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否有权限控制',
  `assoc_service_id` bigint(20) DEFAULT NULL COMMENT '关联服务ID',
  `node_id` bigint(20) NOT NULL COMMENT '关联节点',
  `node_ip` varchar(20) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '节点IP',
  `service_port` int(11) NOT NULL COMMENT '运行服务端口',
  `service_domain` varchar(200) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '服务访问域名',
  `service_dns_id` bigint(20) DEFAULT NULL COMMENT '域名解析记录ID',
  `api_security` json DEFAULT NULL COMMENT 'Api 安全',
  `api_cors` json DEFAULT NULL COMMENT '接口跨域配置',
  `setting` json NOT NULL COMMENT '服务配置',
  `ext_search_merge` varchar(20) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '扩展搜索合并列',
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `created_by` bigint(20) NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  `last_modified_by` bigint(20) NOT NULL COMMENT '最后修改人',
  `last_modified_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '最后修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `idx_created_date` (`created_date`) USING BTREE,
  KEY `idx_name` (`name`) USING BTREE,
  KEY `idx_node_id` (`node_id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_create_by` (`created_by`) USING BTREE,
  KEY `uidx_assoc_project_id` (`assoc_service_id`) USING BTREE,
  KEY `idx_source` (`source`) USING BTREE,
  KEY `idx_node_ip` (`node_ip`) USING BTREE,
  KEY `idx_project_id` (`project_id`),
  KEY `idx_auth` (`auth`) USING BTREE,
  FULLTEXT KEY `fx_name_ext` (`name`,`ext_search_merge`) /*!50100 WITH PARSER `ngram` */
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='Mock服务';

-- ----------------------------
-- Table structure for mock_service_auth
-- ----------------------------
DROP TABLE IF EXISTS `mock_service_auth`;
CREATE TABLE `mock_service_auth` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `mock_service_id` bigint(20) NOT NULL COMMENT 'Mock服务ID',
  `auth_object_type` varchar(20) COLLATE utf8mb4_bin NOT NULL COMMENT '授权对象：用户|部门|组',
  `auth_object_id` bigint(20) NOT NULL COMMENT '授权对象ID',
  `auths` json NOT NULL COMMENT '权限：查看|编辑|删除|添加Mock接口|授权',
  `creator` int(1) NOT NULL COMMENT '创建人授权标识',
  `tenant_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '租户ID',
  `created_by` bigint(20) NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uidx_service_id_auth_object_id_type_creator` (`mock_service_id`,`auth_object_id`,`auth_object_type`,`creator`) USING BTREE,
  KEY `idx_service_id` (`mock_service_id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_auth_object_id` (`auth_object_id`) USING BTREE,
  KEY `idx_created_by` (`created_by`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='服务权限';

-- ----------------------------
-- Table structure for module
-- ----------------------------
DROP TABLE IF EXISTS `module`;
CREATE TABLE `module` (
  `id` bigint(20) NOT NULL COMMENT 'ID',
  `name` varchar(100) COLLATE utf8mb4_bin NOT NULL COMMENT '模块名称',
  `project_id` bigint(20) NOT NULL COMMENT '项目ID',
  `pid` bigint(20) NOT NULL DEFAULT '-1' COMMENT '上级功能ID',
  `sequence` int(11) NOT NULL DEFAULT '1' COMMENT '序号，值越小越靠前',
  `tenant_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '所属租户ID',
  `created_by` bigint(20) NOT NULL DEFAULT '-1' COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  `last_modified_by` bigint(20) NOT NULL DEFAULT '-1' COMMENT '最后修改人',
  `last_modified_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '最后修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_create_date` (`created_date`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_created_by` (`created_by`) USING BTREE,
  KEY `idx_project_id` (`project_id`),
  KEY `idx_pid` (`pid`),
  KEY `idx_sequence` (`sequence`),
  FULLTEXT KEY `fx_name` (`name`) /*!50100 WITH PARSER `ngram` */
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='模块';

-- ----------------------------
-- Table structure for node
-- ----------------------------
DROP TABLE IF EXISTS `node`;
CREATE TABLE `node` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `name` varchar(200) COLLATE utf8mb4_bin NOT NULL COMMENT '名称',
  `ip` varchar(15) COLLATE utf8mb4_bin DEFAULT NULL COMMENT 'IP',
  `public_ip` varchar(15) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '公网IP',
  `region_id` varchar(20) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '实例所属的地域ID',
  `domain` varchar(200) COLLATE utf8mb4_bin DEFAULT '' COMMENT '域名',
  `spec` json DEFAULT NULL COMMENT '节点规格',
  `source` varchar(16) COLLATE utf8mb4_bin NOT NULL COMMENT '来源',
  `free` int(1) NOT NULL COMMENT '是否免费',
  `enabled` int(1) NOT NULL COMMENT '是否有效',
  `username` varchar(200) COLLATE utf8mb4_bin DEFAULT '' COMMENT '登录用户名',
  `password` varchar(800) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '登录用户密码',
  `ssh_port` int(11) DEFAULT NULL COMMENT 'SSH端口',
  `instance_id` varchar(80) COLLATE utf8mb4_bin DEFAULT '' COMMENT '实例ID',
  `instance_name` varchar(100) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '实例名称',
  `instance_expired_date` datetime DEFAULT NULL COMMENT '实例到期时间',
  `deleted` int(1) DEFAULT NULL COMMENT '是否删除:true 是，false 否',
  `order_id` bigint(20) DEFAULT NULL COMMENT '订单ID，只针对购买节',
  `expired` int(1) DEFAULT NULL COMMENT '是否过期',
  `charge_type` varchar(11) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '付费方式（包年包月：PrePaid、按量付费:PostPaid）',
  `install_agent` int(1) DEFAULT NULL COMMENT '是否安装代理',
  `sync` int(1) DEFAULT '0' COMMENT '是否同步节点信息',
  `ext_search_merge` varchar(20) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '扩展搜索合并列',
  `tenant_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '租户ID',
  `created_by` bigint(20) NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL COMMENT '创建时间',
  `last_modified_by` bigint(20) NOT NULL COMMENT '最后修改人',
  `last_modified_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '最后修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uidx_ip_tenant_id` (`ip`,`tenant_id`) USING BTREE,
  KEY `idx_name` (`name`) USING BTREE,
  KEY `idx_created_date` (`created_date`) USING BTREE,
  KEY `idx_source` (`source`) USING BTREE,
  KEY `idx_order_id` (`order_id`) USING BTREE,
  KEY `idx_instance_expired_date` (`instance_expired_date`) USING BTREE,
  KEY `idx_instance_id` (`instance_id`) USING BTREE,
  KEY `idx_created_by` (`created_by`) USING BTREE,
  KEY `idx_free` (`free`) USING BTREE,
  KEY `idx_install_agent` (`install_agent`) USING BTREE,
  KEY `idx_enabled` (`enabled`) USING BTREE,
  KEY `idx_deleted` (`deleted`) USING BTREE,
  KEY `idx_sync` (`sync`) USING BTREE,
  FULLTEXT KEY `fx_name_ext` (`name`,`ext_search_merge`) /*!50100 WITH PARSER `ngram` */
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC COMMENT='节点';

-- ----------------------------
-- Table structure for node_ctrl
-- ----------------------------
DROP TABLE IF EXISTS `node_ctrl`;
CREATE TABLE `node_ctrl` (
  `id` bigint(20) NOT NULL,
  `node_id` bigint(20) NOT NULL,
  `ctrl_instance_ip` varchar(16) COLLATE utf8mb4_bin NOT NULL,
  `created_date` datetime NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uidx_node_id_ctrl_instance_ip` (`node_id`,`ctrl_instance_ip`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='节点控制';

-- ----------------------------
-- Table structure for node_domain
-- ----------------------------
DROP TABLE IF EXISTS `node_domain`;
CREATE TABLE `node_domain` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `name` varchar(200) COLLATE utf8mb4_bin NOT NULL COMMENT '名称',
  `status` varchar(20) COLLATE utf8mb4_bin NOT NULL COMMENT '状态1正常0异常',
  `created_by` bigint(20) NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL COMMENT '创建时间',
  `last_modified_by` bigint(20) NOT NULL COMMENT '最后修改人',
  `last_modified_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '最后修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_name` (`name`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_created_by` (`created_by`) USING BTREE,
  KEY `idx_created_date` (`created_date`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='节点域名';

-- ----------------------------
-- Table structure for node_domain_dns
-- ----------------------------
DROP TABLE IF EXISTS `node_domain_dns`;
CREATE TABLE `node_domain_dns` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `domain_id` bigint(20) NOT NULL COMMENT '主域名ID',
  `type` varchar(16) COLLATE utf8mb4_bin NOT NULL COMMENT '类型',
  `name` varchar(200) COLLATE utf8mb4_bin NOT NULL COMMENT '子域名前缀名',
  `line` varchar(16) COLLATE utf8mb4_bin NOT NULL COMMENT '解析线路',
  `value` varchar(800) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '记录值',
  `ttl` int(11) NOT NULL COMMENT '生存时间，单位秒',
  `status` varchar(20) COLLATE utf8mb4_bin NOT NULL COMMENT '状态',
  `cloud_record_id` varchar(64) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '供应商云记录ID',
  `tenant_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '租户ID',
  `created_by` bigint(20) NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL COMMENT '创建时间',
  `last_modified_by` bigint(20) NOT NULL COMMENT '最后修改人',
  `last_modified_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '最后修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_name` (`name`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_created_by` (`created_by`) USING BTREE,
  KEY `idx_created_date` (`created_date`) USING BTREE,
  KEY `idx_domain_id` (`domain_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='节点域名解析';

-- ----------------------------
-- Table structure for node_info
-- ----------------------------
DROP TABLE IF EXISTS `node_info`;
CREATE TABLE `node_info` (
  `id` bigint(20) NOT NULL,
  `tenant_id` bigint(20) NOT NULL,
  `info` varchar(320) CHARACTER SET ascii DEFAULT NULL,
  `os` varchar(120) CHARACTER SET ascii DEFAULT NULL,
  `agent_installed` int(1) DEFAULT NULL,
  `agent_auth` json DEFAULT NULL,
  `last_modified_date` datetime NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='节点信息';

-- ----------------------------
-- Table structure for node_role
-- ----------------------------
DROP TABLE IF EXISTS `node_role`;
CREATE TABLE `node_role` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `node_id` bigint(20) NOT NULL COMMENT '节点ID',
  `role` varchar(32) COLLATE utf8mb4_bin NOT NULL COMMENT '角色名称',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_node_id` (`role`) USING BTREE,
  KEY `idx_created_by` (`node_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='节点角色';

-- ----------------------------
-- Table structure for project
-- ----------------------------
DROP TABLE IF EXISTS `project`;
CREATE TABLE `project` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `type` varchar(20) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '类型',
  `name` varchar(100) COLLATE utf8mb4_bin NOT NULL COMMENT '名称',
  `avatar` varchar(400) COLLATE utf8mb4_bin DEFAULT '' COMMENT '图标',
  `description` text COLLATE utf8mb4_bin COMMENT '描述',
  `owner_id` bigint(20) NOT NULL COMMENT '负责人ID',
  `start_date` datetime DEFAULT NULL COMMENT '计划开始时间',
  `deadline_date` datetime DEFAULT NULL COMMENT '计划截止时间',
  `deleted` int(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  `deleted_by` bigint(20) DEFAULT NULL COMMENT '删除人',
  `deleted_date` datetime DEFAULT NULL COMMENT '删除时间',
  `tenant_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '租户ID',
  `created_by` bigint(20) NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  `last_modified_by` bigint(20) NOT NULL COMMENT '最后修改人',
  `last_modified_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '最后修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_name` (`name`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_created_date` (`created_date`) USING BTREE,
  KEY `idx_type` (`type`) USING BTREE,
  KEY `idx_deleted` (`deleted`) USING BTREE,
  FULLTEXT KEY `fx_name_description` (`name`,`description`) /*!50100 WITH PARSER `ngram` */
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='项目表';

-- ----------------------------
-- Table structure for project_members
-- ----------------------------
DROP TABLE IF EXISTS `project_members`;
CREATE TABLE `project_members` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `project_id` bigint(20) NOT NULL COMMENT '项目ID',
  `member_type` varchar(20) COLLATE utf8mb4_bin NOT NULL COMMENT '成员类型：用户|部门|组',
  `member_id` bigint(20) NOT NULL COMMENT '成员ID',
  `tenant_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '租户ID',
  `created_by` bigint(20) NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uidx_project_id_member` (`project_id`,`member_id`,`member_type`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_member_id` (`member_id`) USING BTREE,
  KEY `idx_created_by` (`created_by`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='项目成员';

-- ----------------------------
-- Table structure for project_trash
-- ----------------------------
DROP TABLE IF EXISTS `project_trash`;
CREATE TABLE `project_trash` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `target_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '删除项目ID',
  `target_name` varchar(100) COLLATE utf8mb4_bin NOT NULL COMMENT '删除项目名称',
  `tenant_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '租户ID',
  `created_by` bigint(20) NOT NULL DEFAULT '-1' COMMENT '创建人',
  `deleted_by` bigint(20) NOT NULL COMMENT '删除人',
  `deleted_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uidx_target_id` (`target_id`) USING BTREE,
  KEY `idx_created_by` (`created_by`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_deleted_date` (`deleted_date`),
  FULLTEXT KEY `fx_target_name` (`target_name`) /*!50100 WITH PARSER `ngram` */
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC COMMENT='项目回收站';

-- ----------------------------
-- Table structure for scenario
-- ----------------------------
DROP TABLE IF EXISTS `scenario`;
CREATE TABLE `scenario` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `project_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '项目ID',
  `name` varchar(200) COLLATE utf8mb4_bin NOT NULL COMMENT '名称',
  `auth` int(1) NOT NULL COMMENT '是否权限控制',
  `plugin` varchar(100) COLLATE utf8mb4_bin NOT NULL COMMENT '执行插件名称',
  `script_type` varchar(20) COLLATE utf8mb4_bin NOT NULL COMMENT '脚本类型',
  `script_id` bigint(20) NOT NULL COMMENT '脚本ID',
  `description` varchar(800) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '描述',
  `test_func` int(1) NOT NULL COMMENT '开启功能测试标志',
  `test_func_passed` int(1) DEFAULT NULL COMMENT '功能测试通过标志',
  `test_func_failure_message` varchar(1024) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '功能测试未通过原因',
  `test_perf` int(1) NOT NULL COMMENT '开启性能测试标志',
  `test_perf_passed` int(1) DEFAULT NULL COMMENT '性能测试通过标志',
  `test_perf_failure_message` varchar(1024) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '性能测试未通过原因',
  `test_stability` int(1) NOT NULL COMMENT '开启稳定性测试标志',
  `test_stability_passed` int(1) DEFAULT NULL COMMENT '稳定性测试通过标志',
  `test_stability_failure_message` varchar(1024) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '稳定性测试未通过原因',
  `ext_search_merge` varchar(20) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '扩展搜索河合并列',
  `deleted` int(1) NOT NULL DEFAULT '0' COMMENT '删除标志',
  `deleted_by` bigint(20) DEFAULT NULL COMMENT '删除人',
  `deleted_date` datetime DEFAULT NULL COMMENT '删除时间',
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `created_by` bigint(20) NOT NULL DEFAULT '-1' COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  `last_modified_by` bigint(20) NOT NULL DEFAULT '-1' COMMENT '最后修改人',
  `last_modified_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '最后修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_created_by` (`created_by`) USING BTREE,
  KEY `idx_created_date` (`created_date`) USING BTREE,
  KEY `idx_project_id` (`project_id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_plugin_name` (`plugin`) USING BTREE,
  KEY `idx_name` (`name`) USING BTREE,
  KEY `idx_script_type` (`script_type`) USING BTREE,
  KEY `idx_deleted` (`deleted`) USING BTREE,
  KEY `idx_auth` (`auth`) USING BTREE,
  FULLTEXT KEY `fx_name_description_ext` (`name`,`description`,`ext_search_merge`) /*!50100 WITH PARSER `ngram` */
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC COMMENT='场景';

-- ----------------------------
-- Table structure for scenario_auth
-- ----------------------------
DROP TABLE IF EXISTS `scenario_auth`;
CREATE TABLE `scenario_auth` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `scenario_id` bigint(20) NOT NULL COMMENT '场景ID',
  `auth_object_type` varchar(20) COLLATE utf8mb4_bin NOT NULL COMMENT '授权对象：用户|部门|组',
  `auth_object_id` bigint(20) NOT NULL COMMENT '授权对象ID',
  `auths` json NOT NULL COMMENT '权限：查看|编辑|删除|发送请求|测试|授权|分享',
  `creator` int(1) NOT NULL COMMENT '创建人授权标识',
  `tenant_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '租户ID',
  `created_by` bigint(20) NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uidx_scenario_id_auth_creator` (`scenario_id`,`auth_object_id`,`auth_object_type`,`creator`) USING BTREE,
  KEY `idx_created_by` (`created_by`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_auth_object_id` (`auth_object_id`) USING BTREE,
  KEY `idx_created_date` (`created_date`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC COMMENT='场景授权';

-- ----------------------------
-- Table structure for scenario_favourite
-- ----------------------------
DROP TABLE IF EXISTS `scenario_favourite`;
CREATE TABLE `scenario_favourite` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `project_id` bigint(20) NOT NULL COMMENT '项目ID',
  `scenario_id` bigint(20) NOT NULL COMMENT '场景ID',
  `tenant_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '租户ID',
  `created_by` bigint(20) NOT NULL DEFAULT '-1' COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_created_by` (`created_by`) USING BTREE,
  KEY `uidx_scenario_created_id` (`scenario_id`,`created_by`) USING BTREE,
  KEY `idx_scenario_id` (`scenario_id`) USING BTREE,
  KEY `idx_project_id` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC COMMENT='场景收藏';

-- ----------------------------
-- Table structure for scenario_follow
-- ----------------------------
DROP TABLE IF EXISTS `scenario_follow`;
CREATE TABLE `scenario_follow` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `project_id` bigint(20) NOT NULL COMMENT '项目ID',
  `scenario_id` bigint(20) NOT NULL COMMENT '场景ID',
  `tenant_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '租户ID',
  `created_by` bigint(20) NOT NULL DEFAULT '-1' COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_created_by` (`created_by`) USING BTREE,
  KEY `idx_scenario_id` (`scenario_id`) USING BTREE,
  KEY `uidx_scenario_created_id` (`scenario_id`,`created_by`) USING BTREE,
  KEY `idx_project_id` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='场景关注';

-- ----------------------------
-- Table structure for scenario_monitor
-- ----------------------------
DROP TABLE IF EXISTS `scenario_monitor`;
CREATE TABLE `scenario_monitor` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `project_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '项目ID',
  `scenario_id` bigint(20) NOT NULL COMMENT '场景ID',
  `script_id` bigint(20) NOT NULL COMMENT '脚本ID',
  `name` varchar(200) COLLATE utf8mb4_bin NOT NULL COMMENT '名称',
  `description` varchar(800) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '描述',
  `status` varchar(20) COLLATE utf8mb4_bin NOT NULL COMMENT '状态',
  `failure_message` varchar(400) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '失败原因',
  `next_exec_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '下次执行时间',
  `last_monitor_history_id` bigint(20) DEFAULT NULL COMMENT '最后监控执行记录ID',
  `last_monitor_date` datetime DEFAULT '2001-01-01 00:00:00' COMMENT '最后监控执行时间',
  `created_at` varchar(20) COLLATE utf8mb4_bin NOT NULL COMMENT '创建时间类型',
  `time_setting` json NOT NULL COMMENT '创建时间配置',
  `server_setting` json DEFAULT NULL COMMENT '服务器配置',
  `notice_setting` json NOT NULL COMMENT '通知配置',
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `created_by` bigint(20) NOT NULL DEFAULT '-1' COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  `last_modified_by` bigint(20) NOT NULL DEFAULT '-1' COMMENT '最后修改人',
  `last_modified_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '最后修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_created_by` (`created_by`) USING BTREE,
  KEY `idx_created_date` (`created_date`) USING BTREE,
  KEY `idx_project_id` (`project_id`) USING BTREE,
  KEY `idx_tenant_id` (`last_monitor_history_id`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_plugin_name` (`tenant_id`) USING BTREE,
  KEY `idx_scenario_id` (`scenario_id`) USING BTREE,
  KEY `idx_script_id` (`script_id`) USING BTREE,
  KEY `idx_created_at` (`created_at`) USING BTREE,
  KEY `idx_next_exec_date` (`next_exec_date`) USING BTREE,
  FULLTEXT KEY `fx_name_description` (`name`,`description`) /*!50100 WITH PARSER `ngram` */
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC COMMENT='场景监控';

-- ----------------------------
-- Table structure for scenario_monitor_history
-- ----------------------------
DROP TABLE IF EXISTS `scenario_monitor_history`;
CREATE TABLE `scenario_monitor_history` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `project_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '项目ID',
  `monitor_id` bigint(20) NOT NULL COMMENT '监控ID',
  `status` varchar(20) COLLATE utf8mb4_bin NOT NULL COMMENT '状态',
  `failure_message` varchar(400) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '失败原因',
  `response_delay` int(11) DEFAULT NULL COMMENT '响应延迟',
  `exec_id` bigint(20) DEFAULT NULL COMMENT '执行ID',
  `exec_start_date` datetime DEFAULT '2001-01-01 00:00:00' COMMENT '执行开始时间',
  `exec_end_date` datetime DEFAULT '2001-01-01 00:00:00' COMMENT '执行结束时间',
  `sample_content` json DEFAULT NULL COMMENT '采样内容',
  `sample_log_content` mediumtext COLLATE utf8mb4_bin COMMENT '执行日志内容',
  `scheduling_result` json DEFAULT NULL COMMENT '调度结果',
  `created_by` bigint(20) NOT NULL DEFAULT '-1' COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_created_by` (`created_by`) USING BTREE,
  KEY `idx_created_date` (`created_date`) USING BTREE,
  KEY `idx_monitor_id` (`monitor_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC COMMENT='场景监控历史';

-- ----------------------------
-- Table structure for scenario_trash
-- ----------------------------
DROP TABLE IF EXISTS `scenario_trash`;
CREATE TABLE `scenario_trash` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `project_id` bigint(20) NOT NULL COMMENT '项目ID',
  `target_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '删除对象ID',
  `target_name` varchar(100) COLLATE utf8mb4_bin NOT NULL COMMENT '名称',
  `tenant_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '租户ID',
  `created_by` bigint(20) NOT NULL DEFAULT '-1' COMMENT '创建人',
  `deleted_by` bigint(20) NOT NULL COMMENT '删除人',
  `deleted_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uidx_target_id` (`target_id`) USING BTREE,
  KEY `idx_tenant_id` (`target_id`) USING BTREE,
  KEY `idx_created_by` (`created_by`) USING BTREE,
  KEY `idx_project_id` (`project_id`),
  KEY `idx_deleted_date` (`deleted_date`),
  FULLTEXT KEY `fx_target_name` (`target_name`) /*!50100 WITH PARSER `ngram` */
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC COMMENT='场景回收站';

-- ----------------------------
-- Table structure for script
-- ----------------------------
DROP TABLE IF EXISTS `script`;
CREATE TABLE `script` (
  `id` bigint(20) NOT NULL,
  `project_id` bigint(20) NOT NULL COMMENT '项目ID',
  `service_id` bigint(20) DEFAULT NULL COMMENT '接口服务ID',
  `name` varchar(500) COLLATE utf8mb4_bin NOT NULL COMMENT '名称',
  `type` varchar(20) COLLATE utf8mb4_bin NOT NULL COMMENT '类型',
  `source` varchar(20) COLLATE utf8mb4_bin NOT NULL COMMENT '来源',
  `source_id` bigint(20) DEFAULT NULL COMMENT '来源资源ID',
  `auth` int(1) NOT NULL DEFAULT '0' COMMENT '授权控制',
  `description` varchar(800) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '描述',
  `plugin` varchar(100) COLLATE utf8mb4_bin NOT NULL COMMENT '执行插件名称',
  `content` mediumtext COLLATE utf8mb4_bin COMMENT '脚本内容-YAML格式',
  `ext_search_merge` varchar(20) COLLATE utf8mb4_bin NOT NULL COMMENT 'ID搜索扩展',
  `tenant_id` bigint(20) NOT NULL COMMENT '租户ID',
  `created_by` bigint(20) NOT NULL DEFAULT '-1' COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  `last_modified_by` bigint(20) NOT NULL DEFAULT '-1' COMMENT '最后修改人',
  `last_modified_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '最后修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_created_by` (`created_by`) USING BTREE,
  KEY `idx_created_date` (`created_date`) USING BTREE,
  KEY `idx_name` (`name`) USING BTREE,
  KEY `idx_type_source` (`type`,`source`) USING BTREE,
  KEY `idx_source` (`source`) USING BTREE,
  KEY `idx_plugin` (`plugin`) USING BTREE,
  KEY `idx_tenant_id_source` (`tenant_id`,`source`) USING BTREE,
  KEY `idx_last_modified_by` (`last_modified_by`) USING BTREE,
  KEY `idx_last_modified_date` (`last_modified_date`) USING BTREE,
  KEY `idx_project_id` (`project_id`),
  KEY `idx_source_id` (`source_id`) USING BTREE,
  KEY `idx_service_id` (`service_id`),
  KEY `idx_auth` (`auth`) USING BTREE,
  FULLTEXT KEY `fx_name_descript_ext_plugin` (`name`,`ext_search_merge`,`description`,`plugin`) /*!50100 WITH PARSER `ngram` */
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='脚本';

-- ----------------------------
-- Table structure for script_auth
-- ----------------------------
DROP TABLE IF EXISTS `script_auth`;
CREATE TABLE `script_auth` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `script_id` bigint(20) NOT NULL COMMENT '场景ID',
  `auth_object_type` varchar(20) COLLATE utf8mb4_bin NOT NULL COMMENT '授权对象',
  `auth_object_id` bigint(20) NOT NULL COMMENT '授权对象ID',
  `auths` json NOT NULL COMMENT '权限',
  `creator` int(1) NOT NULL COMMENT '创建人授权标识',
  `tenant_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '租户ID',
  `created_by` bigint(20) NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uidx_script_id_auth_creator` (`script_id`,`auth_object_id`,`auth_object_type`,`creator`) USING BTREE,
  KEY `idx_created_by` (`created_by`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_auth_object_id` (`auth_object_id`) USING BTREE,
  KEY `idx_scenario_id` (`script_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC COMMENT='脚本授权';

-- ----------------------------
-- Table structure for script_tag
-- ----------------------------
DROP TABLE IF EXISTS `script_tag`;
CREATE TABLE `script_tag` (
  `id` bigint(20) NOT NULL COMMENT 'ID',
  `name` varchar(100) COLLATE utf8mb4_bin NOT NULL COMMENT '标签名称',
  `tenant_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '所属租户ID',
  `script_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '所属脚本ID',
  `created_by` bigint(20) NOT NULL DEFAULT '-1' COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uidx_script_id_name` (`script_id`,`name`) USING BTREE,
  KEY `idx_create_date` (`created_date`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  FULLTEXT KEY `fx_name` (`name`) /*!50100 WITH PARSER `ngram` */
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='脚本标签对象';

-- ----------------------------
-- Table structure for services
-- ----------------------------
DROP TABLE IF EXISTS `services`;
CREATE TABLE `services` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `project_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '项目ID',
  `name` varchar(100) COLLATE utf8mb4_bin NOT NULL COMMENT '名称',
  `auth` int(1) NOT NULL DEFAULT '0' COMMENT '授权控制',
  `status` varchar(20) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '接口状态',
  `source` varchar(10) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '服务来源：添加；导入；自动同步；',
  `import_source` varchar(16) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '导入来源类型',
  `deleted` int(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  `deleted_by` bigint(20) DEFAULT NULL COMMENT '删除人',
  `deleted_date` datetime DEFAULT NULL COMMENT '删除时间',
  `tenant_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '租户ID',
  `created_by` bigint(20) NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  `last_modified_by` bigint(20) NOT NULL COMMENT '最后修改人',
  `last_modified_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '最后修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_name` (`name`) USING BTREE,
  KEY `idx_pid` (`project_id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_auth` (`auth`) USING BTREE,
  KEY `idx_deleted` (`deleted`) USING BTREE,
  KEY `idx_created_date` (`created_date`) USING BTREE,
  KEY `idx_source` (`source`) USING BTREE,
  KEY `idx_project_id` (`project_id`),
  FULLTEXT KEY `fx_name_code` (`name`) /*!50100 WITH PARSER `ngram` */
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='服务表';

-- ----------------------------
-- Table structure for services_auth
-- ----------------------------
DROP TABLE IF EXISTS `services_auth`;
CREATE TABLE `services_auth` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `service_id` bigint(20) NOT NULL COMMENT '服务ID',
  `auth_object_type` varchar(20) COLLATE utf8mb4_bin NOT NULL COMMENT '授权对象：用户|部门|组',
  `auth_object_id` bigint(20) NOT NULL COMMENT '授权对象ID',
  `auths` json NOT NULL COMMENT '权限：查看|编辑|删除|发送请求|测试|授权|分享',
  `creator` int(11) NOT NULL COMMENT '创建人授权标识',
  `tenant_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '租户ID',
  `created_by` bigint(20) NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uidx_services_auth_object_id_type_creator` (`service_id`,`auth_object_id`,`auth_object_type`,`creator`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_auth_object_id` (`auth_object_id`) USING BTREE,
  KEY `idx_created_by` (`created_by`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='项目权限';

-- ----------------------------
-- Table structure for services_comp
-- ----------------------------
DROP TABLE IF EXISTS `services_comp`;
CREATE TABLE `services_comp` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `service_id` bigint(20) NOT NULL COMMENT '服务ID',
  `type` varchar(20) COLLATE utf8mb4_bin NOT NULL COMMENT '组件类型',
  `key` varchar(400) COLLATE utf8mb4_bin NOT NULL COMMENT '组件名称',
  `ref` varchar(800) COLLATE utf8mb4_bin NOT NULL COMMENT '组件路径',
  `model` text COLLATE utf8mb4_bin NOT NULL COMMENT '组件内容',
  `description` varchar(2000) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '组件描述',
  `schema_hash` int(11) NOT NULL COMMENT '原模型哈希版本',
  `tenant_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '租户ID',
  `last_modified_by` bigint(20) NOT NULL COMMENT '最后修改人',
  `last_modified_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '最后修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_project_id` (`service_id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_type` (`type`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='项目组件';

-- ----------------------------
-- Table structure for services_schema
-- ----------------------------
DROP TABLE IF EXISTS `services_schema`;
CREATE TABLE `services_schema` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `project_id` bigint(20) NOT NULL COMMENT '项目ID',
  `service_id` bigint(20) NOT NULL COMMENT '服务ID',
  `openapi` varchar(16) COLLATE utf8mb4_bin NOT NULL COMMENT 'OpenApi版本',
  `info` json NOT NULL COMMENT '基本信息',
  `external_docs` json DEFAULT NULL COMMENT '其他外部文档',
  `servers` json DEFAULT NULL COMMENT '服务器',
  `security` json DEFAULT NULL COMMENT 'API安全机制声明',
  `tags` json DEFAULT NULL COMMENT '标签组',
  `extensions` json DEFAULT NULL COMMENT '扩展信息',
  `spec_version` varchar(20) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '规范版本',
  `tenant_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '租户ID',
  `last_modified_by` bigint(20) NOT NULL COMMENT '最后修改人',
  `last_modified_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '最后修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `udx_project_id` (`service_id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='项目配置';

-- ----------------------------
-- Table structure for services_sync
-- ----------------------------
DROP TABLE IF EXISTS `services_sync`;
CREATE TABLE `services_sync` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `service_id` bigint(20) NOT NULL COMMENT '服务ID',
  `name` varchar(100) COLLATE utf8mb4_bin NOT NULL COMMENT '配置名称',
  `api_docs_url` varchar(800) COLLATE utf8mb4_bin NOT NULL COMMENT '同步配置地址',
  `strategy_when_duplicated` varchar(20) COLLATE utf8mb4_bin NOT NULL COMMENT '同步数据重复时处理策略',
  `delete_when_not_existed` int(11) NOT NULL COMMENT '同步数据不存在时是否删除本地',
  `auths` json DEFAULT NULL COMMENT '同步认证配置',
  `sync_success` int(1) DEFAULT NULL COMMENT '最后一次同步成功标志',
  `sync_failure_cause` varchar(200) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '最后一次同步失败原因',
  `last_sync_date` datetime DEFAULT NULL COMMENT '最后一次同步时间',
  `tenant_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '租户ID',
  `last_modified_by` bigint(20) NOT NULL COMMENT '最后修改人',
  `last_modified_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '最后修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uidx_project_id_name` (`service_id`,`name`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='项目同步';

-- ----------------------------
-- Table structure for shard_tables
-- ----------------------------
DROP TABLE IF EXISTS `shard_tables`;
CREATE TABLE `shard_tables` (
  `id` bigint(20) NOT NULL,
  `tenant_id` bigint(20) NOT NULL,
  `table_name` varchar(80) CHARACTER SET ascii NOT NULL,
  `db_index` int(11) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='分表记录';

-- ----------------------------
-- Table structure for software_version
-- ----------------------------
DROP TABLE IF EXISTS `software_version`;
CREATE TABLE `software_version` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `project_id` bigint(20) NOT NULL COMMENT '项目ID',
  `name` varchar(40) COLLATE utf8mb4_bin NOT NULL COMMENT '版本名称',
  `start_date` datetime DEFAULT NULL COMMENT '开始日期',
  `release_date` datetime DEFAULT NULL COMMENT '发布日期',
  `status` varchar(20) COLLATE utf8mb4_bin NOT NULL COMMENT '版本状态',
  `description` varchar(200) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '版本描述',
  `tenant_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '租户ID',
  `created_by` bigint(20) NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  `last_modified_by` bigint(20) NOT NULL COMMENT '最后修改人',
  `last_modified_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '最后修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_project_id` (`project_id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_created_by` (`created_by`) USING BTREE,
  KEY `idx_created_date` (`created_date`) USING BTREE,
  FULLTEXT KEY `fx_name_description` (`name`,`description`) /*!50100 WITH PARSER `ngram` */
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='软件版本';

-- ----------------------------
-- Table structure for tag
-- ----------------------------
DROP TABLE IF EXISTS `tag`;
CREATE TABLE `tag` (
  `id` bigint(20) NOT NULL COMMENT 'ID',
  `name` varchar(100) COLLATE utf8mb4_bin NOT NULL COMMENT '标签名称',
  `project_id` bigint(20) NOT NULL COMMENT '项目ID',
  `tenant_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '所属租户ID',
  `created_by` bigint(20) NOT NULL DEFAULT '-1' COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  `last_modified_by` bigint(20) NOT NULL DEFAULT '-1' COMMENT '最后修改人',
  `last_modified_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '最后修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_create_date` (`created_date`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_created_by` (`created_by`) USING BTREE,
  KEY `idx_project_id` (`project_id`),
  FULLTEXT KEY `fx_name` (`name`) /*!50100 WITH PARSER `ngram` */
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='标签';

-- ----------------------------
-- Table structure for tag_target
-- ----------------------------
DROP TABLE IF EXISTS `tag_target`;
CREATE TABLE `tag_target` (
  `id` bigint(20) NOT NULL COMMENT 'ID',
  `tag_id` bigint(20) NOT NULL COMMENT '标签ID',
  `target_type` varchar(40) COLLATE utf8mb4_bin NOT NULL COMMENT '关联资源类型',
  `target_id` bigint(20) NOT NULL COMMENT '关联资源ID',
  `tenant_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '租户ID',
  `created_by` bigint(20) NOT NULL DEFAULT '-1' COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_tag_id` (`tag_id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_target_id` (`target_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='标签对象';

-- ----------------------------
-- Table structure for task
-- ----------------------------
DROP TABLE IF EXISTS `task`;
CREATE TABLE `task` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `name` varchar(400) COLLATE utf8mb4_bin NOT NULL COMMENT '名称',
  `code` varchar(16) COLLATE utf8mb4_bin NOT NULL COMMENT '编号',
  `project_id` bigint(20) NOT NULL COMMENT '项目ID',
  `sprint_id` bigint(20) DEFAULT NULL COMMENT '迭代ID',
  `sprint_auth` int(1) NOT NULL COMMENT '迭代授权控制标志',
  `module_id` bigint(20) DEFAULT '-1' COMMENT '模块ID',
  `software_version` varchar(40) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '软件版本',
  `backlog` int(1) NOT NULL COMMENT '迭代Backlog标志',
  `task_type` varchar(16) COLLATE utf8mb4_bin NOT NULL COMMENT '任务类型：接口测试,场景测试,缺陷,需求,任务',
  `bug_level` varchar(16) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '缺陷等级',
  `target_id` bigint(20) DEFAULT NULL COMMENT '接口或场景ID',
  `target_parent_id` bigint(20) DEFAULT NULL COMMENT '项目或目录ID',
  `test_type` varchar(16) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '测试类型：功能测试|性能测试|稳定性',
  `priority` varchar(16) COLLATE utf8mb4_bin NOT NULL COMMENT '优先级',
  `status` varchar(16) COLLATE utf8mb4_bin NOT NULL COMMENT '任务状态',
  `assignee_id` bigint(20) DEFAULT NULL COMMENT '经办人ID',
  `confirmor_id` bigint(20) DEFAULT NULL COMMENT '确认ID',
  `tester_id` bigint(20) DEFAULT NULL COMMENT '测试人ID',
  `missing_bug` int(1) DEFAULT NULL COMMENT '是否漏测缺陷',
  `unplanned` int(1) NOT NULL DEFAULT '0',
  `script_id` bigint(20) DEFAULT NULL COMMENT '脚本ID',
  `exec_result` varchar(10) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '执行测试结果',
  `exec_failure_message` varchar(200) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '执行失败原因',
  `exec_test_num` int(11) DEFAULT '0' COMMENT '执行次数',
  `exec_test_failure_num` int(11) DEFAULT '0' COMMENT '执行失败次数',
  `exec_id` bigint(20) DEFAULT NULL COMMENT '执行ID',
  `exec_name` varchar(200) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '执行名称',
  `exec_by` bigint(20) DEFAULT NULL COMMENT '执行人ID',
  `exec_date` datetime DEFAULT NULL COMMENT '执行时间',
  `start_date` datetime DEFAULT NULL COMMENT '开始时间',
  `deadline_date` datetime DEFAULT NULL COMMENT '截止时间',
  `overdue` int(1) NOT NULL DEFAULT '0' COMMENT '逾期标志',
  `completed_date` datetime DEFAULT NULL COMMENT '完成时间',
  `canceled_date` datetime DEFAULT NULL COMMENT '取消时间',
  `confirmed_date` datetime DEFAULT NULL COMMENT '确认时间',
  `processed_date` datetime DEFAULT NULL COMMENT '已处理时间',
  `fail_num` int(11) NOT NULL DEFAULT '0' COMMENT '处理失败次数',
  `total_num` int(11) NOT NULL DEFAULT '0' COMMENT '处理总次数',
  `attachments` json DEFAULT NULL COMMENT '附件文件',
  `eval_workload_method` varchar(16) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '预估工作量方式',
  `eval_workload` double(8,2) DEFAULT NULL COMMENT '故事点',
  `actual_workload` double(8,2) DEFAULT '0.00' COMMENT '实际工作量',
  `parent_task_id` bigint(20) DEFAULT '-1' COMMENT '父任务ID',
  `ref_task_ids` json DEFAULT NULL COMMENT '关联任务ID',
  `ref_case_ids` json DEFAULT NULL COMMENT '关联功能用例ID',
  `description` text COLLATE utf8mb4_bin COMMENT '描述',
  `sprint_deleted` int(1) NOT NULL DEFAULT '0' COMMENT '迭代删除标志',
  `tenant_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '租户ID',
  `deleted` int(1) NOT NULL DEFAULT '0' COMMENT '删除标志',
  `deleted_by` bigint(20) DEFAULT NULL COMMENT '删除人',
  `deleted_date` datetime DEFAULT NULL COMMENT '删除时间',
  `created_by` bigint(20) NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  `last_modified_by` bigint(20) DEFAULT NULL COMMENT '最后修改人',
  `last_modified_date` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uidx_code_tenant_id` (`code`,`tenant_id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_test_type` (`test_type`) USING BTREE,
  KEY `idx_exec_no` (`exec_name`) USING BTREE,
  KEY `idx_exec_id` (`exec_id`) USING BTREE,
  KEY `idx_created_date` (`created_date`) USING BTREE,
  KEY `idx_start_date` (`start_date`) USING BTREE,
  KEY `idx_deadline_date` (`deadline_date`) USING BTREE,
  KEY `idx_created_by` (`created_by`) USING BTREE,
  KEY `idx_target_parent_id` (`target_parent_id`) USING BTREE,
  KEY `idx_task_type` (`task_type`) USING BTREE,
  KEY `idx_finish_date` (`completed_date`) USING BTREE,
  KEY `idx_completed_date` (`completed_date`) USING BTREE,
  KEY `idx_exec_date` (`exec_date`) USING BTREE,
  KEY `idx_priority` (`priority`) USING BTREE,
  KEY `idx_exec_result` (`exec_result`) USING BTREE,
  KEY `idx_last_modified_date` (`last_modified_date`),
  KEY `idx_last_modified_by` (`last_modified_by`) USING BTREE,
  KEY `idx_canceled_date` (`canceled_date`) USING BTREE,
  KEY `idx_confirmed_date` (`confirmed_date`) USING BTREE,
  KEY `idx_processed_date` (`processed_date`) USING BTREE,
  KEY `idx_project_id` (`project_id`) USING BTREE,
  KEY `idx_plan_id` (`sprint_id`) USING BTREE,
  KEY `idx_assignee_id` (`assignee_id`),
  KEY `idx_confirmor_id` (`confirmor_id`),
  KEY `idx_module_id` (`module_id`),
  KEY `idx_parent_task_id` (`parent_task_id`) USING BTREE,
  KEY `idx_target_id_test_type` (`target_id`,`test_type`) USING BTREE,
  KEY `idx_bug_level` (`bug_level`),
  KEY `idx_tester_id` (`tester_id`),
  KEY `idx_deleted` (`deleted`) USING BTREE,
  KEY `idx_sprint_deleted` (`sprint_deleted`) USING BTREE,
  KEY `idx_project_auth` (`sprint_auth`) USING BTREE,
  KEY `idx_sprint_auth` (`actual_workload`) USING BTREE,
  KEY `idx_overdue` (`overdue`),
  KEY `idx_backlog` (`backlog`),
  FULLTEXT KEY `fx_name_code` (`name`,`code`) /*!50100 WITH PARSER `ngram` */
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='任务';

-- ----------------------------
-- Table structure for task_assignee
-- ----------------------------
DROP TABLE IF EXISTS `task_assignee`;
CREATE TABLE `task_assignee` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `task_id` bigint(20) NOT NULL COMMENT '任务ID',
  `assignee_id` bigint(20) NOT NULL COMMENT '经办人ID',
  `tenant_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '租户ID',
  `created_by` bigint(20) NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `idx_task_assignee_id` (`task_id`,`assignee_id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_task_id` (`task_id`) USING BTREE,
  KEY `idx_assignee_Id` (`assignee_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='经办人';

-- ----------------------------
-- Table structure for task_confirmor
-- ----------------------------
DROP TABLE IF EXISTS `task_confirmor`;
CREATE TABLE `task_confirmor` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `task_id` bigint(20) NOT NULL COMMENT '任务ID',
  `confirmor_id` bigint(20) NOT NULL COMMENT '确认人ID',
  `tenant_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '租户ID',
  `created_by` bigint(20) NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `idx_task_confirmor_id` (`task_id`,`confirmor_id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_task_id` (`task_id`) USING BTREE,
  KEY `idx_confirmor_id` (`confirmor_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='任务确认人';

-- ----------------------------
-- Table structure for task_favourite
-- ----------------------------
DROP TABLE IF EXISTS `task_favourite`;
CREATE TABLE `task_favourite` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `project_id` bigint(20) NOT NULL COMMENT '项目ID',
  `task_id` bigint(20) NOT NULL COMMENT '任务ID',
  `tenant_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '租户ID',
  `created_by` bigint(20) NOT NULL DEFAULT '-1' COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_created_by` (`created_by`) USING BTREE,
  KEY `idx_task_id` (`task_id`,`created_by`) USING BTREE,
  KEY `idx_scenario_id` (`task_id`) USING BTREE,
  KEY `idx_project_id` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC COMMENT='场景收藏';

-- ----------------------------
-- Table structure for task_follow
-- ----------------------------
DROP TABLE IF EXISTS `task_follow`;
CREATE TABLE `task_follow` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `project_id` bigint(20) NOT NULL COMMENT '项目ID',
  `task_id` bigint(20) NOT NULL COMMENT '任务ID',
  `tenant_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '租户ID',
  `created_by` bigint(20) NOT NULL DEFAULT '-1' COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_created_by` (`created_by`) USING BTREE,
  KEY `idx_task_id` (`task_id`) USING BTREE,
  KEY `uidx_task_created_id` (`task_id`,`created_by`) USING BTREE,
  KEY `idx_project_id` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='场景关注';

-- ----------------------------
-- Table structure for task_func_case
-- ----------------------------
DROP TABLE IF EXISTS `task_func_case`;
CREATE TABLE `task_func_case` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `target_type` varchar(40) COLLATE utf8mb4_bin NOT NULL COMMENT '资源类型',
  `target_id` bigint(20) NOT NULL COMMENT '资源ID',
  `assoc_target_type` varchar(40) COLLATE utf8mb4_bin NOT NULL COMMENT '关联资源类型',
  `assoc_target_id` bigint(20) NOT NULL COMMENT '关联资源ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `udx_target_assoc_id` (`target_id`,`assoc_target_id`) USING BTREE,
  UNIQUE KEY `udx_assoc_target_id` (`assoc_target_id`,`target_id`) USING BTREE,
  KEY `idx_target_type` (`target_type`) USING BTREE,
  KEY `idx_assoc_target_type` (`assoc_target_type`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='任务与功能用例';

-- ----------------------------
-- Table structure for task_meeting
-- ----------------------------
DROP TABLE IF EXISTS `task_meeting`;
CREATE TABLE `task_meeting` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `project_id` bigint(20) NOT NULL COMMENT '任务项目ID',
  `sprint_id` bigint(20) DEFAULT NULL COMMENT '迭代ID',
  `subject` varchar(200) COLLATE utf8mb4_bin NOT NULL COMMENT '主题',
  `type` varchar(40) COLLATE utf8mb4_bin NOT NULL COMMENT '类型',
  `date` varchar(80) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '会议日期',
  `time` varchar(80) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '会议时间',
  `location` varchar(200) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '会议地点',
  `moderator_id` bigint(20) NOT NULL COMMENT '主持人',
  `moderator` json DEFAULT NULL COMMENT '主持人信息',
  `participants` json DEFAULT NULL COMMENT '参会者',
  `content` text COLLATE utf8mb4_bin COMMENT '会议内容',
  `created_by` bigint(20) NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  `last_modified_by` bigint(20) NOT NULL COMMENT '最后修改人',
  `last_modified_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '最后修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_created_by` (`created_by`) USING BTREE,
  KEY `idx_project_id` (`project_id`) USING BTREE,
  KEY `idx_sprint_id` (`sprint_id`) USING BTREE,
  KEY `idx_type` (`type`) USING BTREE,
  KEY `idx_created_date` (`created_date`) USING BTREE,
  KEY `idx_moderator_id` (`moderator_id`) USING BTREE,
  KEY `idx_last_modified_by` (`last_modified_by`) USING BTREE,
  FULLTEXT KEY `fx_subject` (`subject`) /*!50100 WITH PARSER `ngram` */
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='任务会议表';

-- ----------------------------
-- Table structure for task_remark
-- ----------------------------
DROP TABLE IF EXISTS `task_remark`;
CREATE TABLE `task_remark` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `task_id` bigint(20) NOT NULL COMMENT '任务ID',
  `content` text COLLATE utf8mb4_bin NOT NULL COMMENT '备注内容',
  `created_by` bigint(20) NOT NULL COMMENT '备注人ID',
  `created_date` datetime NOT NULL COMMENT '备注时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_task_id` (`task_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='任务备注\n';

-- ----------------------------
-- Table structure for task_sprint
-- ----------------------------
DROP TABLE IF EXISTS `task_sprint`;
CREATE TABLE `task_sprint` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `project_id` bigint(20) NOT NULL COMMENT '任务项目ID',
  `auth` int(1) NOT NULL COMMENT '是否权限控制',
  `name` varchar(200) COLLATE utf8mb4_bin NOT NULL COMMENT '计划名称',
  `status` varchar(20) COLLATE utf8mb4_bin NOT NULL COMMENT '状态',
  `start_date` datetime NOT NULL COMMENT '计划开始时间',
  `deadline_date` datetime NOT NULL COMMENT '计划截止时间',
  `owner_id` bigint(20) NOT NULL COMMENT '负责人',
  `task_prefix` varchar(40) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '任务前缀',
  `eval_workload_method` varchar(16) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '预估工作量方式',
  `attachments` json DEFAULT NULL COMMENT '附件',
  `acceptance_criteria` text COLLATE utf8mb4_bin COMMENT '验收标准',
  `other_information` text COLLATE utf8mb4_bin COMMENT '描述',
  `deleted` int(1) NOT NULL DEFAULT '0' COMMENT '删除状态',
  `deleted_by` bigint(20) DEFAULT NULL COMMENT '删除人',
  `deleted_date` datetime DEFAULT NULL COMMENT '删除时间',
  `tenant_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '租户ID',
  `created_by` bigint(20) NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  `last_modified_by` bigint(20) NOT NULL COMMENT '最后修改人',
  `last_modified_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '最后修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_name` (`name`) USING BTREE,
  KEY `idx_start_date` (`start_date`) USING BTREE,
  KEY `idx_deadline_date` (`deadline_date`) USING BTREE,
  KEY `idx_owner_id` (`owner_id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_created_by` (`created_by`) USING BTREE,
  KEY `idx_project_id` (`project_id`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_created_date` (`created_date`) USING BTREE,
  KEY `idx_last_modified_by` (`last_modified_by`) USING BTREE,
  KEY `idx_deleted` (`deleted`) USING BTREE,
  KEY `idx_auth` (`auth`) USING BTREE,
  FULLTEXT KEY `idx_name_desc` (`name`,`other_information`) /*!50100 WITH PARSER `ngram` */
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='任务迭代表';

-- ----------------------------
-- Table structure for task_sprint_auth
-- ----------------------------
DROP TABLE IF EXISTS `task_sprint_auth`;
CREATE TABLE `task_sprint_auth` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `sprint_id` bigint(20) NOT NULL COMMENT '任务迭代ID',
  `auth_object_type` varchar(20) COLLATE utf8mb4_bin NOT NULL COMMENT '授权对象：用户|部门|组',
  `auth_object_id` bigint(20) NOT NULL COMMENT '授权对象ID',
  `auths` json NOT NULL COMMENT '权限：查看|编辑|删除|发送请求|测试|授权|分享',
  `creator` int(1) NOT NULL COMMENT '创建人授权标识',
  `tenant_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '租户ID',
  `created_by` bigint(20) NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uidx_sprint_id_auth_creator` (`sprint_id`,`auth_object_id`,`auth_object_type`,`creator`) USING BTREE,
  KEY `idx_created_by` (`created_by`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_auth_object_id` (`auth_object_id`) USING BTREE,
  KEY `idx_created_date` (`created_date`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC COMMENT='任务迭代授权表';

-- ----------------------------
-- Table structure for task_trash
-- ----------------------------
DROP TABLE IF EXISTS `task_trash`;
CREATE TABLE `task_trash` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `project_id` bigint(20) NOT NULL COMMENT '项目ID',
  `sprint_id` bigint(20) DEFAULT NULL COMMENT '迭代ID',
  `target_type` varchar(16) COLLATE utf8mb4_bin NOT NULL COMMENT '计划或用例',
  `target_id` bigint(20) NOT NULL COMMENT '项目、计划或用例ID',
  `target_name` varchar(100) COLLATE utf8mb4_bin NOT NULL COMMENT '名称',
  `task_type` varchar(16) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '任务类型：接口测试,场景测试,缺陷,需求,任务',
  `tenant_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '租户ID',
  `created_by` bigint(20) NOT NULL DEFAULT '0' COMMENT '创建人',
  `deleted_by` bigint(20) NOT NULL COMMENT '删除人',
  `deleted_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uidx_target_id_type` (`target_id`,`target_type`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_created_by` (`created_by`) USING BTREE,
  KEY `idx_project_id` (`project_id`),
  KEY `idx_deleted_date` (`deleted_date`),
  KEY `idx_sprint_id` (`sprint_id`) USING BTREE,
  FULLTEXT KEY `fx_target_name` (`target_name`) /*!50100 WITH PARSER `ngram` */
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC COMMENT='任务回收站';

-- @formatter:on
