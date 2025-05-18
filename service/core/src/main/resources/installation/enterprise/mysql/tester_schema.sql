-- @formatter:off
-- ----------------------------
-- Table structure for analysis
-- ----------------------------
DROP TABLE IF EXISTS `analysis`;
CREATE TABLE `analysis` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `project_id` bigint(20) NOT NULL COMMENT '项目ID',
  `resource` varchar(20) COLLATE utf8mb4_bin NOT NULL COMMENT '分析资源',
  `template` varchar(40) COLLATE utf8mb4_bin NOT NULL COMMENT '分析模版',
  `name` varchar(100) COLLATE utf8mb4_bin NOT NULL COMMENT '名称',
  `description` varchar(200) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '描述',
  `object` varchar(20) COLLATE utf8mb4_bin NOT NULL COMMENT '分析对象',
  `plan_id` bigint(20) DEFAULT NULL COMMENT '迭代或计划ID',
  `org_type` varchar(20) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '分析组织类型',
  `org_id` bigint(20) DEFAULT NULL COMMENT '分析组织ID',
  `contains_user_analysis` tinyint(1) DEFAULT '1' COMMENT '是否包含用户分析',
  `contains_data_detail` tinyint(1) DEFAULT '1' COMMENT '是否包含数据明细',
  `time_range` varchar(20) COLLATE utf8mb4_bin NOT NULL COMMENT '时间范围',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `datasource` varchar(20) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '分析数据源',
  `filter_criteria` varchar(400) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '筛选条件',
  `tenant_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '租户ID',
  `created_by` bigint(20) NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  `last_modified_by` bigint(20) NOT NULL COMMENT '最后修改人',
  `last_modified_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_project_id` (`project_id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_created_by` (`created_by`) USING BTREE,
  KEY `idx_created_date` (`created_date`) USING BTREE,
  KEY `idx_last_modified_by` (`last_modified_by`) USING BTREE,
  FULLTEXT KEY `udx_name_description` (`name`,`description`) /*!50100 WITH PARSER `ngram` */
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='分析';

-- ----------------------------
-- Table structure for analysis_snapshot
-- ----------------------------
DROP TABLE IF EXISTS `analysis_snapshot`;
CREATE TABLE `analysis_snapshot` (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `analysis_id` bigint(20) NOT NULL COMMENT '分析ID',
  `data` longtext COLLATE utf8mb4_bin COMMENT '数据',
  `created_by` bigint(20) NOT NULL COMMENT '创建人',
  `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
  `last_modified_by` bigint(20) NOT NULL COMMENT '最后修改人',
  `last_modified_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `udix_analysis_id` (`analysis_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

