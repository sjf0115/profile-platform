-- 创建计算引擎表
CREATE TABLE `profile_meta_engine` (
    `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
    `status` int NOT NULL DEFAULT 1 COMMENT '状态:1-启用,2-停用',
    `engine_id` varchar(40) NOT NULL COMMENT '引擎ID',
    `engine_name` varchar(100) NOT NULL COMMENT '引擎名称',
    `engine_type` VARCHAR(50) NOT NULL COMMENT '引擎类型:clickhouse,doris,spark,flink',
    `engine_desc` varchar(100) NOT NULL COMMENT '引擎描述',
    `is_default` TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认引擎:0-否,1-是',
    `source_type` int NOT NULL DEFAULT 1 COMMENT '创建方式: 1-系统内置,2-自定义',
    `config` text NOT NULL COMMENT '引擎配置',
    `creator` varchar(100) NOT NULL COMMENT '创建者',
    `modifier` varchar(100) NOT NULL COMMENT '修改者',
    `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE(`engine_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='画像-计算分析引擎';

-- 插入默认 ClickHouse 引擎
INSERT INTO `profile_meta_engine` 
(`status`, `engine_id`, `engine_name`, `engine_type`, `engine_desc`, `is_default`, `source_type`, `config`, `creator`, `modifier`)
VALUES 
(1, 'default_clickhouse', 'ClickHouse(默认)', 'clickhouse', '默认ClickHouse计算引擎', 1, 1, 
 '{"host":"localhost","port":8123,"database":"profile","username":"default","password":"","maxConnections":10,"connectionTimeout":30000}', 
 '100000', '100000');
