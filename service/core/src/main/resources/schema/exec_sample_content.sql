CREATE TABLE `exec_sample_content`
(
    `id`         bigint(20)   NOT NULL,
    `exec_id`    bigint(20)   NOT NULL,
    `node_id`    bigint(20)   NOT NULL,
    `tenant_id`  bigint(20)   NOT NULL,
    `finish`     bit(1)       NOT NULL,
    `timestamp`  bigint(20)   NOT NULL,
    `timestamp0` bigint(20)   NOT NULL,
    `name`       varchar(400) NOT NULL,
    `ext_field`  varchar(20)  NOT NULL,
    `iteration`  bigint(20)   NOT NULL,
    `key`        varchar(200) NOT NULL,
    `content`    text         NOT NULL,
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_exec_id` (`exec_id`) USING BTREE,
    INDEX `idx_node_id` (`node_id`) USING BTREE,
    #INDEX `idx_tenant_id` (`tenant_id`) USING BTREE,
    INDEX `idx_timestamp` (`timestamp`) USING BTREE,
    INDEX `idx_name` (`name`) USING BTREE,
    INDEX `idx_ext_field` (`ext_field`) USING BTREE,
    INDEX `idx_finish` (`finish`) USING BTREE
) ENGINE = InnoDB;
