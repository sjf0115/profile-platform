
DROP DATABASE profile;
CREATE DATABASE profile;

-- 1. 用户
DROP Table `profile_meta_user`;
CREATE TABLE IF NOT EXISTS `profile_meta_user`(
    `id` BIGINT UNSIGNED AUTO_INCREMENT COMMENT '自增ID',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态:0-停用, 1-未激活,2-激活',
    `user_id` VARCHAR(40) NOT NULL COMMENT '用户ID',
    `user_name` VARCHAR(100) NOT NULL COMMENT '用户名称',
    `email` VARCHAR(100) COMMENT '邮箱',
    `password` VARCHAR(100) NOT NULL COMMENT '密码',
    `source_type` INT NOT NULL DEFAULT 1 COMMENT '创建方式: 1-系统内置,2-自定义',
    `creator` VARCHAR(100) NOT NULL COMMENT '创建者',
    `modifier` VARCHAR(100) NOT NULL COMMENT '修改者',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE(`user_id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '画像-用户';

INSERT INTO `profile_meta_user` (`status`, `user_id`, `user_name`, `password`, `source_type`, `creator`, `modifier`)
VALUES (1, '100000', 'admin', 'admin', 1, '100000', '100000');

-- 2. 实体
-- 实体 uid ，实体类型为 用户
DROP Table `profile_meta_entity`;
CREATE TABLE IF NOT EXISTS `profile_meta_entity`(
    `id` BIGINT UNSIGNED AUTO_INCREMENT COMMENT '自增ID',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态:1-启用,2-停用',
    `entity_id` VARCHAR(40) NOT NULL COMMENT '实体ID',
    `entity_name` VARCHAR(100) NOT NULL COMMENT '实体名称',
    `entity_type_id` VARCHAR(100) NOT NULL COMMENT '实体类型ID',
    `source_type` INT NOT NULL DEFAULT 1 COMMENT '创建方式: 1-系统内置,2-自定义',
    `creator` VARCHAR(100) NOT NULL COMMENT '创建者',
    `modifier` VARCHAR(100) NOT NULL COMMENT '修改者',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE(`entity_id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '画像-实体';

INSERT INTO `profile_meta_entity` (`status`, `entity_id`, `entity_name`, `entity_type_id`, `source_type`, `creator`, `modifier`)
VALUES (1, '0219740078368128', 'uid', '0319740109099392', 1, '100000', '100000')
;

-- 3. 实体类型
DROP Table `profile_meta_entity_type`;
CREATE TABLE IF NOT EXISTS `profile_meta_entity_type`(
    `id` BIGINT UNSIGNED AUTO_INCREMENT COMMENT '自增ID',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态:1-启用,2-停用',
    `entity_type_id` VARCHAR(40) NOT NULL COMMENT '实体类型ID',
    `entity_type_name` VARCHAR(100) NOT NULL COMMENT '实体类型名称',
    `source_type` INT NOT NULL DEFAULT 1 COMMENT '创建方式: 1-系统内置,2-自定义',
    `creator` VARCHAR(100) NOT NULL COMMENT '创建者',
    `modifier` VARCHAR(100) NOT NULL COMMENT '修改者',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE(`entity_type_id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '画像-实体类型';

INSERT INTO `profile_meta_entity_type` (`status`, `entity_type_id`, `entity_type_name`, `source_type`, `creator`, `modifier`)
VALUES (1, '0319740109099392', '用户', 1, '100000', '100000')
;

-- 4. 数据源Schema 废弃
--CREATE TABLE `profile_meta_datasource_schema` (
--    `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
--    `status` INT NOT NULL DEFAULT 1 COMMENT '状态:1-启用,2-停用',
--    `schema_id` varchar(40) NOT NULL COMMENT '数据源 Schema ID',
--    `schema_name` varchar(100) NOT NULL COMMENT '数据源 Schema 名称',
--    `schema_type` varchar(100) NOT NULL COMMENT '数据源 Schema 类型:1-source,2-sink,3-source/sink',
--    `jdbc_protocol` varchar(50) NOT NULL COMMENT '数据源 Schema JDBC 协议 例如 jdbc://mysql',
--    `source_type` INT NOT NULL DEFAULT 1 COMMENT '创建方式: 1-系统内置,2-自定义',
--    `config_template` text NOT NULL COMMENT '配置模板',
--    `creator` varchar(100) NOT NULL COMMENT '创建者',
--    `modifier` varchar(100) NOT NULL COMMENT '修改者',
--    `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
--    `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
--    PRIMARY KEY (`id`),
--    UNIQUE(`schema_id`)
--) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='画像-数据源Schema';

-- 5. 数据源
DROP Table `profile_meta_datasource`;
CREATE TABLE `profile_meta_datasource` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `status` int NOT NULL DEFAULT '1' COMMENT '状态:1-启用,2-停用',
  `datasource_id` varchar(40) NOT NULL COMMENT '数据源ID',
  `datasource_name` varchar(100) NOT NULL COMMENT '数据源名称',
  `datasource_desc` varchar(100) NOT NULL COMMENT '数据源描述',
  `datasource_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '数据源类型',
  `source_type` int NOT NULL DEFAULT '1' COMMENT '创建方式: 1-系统内置,2-自定义',
  `config` text NOT NULL COMMENT '数据源配置',
  `owner` varchar(100) NOT NULL COMMENT '负责人',
  `creator` varchar(100) NOT NULL COMMENT '创建者',
  `modifier` varchar(100) NOT NULL COMMENT '修改者',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `datasource_id` (`datasource_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='画像-数据源'

INSERT INTO `profile_meta_datasource` (`status`, `datasource_id`, `datasource_name`, `datasource_desc`, `schema_id`, `source_type`, `config`, `owner`, `creator`, `modifier`)
VALUES (1, '0500000000000001', 'bi-reports', 'MySQL数据平台报表数据库', '0400000000000001', 2, '{"host":"localhost","port":"3306","database":"test","user_name":"root","password":"root", "driver": "com.mysql.cj.jdbc.Driver"}', '100000', '100000', '100000');


-- 6. 数据集
DROP Table `profile_meta_dataset`;
CREATE TABLE `profile_meta_dataset` (
    `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
    `status` int NOT NULL DEFAULT '1' COMMENT '状态:1-启用,2-停用',
    `dataset_id` varchar(40) NOT NULL COMMENT '数据集ID',
    `dataset_name` varchar(100) NOT NULL COMMENT '数据集名称',
    `dataset_type` int NOT NULL DEFAULT '1' COMMENT '数据集类型: 1-标签数据集,2-行为数据集,3-统计数据集',
    `dataset_desc` varchar(200) DEFAULT NULL COMMENT '数据集描述',
    `source_type` int NOT NULL DEFAULT '1' COMMENT '创建方式: 1-系统内置,2-自定义',
    `datasource_id` varchar(50) NOT NULL COMMENT '同步的数据源ID',
    `table_name` varchar(50) NOT NULL COMMENT '原始数据表名',
    `partition_field` varchar(50) DEFAULT NULL COMMENT '同步的数据表的时间分区字段',
    `partition_format` varchar(50) DEFAULT NULL COMMENT '同步的数据表分区值格式',
    `entity_id` varchar(50) NOT NULL COMMENT '主体(实体)ID',
    `entity_field` varchar(100) NOT NULL COMMENT '主体(实体)标识字段',
    `owner` varchar(100) NOT NULL COMMENT '负责人',
    `creator` varchar(100) NOT NULL COMMENT '创建者',
    `modifier` varchar(100) NOT NULL COMMENT '修改者',
    `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `dataset_id` (`dataset_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='画像-数据集';

-- 7. 标签类目
DROP TABLE IF EXISTS `profile_meta_label_category`;
CREATE TABLE IF NOT EXISTS `profile_meta_label_category`(
    `id` BIGINT UNSIGNED AUTO_INCREMENT COMMENT '自增ID',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态:启用-1,删除-2',
    `is_default` INT NOT NULL DEFAULT 2 COMMENT '是否是默认兜底类目:是-1,否-2',
    `category_id` VARCHAR(40) NOT NULL COMMENT '标签类目ID',
    `category_name` VARCHAR(100) NOT NULL COMMENT '标签类目名称',
    `category_level` INT NOT NULL COMMENT '标签类目层级',
    `parent_category_id` VARCHAR(40) NOT NULL COMMENT '父标签类目ID',
    `category_seq` INT NOT NULL COMMENT '标签类目同级展示序列, 从1开始',
    `source_type` INT NOT NULL DEFAULT 1 COMMENT '创建方式: 1-系统内置,2-自定义',
    `creator` VARCHAR(100) NOT NULL COMMENT '创建者',
    `modifier` VARCHAR(100) NOT NULL COMMENT '修改者',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE(`category_id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '画像-标签类目';


INSERT INTO `profile_meta_label_category` (`status`, `is_default`, `category_id`, `category_name`, `category_level`, `parent_category_id`, `category_seq`, `source_type`, `creator`, `modifier`)
VALUES (1, 1, '0700000000000001', '未分类', 1, '0', 1, 1, '100000', '100000');

-- 8. 标签
DROP TABLE IF EXISTS `profile_meta_label`;
CREATE TABLE IF NOT EXISTS `profile_meta_label`(
    `id` BIGINT UNSIGNED AUTO_INCREMENT COMMENT '自增ID',
    `is_valid` INT NOT NULL DEFAULT 1 COMMENT '是否有效: 1-有效,0-无效',
    `label_id` VARCHAR(40) NOT NULL COMMENT '标签ID',
    `label_name` VARCHAR(150) NOT NULL COMMENT '标签名称',
    `label_status` INT NOT NULL DEFAULT 1 COMMENT '状态:0-未绑定,1-启用,2-禁用',
    `label_type` VARCHAR(100) NOT NULL COMMENT '标签类型: 1-属性标签,2-行为标签',
    `label_desc` VARCHAR(500) COMMENT '标签描述',
    `label_category_id` VARCHAR(100) NOT NULL COMMENT '标签类目ID',
    `label_data_type` INT NOT NULL DEFAULT 1 COMMENT '标签数据类型: 1-文本型,2-数值型,3-时间型',
    `label_dist_type` INT NOT NULL DEFAULT 1 COMMENT '标签数据分布类型: 1-枚举,2-非枚举',
    `label_organize_type` INT NOT NULL DEFAULT 1 COMMENT '标签组织类型: 1-单值,2-多值,3-KV,4-KKV',
    `label_produce_type` INT DEFAULT 0 COMMENT '标签加工类型: 0-未知,1-事实标签,2-统计标签,3-预测标签',
    `label_time_type` INT DEFAULT 0 COMMENT '标签时效性类型: 0-未知,1-离线标签,2-实时标签',
    `entity_identifier_id` VARCHAR(500) COMMENT '标签实体标识ID',
    `source_type` INT NOT NULL DEFAULT 1 COMMENT '创建方式: 1-系统内置,2-数据集导入,3-文件上传,4-四则运算,5-SQL计算,6-自定义规则,7-API导入,8-数据表导入',
    `config` VARCHAR(500) NOT NULL COMMENT '标签计算规则,不同创建方式不同规则',
    `is_office` INT NOT NULL DEFAULT 0 COMMENT '是否官方认证:0-否,1-是',
    `owner` VARCHAR(100) NOT NULL COMMENT '标签负责人',
    `creator` VARCHAR(100) NOT NULL COMMENT '标签创建者',
    `modifier` VARCHAR(100) NOT NULL COMMENT '标签最后修改者',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '标签创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '标签最后修改时间',
    PRIMARY KEY (`id`),
    UNIQUE(`label_id`), UNIQUE(`label_name`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '画像-标签';

INSERT INTO `profile_meta_label` (
    `is_valid`, `label_id`, `label_name`, `label_status`, `label_type`, `label_desc`, `label_category_id`, `label_data_type`, `label_dist_type`,
    `label_organize_type`,	`label_produce_type`, `label_time_type`, `source_type`, `config`, `is_office`, `owner`, `creator`, `modifier`)
VALUES (1, '07H137JO1D', '下单次数', 4, 2, '下单次数', '082ENU08E8', 2, 1, 1, 2, 1, 2, '', 1, '0100000', '0100000', '0100000');

-- 数据集字段
DROP TABLE IF EXISTS `profile_meta_dataset_field`;
CREATE TABLE IF NOT EXISTS `profile_meta_dataset_field`(
    `id` BIGINT UNSIGNED AUTO_INCREMENT COMMENT '自增ID',
    `dataset_id` VARCHAR(40) NOT NULL COMMENT '数据集ID',
    `field_status` INT NOT NULL DEFAULT 1 COMMENT '状态:1-新增字段:数据集字段没有但原始表列有(标记新增的标识)、2-修改字段:数据集字段和原始表列均有、3-删除字段:数据集字段有但原始表列已经删除(标记删除标识)',
    `field_name` VARCHAR(40) NOT NULL COMMENT '字段名称',
    `field_desc` VARCHAR(200) COMMENT '字段描述',
    `field_type` VARCHAR(200) NOT NULL COMMENT '字段类型',
    `import_status` INT NOT NULL DEFAULT 1 COMMENT '导入状态: 1-导入,2-不导入',
    `related_id` VARCHAR(40) NULL COMMENT '数据集关联对象ID',
    `creator` VARCHAR(100) NOT NULL COMMENT '创建者',
    `modifier` VARCHAR(100) NOT NULL COMMENT '最后修改者',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
    PRIMARY KEY (`id`),
    UNIQUE(`dataset_id`, `field_name`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '画像-数据集字段';

-- 9. 群组
DROP Table `profile_meta_group`;
CREATE TABLE IF NOT EXISTS `profile_meta_group`(
    `id` BIGINT UNSIGNED AUTO_INCREMENT COMMENT '自增ID',
    `group_id` VARCHAR(40) NOT NULL COMMENT '群组ID',
    `group_status` INT NOT NULL COMMENT '群组状态: 1-启用,2-停用',
    `group_name` VARCHAR(100) NOT NULL COMMENT '群组名称',
    `group_type` INT NOT NULL COMMENT '群组类型: 1-标签筛选,2-群组交并,3-行为圈选,4-行为序列圈选,5-组合人群,6-文件上传',
    `group_desc` VARCHAR(200) COMMENT '群组描述',
    `group_rule` VARCHAR(500) NOT NULL COMMENT '群组规则',
    `group_count` INT NOT NULL COMMENT '群组覆盖规模',
    `entity_identifier_id` VARCHAR(50) NOT NULL COMMENT '群组主体标识ID',
    `source_type` INT NOT NULL DEFAULT 1 COMMENT '创建方式: 1-系统内置,2-自定义',
    `owner` VARCHAR(100) NOT NULL COMMENT '群组负责人',
    `creator` VARCHAR(100) NOT NULL COMMENT '创建者',
    `modifier` VARCHAR(100) NOT NULL COMMENT '修改者',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE (`group_id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '画像-群组';

-- 10. 投递
DROP Table `profile_meta_export`;
CREATE TABLE `profile_meta_export` (
    `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
    `status` int NOT NULL DEFAULT 1 COMMENT '状态:1-启用,2-停用',
    `export_id` varchar(40) NOT NULL COMMENT '投递ID',
    `export_type` int NOT NULL COMMENT '投递类型：1-群组,2-标签',
    `export_name` varchar(100) NOT NULL COMMENT '投递名称',
    `export_desc` varchar(100) COMMENT '投递描述',
    `export_config` text NOT NULL COMMENT '投递配置',
    `export_mode` int NOT NULL COMMENT '投递方式:1-数据源,2-应用',
    `scheduler_type` int NOT NULL COMMENT '调度类型:1-手动触发调度,2-API触发调度,3-日周期调度,4-小时周期调度',
    `scheduler_cron` varchar(20) COMMENT '调度 cron 表达式:只有周期自动触发更新才有',
    `scheduler_url` VARCHAR(100) COMMENT '调度触发URL:只有API触发调度才有',
    `scheduler_start_time` bigint COMMENT '触发调度有效开始时间:只有周期自动触发更新才有',
    `scheduler_end_time` bigint COMMENT '触发调度有效结束时间:只有周期自动触发更新才有',
    `source_type` int NOT NULL DEFAULT 1 COMMENT '创建方式: 1-系统内置,2-自定义',
    `owner` varchar(100) NOT NULL COMMENT '负责人',
    `creator` varchar(100) NOT NULL COMMENT '创建者',
    `modifier` varchar(100) NOT NULL COMMENT '修改者',
    `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE(`export_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='画像-投递';

-- 13. 事件
DROP Table `profile_meta_event`;
CREATE TABLE IF NOT EXISTS `profile_meta_event`(
    `id` BIGINT UNSIGNED AUTO_INCREMENT COMMENT '自增ID',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态:1-启用,2-停用',
    `event_id` VARCHAR(40) NOT NULL COMMENT '事件ID',
    `event_name` VARCHAR(100) NOT NULL COMMENT '事件名称',
    `event_desc` VARCHAR(200) DEFAULT NULL COMMENT '事件描述',
    `event_rules` VARCHAR(800) NOT NULL COMMENT '事件规则',
    `dataset_id` VARCHAR(200) NOT NULL COMMENT '数据集ID: 配置事件必须配置一个对应的行为数据集',
    `source_type` INT NOT NULL DEFAULT 1 COMMENT '创建方式: 1-系统内置,2-自定义',
    `owner` VARCHAR(100) NOT NULL COMMENT '负责人',
    `creator` VARCHAR(100) NOT NULL COMMENT '创建者',
    `modifier` VARCHAR(100) NOT NULL COMMENT '修改者',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE (`event_id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '画像-事件';

-- 14. 事件属性
DROP Table `profile_meta_event_attr`;
CREATE TABLE IF NOT EXISTS `profile_meta_event_attr`(
    `id` BIGINT UNSIGNED AUTO_INCREMENT COMMENT '自增ID',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态:1-启用,2-停用',
    `attr_id` VARCHAR(40) NOT NULL COMMENT '事件属性ID',
    `attr_name` VARCHAR(100) NOT NULL COMMENT '事件属性名称',
    `attr_desc` VARCHAR(200) COMMENT '事件属性描述',
    `attr_type` INT NOT NULL DEFAULT 2 COMMENT '事件属性类型: 1-内置属性,2-公共属性(数据集),3-事件属性',
    `dataset_id` VARCHAR(40) DEFAULT NULL COMMENT '数据集ID: 内置属性不需要',
    `event_id` VARCHAR(40) DEFAULT NULL COMMENT '事件ID: 只有事件属性需要',
    `entity_id` VARCHAR(40) DEFAULT NULL COMMENT '实体ID: 该属性对应实体时配置',
    `attr_field` VARCHAR(100) NOT NULL COMMENT '属性提取字段',
    `attr_path` VARCHAR(100)  NOT NULL COMMENT '属性提取的字段路径',
    `attr_data_type` INT NOT NULL DEFAULT 1 COMMENT '属性数据类型: 1-文本型,2-数值型,3-时间型',
    `attr_dist_type` INT NOT NULL DEFAULT 1 COMMENT '属性分布类型: 1-枚举,2-非枚举',
    `attr_organize_type` INT NOT NULL DEFAULT 1 COMMENT '属性组织类型: 1-单值,2-多值',
    `creator` VARCHAR(100) NOT NULL COMMENT '创建者',
    `modifier` VARCHAR(100) NOT NULL COMMENT '修改者',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE (`attr_id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '画像-事件属性';


--INSERT INTO `profile_meta_event_attr` (`status`, `attr_id`, `attr_name`, `attr_desc`, `attr_type`, `dataset_id`, `entity_id`, `attr_field`, `attr_path`, `attr_data_type`, `attr_dist_type`, `attr_organize_type`)
--VALUES
--    (1, '', '主体对象ID', '一般指用户ID', 1, null, '03W199ZY5Z', 'subject_id', null, 1, 1, 1)
--    ,(1, '', '主体对象类型', '一般指用户', 1, null, '03W199ZY5Z', 'subject_type', null, 1, 1, 1)
--    ,(1, '', '主体对象属性', '一般指用户属性', 1, null, '03W199ZY5Z', 'subject_attr', null, 1, 1, 1)
--    ,(1, '', '客体对象ID', '被操作对象的ID', 1, null, '03W199ZY5Z', 'object_id', null, 1, 1, 1)
--    ,(1, '', '客体对象类型', '一般指内容或者媒体号', 1, null, '03W199ZY5Z', 'object_type', null, 1, 1, 1)
--    ,(1, '', '客体对象属性', '被操作对象属性', 1, null, '03W199ZY5Z', 'object_attr', null, 1, 1, 1)
--    ,(1, '', '行为时间', '行为发生时间', 1, null, '03W199ZY5Z', 'behavior_time', null, 1, 1, 1)
--    ,(1, '', '行为类型', '行为类型', 1, null, '03W199ZY5Z', 'behavior_type', null, 1, 1, 1)
--    ,(1, '', '行为属性', '行为属性', 1, null, '03W199ZY5Z', 'behavior_args', null, 1, 1, 1)
--;


-- 9. 任务 调度任务配置
DROP Table `profile_meta_task`;
CREATE TABLE IF NOT EXISTS `profile_meta_task`(
    `id` BIGINT UNSIGNED AUTO_INCREMENT COMMENT '自增ID',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1-启用,2-停用',
    `task_id` VARCHAR(40) NOT NULL COMMENT '调度任务ID',
    `task_name` VARCHAR(100) NOT NULL COMMENT '调度任务名称',
    `task_desc` VARCHAR(100) COMMENT '调度任务描述',
    `task_type` INT NOT NULL COMMENT '调度任务类型:1-群组圈选,2-群组投递,3-数据集同步',
    `task_related_id` VARCHAR(100) COMMENT '调度任务关联ID',
    `trigger_target_id` VARCHAR(100) COMMENT '调度对象ID',
    `trigger_type` INT DEFAULT 1 COMMENT '调度类型:1-无调度(手动调度),2-日周期调度,3-小时周期调度',
    `trigger_cron` VARCHAR(20) COMMENT '调度 cron 表达式:只有周期自动触发更新才有',
    `trigger_url` VARCHAR(20) COMMENT '调度触发URL:只有API触发调度才有',
    `trigger_start_time` VARCHAR(20) COMMENT '触发调度有效开始时间:只有周期自动触发更新才有',
    `trigger_end_time` VARCHAR(20) COMMENT '触发调度有效结束时间:只有周期自动触发更新才有',
    `schedule_id` VARCHAR(100) COMMENT '调度引擎侧的调度标识',
    `upstream_task_ids` VARCHAR(500) COMMENT '上游任务ID列表(逗号分隔)',
    `alert_condition` VARCHAR(20) COMMENT '告警触发条件:failure-执行失败,success-执行成功,finished-执行完成(空=未配置)',
    `alert_channels` VARCHAR(100) COMMENT '报警方式(逗号分隔):sms,email,phone,dingtalk,webhook。本期仅email生效',
    `alert_receivers` VARCHAR(1000) COMMENT '接收人JSON条目:[{"type":"owner"},{"type":"user","value":"userId"}]',
    `source_type` INT NOT NULL DEFAULT 1 COMMENT '创建方式: 1-系统内置,2-自定义',
    `owner` VARCHAR(100) NOT NULL COMMENT '任务负责人',
    `creator` VARCHAR(100) NOT NULL COMMENT '创建者',
    `modifier` VARCHAR(100) NOT NULL COMMENT '修改者',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE (`task_id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '画像-调度任务';

-- 任务实例 调度任务运行实例
DROP Table `profile_meta_task_instance`;
CREATE TABLE IF NOT EXISTS `profile_meta_task_instance`(
    `id` BIGINT UNSIGNED AUTO_INCREMENT COMMENT '自增ID',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态:1-未运行,2-运行中,3-运行失败,4-运行成功',
    `instance_id` VARCHAR(40) NOT NULL COMMENT '实例ID',
    `instance_name` VARCHAR(100) NOT NULL COMMENT '实例名称',
    `task_id` VARCHAR(100) NOT NULL COMMENT '任务ID',
    `instance_related_id` VARCHAR(100) COMMENT '实例关联ID',
    `trigger_mode` VARCHAR(100) COMMENT '触发模式：1-手动触发,2-定时调度,3-API触发',
    `start_time` BIGINT NOT NULL COMMENT '实例运行的开始时间:毫秒时间戳',
    `end_time` BIGINT NOT NULL COMMENT '实例运行的结束时间:毫秒时间戳',
    `duration` BIGINT NOT NULL COMMENT '实例运行时长:毫秒',
    `message` VARCHAR(200) NOT NULL COMMENT '实例运行信息',
    `creator` VARCHAR(100) NOT NULL COMMENT '创建者',
    `modifier` VARCHAR(100) NOT NULL COMMENT '修改者',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE (`instance_id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '画像-任务实例';


-- 11. 登录历史表
DROP Table `profile_meta_user_login`;
CREATE TABLE IF NOT EXISTS `profile_meta_user_login`(
    `id` BIGINT UNSIGNED AUTO_INCREMENT COMMENT '自增ID',
    `user_id` VARCHAR(40) NOT NULL COMMENT '用户ID',
    `login_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    `login_ip` VARCHAR(50) COMMENT '登录IP',
    `login_ua` VARCHAR(500) COMMENT '登录浏览器UserAgent',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_login_time` (`login_time`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '画像-用户登录历史';

-- 20. 引擎表
DROP Table `profile_meta_engine`;
CREATE TABLE `profile_meta_engine` (
    `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
    `status` int NOT NULL DEFAULT 1 COMMENT '状态:1-启用,2-停用',
    `engine_id` varchar(40) NOT NULL COMMENT '引擎ID',
    `engine_name` varchar(100) NOT NULL COMMENT '引擎名称',
    `engine_type` VARCHAR(50) NOT NULL COMMENT '引擎类型:clickhouse,doris,spark,flink',
    `engine_category` varchar(50) NOT NULL DEFAULT 'di' COMMENT '引擎分类:di, analysis',
    `engine_desc` varchar(100) COMMENT '引擎描述',
    `is_default` TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认引擎:0-否,1-是',
    `source_type` int NOT NULL DEFAULT 1 COMMENT '创建方式: 1-系统内置,2-自定义',
    `config` text COMMENT '引擎配置',
    `creator` varchar(100) NOT NULL COMMENT '创建者',
    `modifier` varchar(100) NOT NULL COMMENT '修改者',
    `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE(`engine_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='画像-引擎';

-- 插入默认 ClickHouse 引擎
INSERT INTO `profile_meta_engine` (`status`, `engine_id`, `engine_name`, `engine_type`, `engine_category`, `is_default`, `config`, `creator`, `modifier`)
VALUES (1, '2000000000000001', 'ClickHouse默认计算引擎', 'clickhouse', 'analysis', 1,
 '{"host":"localhost","port":8123,"database":"profile","username":"test","password":"test"}',
 '100000', '100000'),
 (1, '2000000000000002', 'DataX默认同步引擎', 'datax', 'di', 1, '', '100000', '100000'),
 (1, '2000000000000003', 'DolphinScheduler默认调度引擎', 'dolphinscheduler', 'schedule', 1,
 '{"apiUrl":"http://localhost:12345/dolphinscheduler","token":"2414ea93cbc65f55497da84a1e1b1973","projectCode":"22012166559808","callbackBaseUrl":"http://localhost:3000"}',
 '100000', '100000');



 INSERT INTO `profile_meta_engine` (`status`, `engine_id`, `engine_name`, `engine_type`, `engine_category`, `is_default`, `config`, `creator`, `modifier`)
VALUES 
 (1, '2000000000000003', 'DolphinScheduler默认调度引擎', 'dolphinscheduler', 'schedule', 1,
 '{"apiUrl":"http://localhost:12345/dolphinscheduler","token":"2414ea93cbc65f55497da84a1e1b1973","projectCode":"22012166559808","callbackBaseUrl":"http://localhost:3000"}',
 '100000', '100000');

-- 21. 用户标签关联表
DROP TABLE IF EXISTS `profile_user_label`;
CREATE TABLE IF NOT EXISTS `profile_user_label` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` VARCHAR(64) NOT NULL COMMENT '用户ID',
  `label_id` VARCHAR(64) NOT NULL COMMENT '标签ID（关联 profile_meta_label.label_id）',
  `label_value` VARCHAR(500) DEFAULT NULL COMMENT '标签值（多值逗号分隔，从数据集表同步）',
  `sort_order` INT DEFAULT 0 COMMENT '类目排序序号（拖拽排序用）',
  `gmt_create` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_label` (`user_id`, `label_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='画像-用户标签关联表';

-- 22. 应用管理
DROP TABLE IF EXISTS `profile_meta_application`;
CREATE TABLE IF NOT EXISTS `profile_meta_application` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT COMMENT '自增ID',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态: 1-启用, 2-停用',
    `app_name` VARCHAR(100) NOT NULL COMMENT '应用名称',
    `app_desc` VARCHAR(200) COMMENT '应用描述',
    `app_key` VARCHAR(64) NOT NULL COMMENT '应用Key（唯一标识 + API凭证）',
    `app_secret` VARCHAR(128) NOT NULL COMMENT '应用Secret（API密钥）',
    `target_config` TEXT COMMENT '投递目标配置(JSON)，可选，适配不同数据源类型',
    `webhook_url` VARCHAR(200) COMMENT 'Webhook地址（可选，用于回调通知）',
    `rate_limit` INT DEFAULT 100 COMMENT 'API调用频率限制（次/分钟）',
    `ip_whitelist` TEXT COMMENT 'IP白名单（逗号分隔，为空则不限制）',
    `source_type` INT NOT NULL DEFAULT 2 COMMENT '创建方式: 1-系统内置, 2-自定义',
    `owner` VARCHAR(100) NOT NULL COMMENT '负责人',
    `creator` VARCHAR(100) NOT NULL COMMENT '创建者',
    `modifier` VARCHAR(100) NOT NULL COMMENT '修改者',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE (`app_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='画像-应用管理';

-- 15. 投递记录
DROP TABLE IF EXISTS `profile_delivery_record`;
CREATE TABLE IF NOT EXISTS `profile_delivery_record` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT COMMENT '自增ID',
    `export_id` VARCHAR(40) NOT NULL COMMENT '投递ID',
    `task_instance_id` VARCHAR(40) NOT NULL COMMENT '任务实例ID',
    `group_id` VARCHAR(40) NOT NULL COMMENT '群组ID',
    `delivery_type` VARCHAR(20) NOT NULL COMMENT '投递类型：datasource/application',
    `target_type` VARCHAR(20) COMMENT '目标类型：table/file/topic/index',
    `record_count` INT COMMENT '投递记录数',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态：1-成功,2-失败',
    `error_message` TEXT COMMENT '错误信息',
    `start_time` DATETIME COMMENT '开始时间',
    `end_time` DATETIME COMMENT '结束时间',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_export_id` (`export_id`),
    INDEX `idx_task_instance_id` (`task_instance_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='画像-投递记录';


-- 21. 群组分析
CREATE TABLE IF NOT EXISTS `profile_meta_group_analysis`(
    `id` BIGINT UNSIGNED AUTO_INCREMENT COMMENT '自增ID',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态:1-启用,2-停用',
    `analysis_id` VARCHAR(40) NOT NULL COMMENT '分析ID',
    `analysis_name` VARCHAR(200) NOT NULL COMMENT '分析名称',
    `analysis_desc` VARCHAR(500) COMMENT '分析描述',
    `group_id` VARCHAR(40) NOT NULL COMMENT '当前群组ID',
    `compare_group_ids` VARCHAR(2000) COMMENT '对比群组ID列表(JSON数组)',
    `label_ids` VARCHAR(5000) COMMENT '已选标签ID列表(JSON数组)',
    `source_type` INT DEFAULT 2 COMMENT '创建方式:1-系统内置,2-自定义',
    `owner` VARCHAR(100) COMMENT '负责人',
    `creator` VARCHAR(100) NOT NULL COMMENT '创建者',
    `modifier` VARCHAR(100) NOT NULL COMMENT '修改者',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE (`analysis_id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='画像-群组分析';

-- 23. 角色表
DROP TABLE IF EXISTS `profile_meta_role`;
CREATE TABLE IF NOT EXISTS `profile_meta_role`(
    `id` BIGINT UNSIGNED AUTO_INCREMENT COMMENT '自增ID',
    `role_id` VARCHAR(40) NOT NULL COMMENT '角色ID',
    `role_type` INT NOT NULL DEFAULT 2 COMMENT '角色类型：1-管理员(跳过权限验证),2-普通成员(需要验证权限)',
    `role_name` VARCHAR(100) NOT NULL COMMENT '角色名称',
    `role_desc` VARCHAR(255) COMMENT '角色描述',
    `source_type` INT NOT NULL DEFAULT 1 COMMENT '创建方式:1-系统内置,2-自定义',
    `creator` VARCHAR(100) NOT NULL COMMENT '创建者',
    `modifier` VARCHAR(100) NOT NULL COMMENT '修改者',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE(`role_id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='画像-角色';

-- 24. 用户角色关联表
DROP TABLE IF EXISTS `profile_meta_user_role`;
CREATE TABLE IF NOT EXISTS `profile_meta_user_role`(
    `id` BIGINT UNSIGNED AUTO_INCREMENT COMMENT '自增ID',
    `user_id` VARCHAR(40) NOT NULL COMMENT '用户ID',
    `role_id` VARCHAR(40) NOT NULL COMMENT '角色ID',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE(`user_id`,`role_id`),
    KEY `idx_user_id` (`user_id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='画像-用户角色关联';

-- 25. 权限点表
DROP TABLE IF EXISTS `profile_meta_permission`;
CREATE TABLE IF NOT EXISTS `profile_meta_permission`(
    `id` BIGINT UNSIGNED AUTO_INCREMENT COMMENT '自增ID',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态:1-启用,2-停用',
    `permission_id` VARCHAR(40) NOT NULL COMMENT '权限点ID',
    `permission_name` VARCHAR(100) NOT NULL COMMENT '权限点名称',
    `permission_code` VARCHAR(100) NOT NULL COMMENT '权限码,如 label:create',
    `permission_type` INT NOT NULL DEFAULT 2 COMMENT '权限类型:1-菜单,2-按钮,3-API',
    `parent_id` VARCHAR(40) NOT NULL DEFAULT '0' COMMENT '父权限ID(菜单树),顶层为0',
    `menu_path` VARCHAR(200) COMMENT '前端路由路径(菜单类)',
    `api_pattern` VARCHAR(200) COMMENT 'API匹配模式,如 POST:/label/save',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '同级排序',
    `source_type` INT NOT NULL DEFAULT 1 COMMENT '创建方式:1-系统内置,2-自定义',
    `creator` VARCHAR(100) NOT NULL COMMENT '创建者',
    `modifier` VARCHAR(100) NOT NULL COMMENT '修改者',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE(`permission_id`),
    UNIQUE(`permission_code`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='画像-权限点';

-- 26. 角色-权限关联表
DROP TABLE IF EXISTS `profile_meta_role_permission`;
CREATE TABLE IF NOT EXISTS `profile_meta_role_permission`(
    `id` BIGINT UNSIGNED AUTO_INCREMENT COMMENT '自增ID',
    `role_id` VARCHAR(40) NOT NULL COMMENT '角色ID',
    `permission_id` VARCHAR(40) NOT NULL COMMENT '权限点ID',
    `creator` VARCHAR(100) NOT NULL COMMENT '创建者',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE(`role_id`,`permission_id`),
    KEY `idx_role_id` (`role_id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='画像-角色权限关联';

-- ==============================
-- 资源授权表（P1 数据权限）
-- ==============================
DROP TABLE IF EXISTS `profile_meta_resource_grant`;
CREATE TABLE IF NOT EXISTS `profile_meta_resource_grant`(
    `id` BIGINT UNSIGNED AUTO_INCREMENT COMMENT '自增ID',
    `grant_id` VARCHAR(40) NOT NULL COMMENT '授权ID',
    `resource_type` VARCHAR(10) NOT NULL COMMENT '资源类型（ModelType编码）',
    `resource_id` VARCHAR(40) NOT NULL COMMENT '资源ID',
    `grantee_type` INT NOT NULL COMMENT '受权者类型：1=用户 2=角色',
    `grantee_id` VARCHAR(40) NOT NULL COMMENT '受权者ID',
    `action` INT NOT NULL COMMENT '权限动作：1=READ 2=WRITE 3=EXPORT 4=MANAGE',
    `expire_time` DATETIME DEFAULT NULL COMMENT '过期时间（NULL表示永久）',
    `creator` VARCHAR(100) NOT NULL COMMENT '创建者',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE(`grant_id`),
    KEY `idx_resource` (`resource_type`, `resource_id`),
    KEY `idx_grantee` (`grantee_type`, `grantee_id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='画像-资源授权';

-- ==============================
-- 权限申请主表（P2 审批流）
-- ==============================
DROP TABLE IF EXISTS `profile_meta_auth_apply`;
CREATE TABLE IF NOT EXISTS `profile_meta_auth_apply`(
    `id` BIGINT UNSIGNED AUTO_INCREMENT COMMENT '自增ID',
    `apply_id` VARCHAR(40) NOT NULL COMMENT '申请单ID',
    `applicant` VARCHAR(40) NOT NULL COMMENT '申请人ID',
    `apply_reason` VARCHAR(500) COMMENT '申请理由',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态：1=待审批 2=已通过 3=已拒绝 4=已取消',
    `approver` VARCHAR(40) COMMENT '审批人ID',
    `approve_time` DATETIME COMMENT '审批时间',
    `approve_remark` VARCHAR(500) COMMENT '审批备注',
    `creator` VARCHAR(100) NOT NULL COMMENT '创建者',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE(`apply_id`),
    KEY `idx_applicant` (`applicant`),
    KEY `idx_approver` (`approver`),
    KEY `idx_status` (`status`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='画像-权限申请单';

-- ==============================
-- 权限申请明细表（P2 审批流）
-- ==============================
DROP TABLE IF EXISTS `profile_meta_auth_apply_item`;
CREATE TABLE IF NOT EXISTS `profile_meta_auth_apply_item`(
    `id` BIGINT UNSIGNED AUTO_INCREMENT COMMENT '自增ID',
    `item_id` VARCHAR(40) NOT NULL COMMENT '明细ID',
    `apply_id` VARCHAR(40) NOT NULL COMMENT '所属申请单ID',
    `resource_type` VARCHAR(10) NOT NULL COMMENT '资源类型（ModelType编码）',
    `resource_id` VARCHAR(40) NOT NULL COMMENT '资源ID',
    `action` INT NOT NULL COMMENT '权限动作：1=READ 2=WRITE 3=EXPORT 4=MANAGE',
    `expire_time` DATETIME DEFAULT NULL COMMENT '申请过期时间（NULL表示永久）',
    `creator` VARCHAR(100) NOT NULL COMMENT '创建者',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE(`item_id`),
    KEY `idx_apply_id` (`apply_id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='画像-权限申请明细';

-- ==============================
-- 预置角色种子数据
-- ==============================
INSERT INTO `profile_meta_role` (`role_id`, `role_type`, `role_name`, `role_desc`, `source_type`, `creator`, `modifier`)
VALUES
('role_admin',   1, '管理员',   '拥有系统所有权限，可管理用户、角色及全部业务功能', 1, 'system', 'system'),
('role_analyst', 2, '分析师',   '可使用标签、群组、数据集、分析等业务功能，无系统管理权限', 1, 'system', 'system'),
('role_viewer',  2, '只读用户', '仅有查看权限，不可执行任何写操作',                  1, 'system', 'system')
ON DUPLICATE KEY UPDATE `role_name` = VALUES(`role_name`);

-- 管理员账号绑定管理员角色
INSERT INTO `profile_meta_user_role` (`user_id`, `role_id`)
VALUES ('100000', 'role_admin')
ON DUPLICATE KEY UPDATE `role_id` = VALUES(`role_id`);

-- ==============================
-- 系统配置（Key-Value）
-- ==============================
CREATE TABLE IF NOT EXISTS `profile_meta_system_config`(
    `id` BIGINT UNSIGNED AUTO_INCREMENT COMMENT '自增ID',
    `config_group` VARCHAR(50) NOT NULL COMMENT '配置分组: smtp/platform/appearance ...',
    `config_key` VARCHAR(100) NOT NULL COMMENT '配置键',
    `config_value` TEXT COMMENT '配置值（敏感字段加密存储）',
    `config_desc` VARCHAR(200) COMMENT '配置说明',
    `creator` VARCHAR(100) NOT NULL COMMENT '创建者',
    `modifier` VARCHAR(100) NOT NULL COMMENT '修改者',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE(`config_group`, `config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='画像-系统配置';

-- ==============================
-- 告警发送记录
-- ==============================
DROP TABLE IF EXISTS `profile_meta_alert_history`;
CREATE TABLE IF NOT EXISTS `profile_meta_alert_history`(
    `id` BIGINT UNSIGNED AUTO_INCREMENT COMMENT '自增ID',
    `history_id` VARCHAR(40) NOT NULL COMMENT '告警记录ID',
    `task_id` VARCHAR(40) NOT NULL COMMENT '任务ID',
    `instance_id` VARCHAR(40) COMMENT '触发的任务实例ID',
    `alert_condition` VARCHAR(20) NOT NULL COMMENT '命中的触发条件',
    `alert_channel` VARCHAR(20) NOT NULL COMMENT '发送通道:email等',
    `receivers` VARCHAR(500) COMMENT '实际接收人(邮箱,逗号分隔)',
    `subject` VARCHAR(200) COMMENT '告警标题',
    `content` TEXT COMMENT '告警内容',
    `send_status` TINYINT NOT NULL DEFAULT 0 COMMENT '发送状态:0-失败,1-成功,2-跳过(通道未实现/无接收人)',
    `send_message` VARCHAR(500) COMMENT '发送结果说明',
    `creator` VARCHAR(100) NOT NULL COMMENT '创建者',
    `modifier` VARCHAR(100) NOT NULL COMMENT '修改者',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE (`history_id`),
    KEY `idx_task` (`task_id`),
    KEY `idx_instance` (`instance_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='画像-告警发送记录';

-- ==============================
-- 数据资产血缘关系
-- ==============================
DROP TABLE IF EXISTS `profile_meta_lineage`;
CREATE TABLE IF NOT EXISTS `profile_meta_lineage`(
    `id` BIGINT UNSIGNED AUTO_INCREMENT COMMENT '自增ID',
    `lineage_id` VARCHAR(40) NOT NULL COMMENT '血缘关系ID',
    `upstream_type` VARCHAR(20) NOT NULL COMMENT '被依赖方类型:datasource/dataset/label/event/group/analysis/export/application',
    `upstream_id` VARCHAR(64) NOT NULL COMMENT '被依赖方业务ID',
    `downstream_type` VARCHAR(20) NOT NULL COMMENT '引用方类型',
    `downstream_id` VARCHAR(64) NOT NULL COMMENT '引用方业务ID',
    `relation_type` VARCHAR(30) NOT NULL DEFAULT 'reference' COMMENT '关系语义:derive-派生,reference-引用,consume-消费,export-投递',
    `source_type` INT NOT NULL DEFAULT 1 COMMENT '创建方式:1-系统自动采集,2-手动登记',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '关系备注',
    `creator` VARCHAR(100) NOT NULL COMMENT '创建者',
    `modifier` VARCHAR(100) NOT NULL COMMENT '修改者',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_edge` (`upstream_type`,`upstream_id`,`downstream_type`,`downstream_id`,`relation_type`),
    KEY `idx_upstream` (`upstream_type`,`upstream_id`),
    KEY `idx_downstream` (`downstream_type`,`downstream_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='画像资产血缘关系表';