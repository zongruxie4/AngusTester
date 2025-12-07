CREATE TABLE `node_usage`
(
    `id`         bigint(20) NOT NULL,
    `timestamp`  bigint(20) NOT NULL,
    `node_id`    bigint(20) NOT NULL,
    `tenant_id`  bigint(20) NOT NULL,
    `cpu`        varchar(60) NULL DEFAULT NULL,
    `memory`     varchar(120) NULL DEFAULT NULL,
    `filesystem` varchar(80) NULL DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE,
    INDEX        `idx_timestamp` (`timestamp`) USING BTREE,
    #INDEX       `idx_tenant_id` (`tenant_id`) USING BTREE,
    INDEX        `idx_node_id` (`node_id`) USING BTREE
) ENGINE = InnoDB;
