CREATE TABLE `node_disk_usage`
(
    `id`          bigint(20)   NOT NULL,
    `node_id`     bigint(20)   NOT NULL,
    `timestamp`   bigint(20)   NOT NULL,
    `tenant_id`   bigint(20)   NOT NULL,
    `device_name` varchar(120) NOT NULL,
    `disk`        varchar(100) NOT NULL,
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_timestamp` (`timestamp`) USING BTREE,
    INDEX `idx_node_id` (`node_id`) USING BTREE,
    #INDEX `idx_tenant_id` (`tenant_id`) USING BTREE,
    INDEX `idx_device_name` (`device_name`) USING BTREE
) ENGINE = InnoDB;