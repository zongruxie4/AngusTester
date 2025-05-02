CREATE TABLE `jvm_service_usage`
(
    `id`         bigint(20)   NOT NULL,
    `timestamp`  bigint(20)   NOT NULL,
    `node_id`    bigint(20)   NOT NULL,
    `tenant_id`  bigint(20)   NOT NULL,
    `service_id` bigint(20)   NOT NULL,
    `source`     varchar(40)  NOT NULL,
    `jvm`        varchar(120) NULL DEFAULT NULL,
    `processor`  varchar(80)  NULL DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_timestamp` (`timestamp`) USING BTREE,
    INDEX `idx_node_id` (`node_id`) USING BTREE,
    #INDEX `idx_tenant_id` (`tenant_id`) USING BTREE,
    INDEX `idx_service_id` (`service_id`) USING BTREE
) ENGINE = InnoDB;