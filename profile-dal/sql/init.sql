
DROP DATABASE profile;
CREATE DATABASE profile;

-- 1. 用户
DROP Table `profile_meta_user`;
CREATE TABLE IF NOT EXISTS `profile_meta_user`(
    `id` BIGINT UNSIGNED AUTO_INCREMENT COMMENT '自增ID',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态:1-启用,2-停用',
    `user_id` VARCHAR(40) NOT NULL COMMENT '用户ID',
    `user_name` VARCHAR(100) NOT NULL COMMENT '用户名称',
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

-- 4. 数据源Schema
CREATE TABLE `profile_meta_datasource_schema` (
    `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态:1-启用,2-停用',
    `schema_id` varchar(40) NOT NULL COMMENT '数据源 Schema ID',
    `schema_name` varchar(100) NOT NULL COMMENT '数据源 Schema 名称',
    `schema_type` varchar(100) NOT NULL COMMENT '数据源 Schema 类型:1-source,2-sink,3-source/sink',
    `jdbc_protocol` varchar(50) NOT NULL COMMENT '数据源 Schema JDBC 协议 例如 jdbc://mysql',
    `source_type` INT NOT NULL DEFAULT 1 COMMENT '创建方式: 1-系统内置,2-自定义',
    `config_template` text NOT NULL COMMENT '配置模板',
    `creator` varchar(100) NOT NULL COMMENT '创建者',
    `modifier` varchar(100) NOT NULL COMMENT '修改者',
    `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE(`schema_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='画像-数据源Schema';

-- 5. 数据源
DROP Table `profile_meta_datasource`;
CREATE TABLE `profile_meta_datasource` (
    `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
    `status` int NOT NULL DEFAULT 1 COMMENT '状态:1-启用,2-停用',
    `datasource_id` varchar(40) NOT NULL COMMENT '数据源ID',
    `datasource_name` varchar(100) NOT NULL COMMENT '数据源名称',
    `datasource_desc` varchar(100) NOT NULL COMMENT '数据源描述',
    `schema_id` varchar(100) NOT NULL COMMENT '数据源SchemaID',
    `source_type` int NOT NULL DEFAULT 1 COMMENT '创建方式: 1-系统内置,2-自定义',
    `config` text NOT NULL COMMENT '数据源配置',
    `owner` varchar(100) NOT NULL COMMENT '负责人',
    `creator` varchar(100) NOT NULL COMMENT '创建者',
    `modifier` varchar(100) NOT NULL COMMENT '修改者',
    `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE(`datasource_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='画像-数据源';

-- 6. 数据集
DROP Table `profile_meta_dataset`;
CREATE TABLE IF NOT EXISTS `profile_meta_dataset`(
    `id` BIGINT UNSIGNED AUTO_INCREMENT COMMENT '自增ID',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态:1-启用,2-停用',
    `dataset_id` VARCHAR(40) NOT NULL COMMENT '数据集ID',
    `dataset_name` VARCHAR(100) NOT NULL COMMENT '数据集名称',
    `dataset_type` INT NOT NULL DEFAULT 1 COMMENT '数据集类型: 1-标签数据集,2-行为数据集,3-统计数据集',
    `dataset_desc` VARCHAR(200) COMMENT '数据集描述',
    `source_type` INT NOT NULL DEFAULT 1 COMMENT '创建方式: 1-系统内置,2-自定义',
    `datasource_id` VARCHAR(50) NOT NULL COMMENT '同步的数据源ID',
    `table_name` VARCHAR(50) NOT NULL COMMENT '原始数据表名',
    `partition_field` VARCHAR(50) COMMENT '同步的数据表的时间分区字段',
    `partition_format` VARCHAR(50) COMMENT '同步的数据表分区值格式',
    `entity_id` VARCHAR(50) NOT NULL COMMENT '主体(实体)ID',
    `entity_field` VARCHAR(100) NOT NULL COMMENT '主体(实体)标识字段',
    `fields` TEXT NOT NULL COMMENT '数据集字段',
    `instance_id` INT COMMENT '最新执行任务实例ID',
    `instance_status` INT COMMENT '最新执行状态: 1-未运行,2-运行中,3-运行成功,4-运行失败',
    `instance_start_time` DATETIME COMMENT '最新执行开始时间',
    `instance_end_time` DATETIME COMMENT '最新执行结束时间',
    `instance_msg` VARCHAR(500) COMMENT '最新执行信息，只有运行失败时才有',
    `owner` VARCHAR(100) NOT NULL COMMENT '负责人',
    `creator` VARCHAR(100) NOT NULL COMMENT '创建者',
    `modifier` VARCHAR(100) NOT NULL COMMENT '修改者',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE(`dataset_id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '画像-数据集';

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
    `label_status` INT NOT NULL DEFAULT 1 COMMENT '状态:0-待上架,1-在线,2-已下线,3-已暂停',
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
    `entity_id` VARCHAR(50) NOT NULL COMMENT '群组主体ID',
    `source_type` INT NOT NULL DEFAULT 1 COMMENT '创建方式: 1-系统内置,2-自定义',
    `instance_id` INT COMMENT '最新执行任务实例ID',
    `instance_status` INT COMMENT '最新执行状态: 1-未运行,2-运行中,3-运行成功,4-运行失败',
    `instance_start_time` DATETIME COMMENT '最新执行开始时间',
    `instance_end_time` DATETIME COMMENT '最新执行结束时间',
    `instance_msg` VARCHAR(500) COMMENT '最新执行信息，只有运行失败时才有',
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
    `task_type` INT NOT NULL COMMENT '调度任务类型:1-数据集,2-群组圈选',
    `task_related_id` VARCHAR(100) COMMENT '调度任务关联ID',
    `trigger_target_id` VARCHAR(100) COMMENT '调度对象ID',
    `trigger_type` INT NOT NULL COMMENT '调度类型:1-手动触发调度,2-API触发调度,3-周期调度',
    `trigger_cron` VARCHAR(20) COMMENT '调度 cron 表达式:只有周期自动触发更新才有',
    `trigger_url` VARCHAR(20) COMMENT '调度触发URL:只有API触发调度才有',
    `trigger_start_time` VARCHAR(20) COMMENT '触发调度有效开始时间:只有周期自动触发更新才有',
    `trigger_end_time` VARCHAR(20) COMMENT '触发调度有效结束时间:只有周期自动触发更新才有',
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
DROP Table `profile_meta_instance`;
CREATE TABLE IF NOT EXISTS `profile_meta_task_instance`(
    `id` BIGINT UNSIGNED AUTO_INCREMENT COMMENT '自增ID',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态:1-未运行,2-运行中,3-运行失败,4-运行成功',
    `instance_id` VARCHAR(40) NOT NULL COMMENT '实例ID',
    `instance_name` VARCHAR(100) NOT NULL COMMENT '实例名称',
    `task_id` VARCHAR(100) NOT NULL COMMENT '任务ID',
    `instance_related_id` VARCHAR(100) COMMENT '实例关联ID',
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


-- 11. 登录表
DROP Table `profile_meta_user_login`;
CREATE TABLE IF NOT EXISTS `profile_meta_user_login`(
    `id` BIGINT UNSIGNED AUTO_INCREMENT COMMENT '自增ID',
    `user_id` VARCHAR(40) NOT NULL COMMENT '用户ID',
    `creator` VARCHAR(100) NOT NULL COMMENT '创建者',
    `modifier` VARCHAR(100) NOT NULL COMMENT '修改者',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '画像-用户登录';