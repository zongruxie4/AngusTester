CREATE TABLE `exec_sample_error_cause`
(
    `id`         bigint(20) NOT NULL,
    `exec_id`    bigint(20) NOT NULL,
    `node_id`    bigint(20) NOT NULL,
    `tenant_id`  bigint(20) NOT NULL,
    `finish`     bit(1)        NOT NULL,
    `timestamp`  bigint(20) NOT NULL,
    `timestamp0` bigint(20) NOT NULL,
    `name`       varchar(400)  NOT NULL,
    `key`        varchar(200)  NOT NULL,
    `content`    varchar(4096) NOT NULL,
    PRIMARY KEY (`id`) USING BTREE,
    INDEX        `idx_exec_id` (`exec_id`) USING BTREE,
    INDEX        `idx_node_id` (`node_id`) USING BTREE,
    #INDEX       `idx_tenant_id` (`tenant_id`) USING BTREE,
    INDEX        `idx_timestamp` (`timestamp`) USING BTREE,
    INDEX        `idx_name` (`name`) USING BTREE,
    INDEX        `idx_finish` (`finish`) USING BTREE
) ENGINE = InnoDB;