-- ----------------------------
-- Table structure for report
-- ----------------------------
DROP TABLE IF EXISTS `report`;
CREATE TABLE `report` (
    `id` bigint(20) NOT NULL COMMENT '主键ID',
    `project_id` bigint(20) NOT NULL COMMENT '项目ID',
    `auth` int(1) NOT NULL COMMENT '是否权限控制',
    `name` varchar(100) COLLATE utf8mb4_bin NOT NULL COMMENT '名称',
    `version` varchar(20) COLLATE utf8mb4_bin NOT NULL COMMENT '版本',
    `description` varchar(200) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '描述',
    `category` varchar(40) COLLATE utf8mb4_bin NOT NULL COMMENT '分类',
    `template` varchar(40) COLLATE utf8mb4_bin NOT NULL COMMENT '模版',
    `status` varchar(40) COLLATE utf8mb4_bin NOT NULL COMMENT '状态',
    `failure_message` varchar(200) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '生成失败原因',
    `created_at` varchar(20) COLLATE utf8mb4_bin NOT NULL COMMENT '报告生成时间类型',
    `next_generation_date` datetime NOT NULL COMMENT '下次生成时间',
    `target_type` varchar(40) COLLATE utf8mb4_bin NOT NULL COMMENT '报告主资源类型',
    `target_id` bigint(20) NOT NULL COMMENT '报告主资源ID',
    `target_name` varchar(400) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '报告主资源类型',
    `share_token` varchar(40) COLLATE utf8mb4_bin NOT NULL COMMENT '分享授权令牌',
    `create_time_setting` json NOT NULL COMMENT '创建时间配置',
    `basic_info_setting` json NOT NULL COMMENT '基本信息配置',
    `content_setting` json NOT NULL COMMENT '内容配置',
    `tenant_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '租户ID',
    `created_by` bigint(20) NOT NULL COMMENT '创建人',
    `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
    `last_modified_by` bigint(20) NOT NULL COMMENT '最后修改人',
    `last_modified_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '最后修改时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_project_id` (`project_id`) USING BTREE,
    KEY `idx_category` (`category`) USING BTREE,
    KEY `idx_template` (`template`) USING BTREE,
    KEY `idx_status` (`status`) USING BTREE,
    KEY `idx_created_at` (`created_at`) USING BTREE,
    KEY `idx_next_generation_date` (`next_generation_date`) USING BTREE,
    KEY `idx_target_id` (`target_id`) USING BTREE,
    KEY `idx_target_type` (`target_type`) USING BTREE,
    KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
    KEY `idx_created_by` (`created_by`) USING BTREE,
    KEY `idx_created_date` (`created_date`) USING BTREE,
    KEY `idx_last_modified_by` (`last_modified_by`) USING BTREE,
    KEY `idx_last_modified_date` (`last_modified_date`) USING BTREE,
    KEY `idx_name` (`name`) USING BTREE,
    KEY `idx_auth` (`auth`) USING BTREE,
    FULLTEXT KEY `fx_name_description` (`name`,`description`) /*!50100 WITH PARSER `ngram` */
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='报告';

-- ----------------------------
-- Table structure for report_auth
-- ----------------------------
DROP TABLE IF EXISTS `report_auth`;
CREATE TABLE `report_auth` (
 `id` bigint(20) NOT NULL COMMENT '主键ID',
 `report_id` bigint(20) NOT NULL COMMENT '报告ID',
 `auth_object_type` varchar(20) COLLATE utf8mb4_bin NOT NULL COMMENT '授权对象：用户|部门|组',
 `auth_object_id` bigint(20) NOT NULL COMMENT '授权对象ID',
 `auths` json NOT NULL COMMENT '权限：查看|编辑|删除|发送请求|测试|授权|分享',
 `creator` int(1) NOT NULL COMMENT '创建人授权标识',
 `tenant_id` bigint(20) NOT NULL DEFAULT '-1' COMMENT '租户ID',
 `created_by` bigint(20) NOT NULL COMMENT '创建人',
 `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
 PRIMARY KEY (`id`) USING BTREE,
 UNIQUE KEY `uidx_report_id_auth_creator` (`report_id`,`auth_object_id`,`auth_object_type`,`creator`) USING BTREE,
 KEY `idx_created_by` (`created_by`) USING BTREE,
 KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
 KEY `idx_auth_object_id` (`auth_object_id`) USING BTREE,
 KEY `idx_created_date` (`created_date`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC COMMENT='报告授权表';

-- ----------------------------
-- Table structure for report_record
-- ----------------------------
DROP TABLE IF EXISTS `report_record`;
CREATE TABLE `report_record` (
   `id` bigint(20) NOT NULL COMMENT '主键ID',
   `project_id` bigint(20) NOT NULL COMMENT '项目ID',
   `report_id` bigint(20) NOT NULL COMMENT '报告ID',
   `category` varchar(40) COLLATE utf8mb4_bin NOT NULL COMMENT '分类',
   `template` varchar(40) COLLATE utf8mb4_bin NOT NULL COMMENT '模版',
   `content` json NOT NULL COMMENT '报告数据ID',
   `created_by` bigint(20) NOT NULL COMMENT '创建人',
   `created_date` datetime NOT NULL DEFAULT '2001-01-01 00:00:00' COMMENT '创建时间',
   PRIMARY KEY (`id`) USING BTREE,
   KEY `idx_report_id` (`report_id`) USING BTREE,
   KEY `idx_created_by` (`created_by`) USING BTREE,
   KEY `idx_created_date` (`created_date`) USING BTREE,
   KEY `idx_category` (`category`) USING BTREE,
   KEY `idx_template` (`template`) USING BTREE,
   KEY `idx_project_id` (`project_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='报告记录';

-- @formatter:on
