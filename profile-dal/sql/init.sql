
DROP DATABASE profile;
CREATE DATABASE profile;

-- 1. 数据源
DROP Table `profile_meta_datasource`;
CREATE TABLE IF NOT EXISTS `profile_meta_datasource`(
    `id` BIGINT UNSIGNED AUTO_INCREMENT COMMENT '自增ID',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态:启用-1,停用-2',
    `datasource_id` VARCHAR(40) NOT NULL COMMENT '数据源ID',
    `datasource_name` VARCHAR(100) NOT NULL COMMENT '数据源名称',
    `datasource_desc` VARCHAR(100) NOT NULL COMMENT '数据源描述',
    `datasource_type_id` VARCHAR(100) NOT NULL COMMENT '数据源类型ID',
    `source_type` INT NOT NULL DEFAULT 1 COMMENT '创建方式: 系统内置-1,自定义-2',
    `config` text NOT NULL COMMENT '数据源配置',
    `creator` VARCHAR(100) NOT NULL COMMENT '创建者',
    `modifier` VARCHAR(100) NOT NULL COMMENT '修改者',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '画像-数据源';

INSERT INTO `profile_meta_datasource` VALUES (1, 1, '040EW35R0G', '数据平台报表MySQL', '数据平台报表MySQL数据源', '0559Y4C0OU', 2, '{\"host\":\"127.0.0.1\",\"port\":\"3306\",\"database\":\"reports\",\"user\":\"root\",\"password\":\"root\"}', '100000', '100000', '2024-07-11 07:43:13', '2024-07-11 07:43:13');

-- 2. 数据源类型
DROP Table `profile_meta_datasource_type`;
CREATE TABLE IF NOT EXISTS `profile_meta_datasource_type`(
    `id` BIGINT UNSIGNED AUTO_INCREMENT COMMENT '自增ID',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态:启用-1,停用-2',
    `datasource_type_id` VARCHAR(40) NOT NULL COMMENT '数据源类型ID',
    `datasource_type_name` VARCHAR(100) NOT NULL COMMENT '数据源类型名称',
    `source_type` INT NOT NULL DEFAULT 1 COMMENT '创建方式: 系统内置-1,自定义-2',
    `config_template` text NOT NULL COMMENT '配置模板',
    `creator` VARCHAR(100) NOT NULL COMMENT '创建者',
    `modifier` VARCHAR(100) NOT NULL COMMENT '修改者',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '画像-数据源类型';

INSERT INTO `profile_meta_datasource_type` VALUES (1, 1, '0559Y4C0OU', 'MySQL', 1, '[{\"show_name\":\"Host\",\"key\":\"host\",\"value\":\"请输入\",\"required\":\"1\",\"encrypt\":\"0\",\"tip\":\"\"},{\"show_name\":\"端口号\",\"key\":\"port\",\"value\":\"3306\",\"required\":\"1\",\"encrypt\":\"0\",\"tip\":\"port\"},{\"show_name\":\"数据库\",\"key\":\"database\",\"value\":\"请输入\",\"required\":\"1\",\"encrypt\":\"0\",\"tip\":\"此处填写的数据库是导入或者导出的数据库\"},{\"show_name\":\"用户名\",\"key\":\"user\",\"value\":\"请输入\",\"required\":\"1\",\"encrypt\":\"0\",\"tip\":\"\"},{\"show_name\":\"密码\",\"key\":\"password\",\"value\":\"请输入\",\"required\":\"1\",\"encrypt\":\"1\",\"tip\":\"\"}]', '100000', '100000', '2024-07-10 07:34:43', '2024-07-11 07:42:10');
INSERT INTO `profile_meta_datasource_type` VALUES (2, 1, '05U08SU24N', 'ClickHouse', 1, '[{\"show_name\":\"Host\",\"key\":\"host\",\"value\":\"请输入\",\"required\":\"1\",\"encrypt\":\"0\",\"tip\":\"\"},{\"show_name\":\"端口号\",\"key\":\"port\",\"value\":\"3306\",\"required\":\"1\",\"encrypt\":\"0\",\"tip\":\"port\"},{\"show_name\":\"数据库\",\"key\":\"database\",\"value\":\"请输入\",\"required\":\"1\",\"encrypt\":\"0\",\"tip\":\"此处填写的数据库是导入或者导出的数据库\"},{\"show_name\":\"用户名\",\"key\":\"user\",\"value\":\"请输入\",\"required\":\"1\",\"encrypt\":\"0\",\"tip\":\"\"},{\"show_name\":\"密码\",\"key\":\"password\",\"value\":\"请输入\",\"required\":\"1\",\"encrypt\":\"1\",\"tip\":\"\"}]', '100000', '100000', '2024-07-10 07:35:26', '2024-07-11 07:42:32');

-- 3. 数据集
DROP Table `profile_meta_dataset`;
CREATE TABLE IF NOT EXISTS `profile_meta_dataset`(
    `id` BIGINT UNSIGNED AUTO_INCREMENT COMMENT '自增ID',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态:启用-1,停用-2',
    `dataset_id` VARCHAR(40) NOT NULL COMMENT '数据集ID',
    `dataset_name` VARCHAR(100) NOT NULL COMMENT '数据集名称',
    `source_type` INT NOT NULL DEFAULT 1 COMMENT '创建方式: 系统内置-1,自定义-2',
    `creator` VARCHAR(100) NOT NULL COMMENT '创建者',
    `modifier` VARCHAR(100) NOT NULL COMMENT '修改者',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '画像-数据源类型';

-- 4. 用户
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
    PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '画像-用户';

INSERT INTO `profile_meta_user` (`status`, `user_id`, `user_name`, `password`, `source_type`, `creator`, `modifier`)
VALUES (1, '100000', 'admin', 'admin', 1, '100000', '100000');


-- 5. 实体
DROP Table `profile_meta_entity`;
CREATE TABLE IF NOT EXISTS `profile_meta_entity`(
    `id` BIGINT UNSIGNED AUTO_INCREMENT COMMENT '自增ID',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态:1-启用,2-停用',
    `entity_id` VARCHAR(40) NOT NULL COMMENT '实体ID',
    `entity_name` VARCHAR(100) NOT NULL COMMENT '实体名称',
    `source_type` INT NOT NULL DEFAULT 1 COMMENT '创建方式: 1-系统内置,2-自定义',
    `creator` VARCHAR(100) NOT NULL COMMENT '创建者',
    `modifier` VARCHAR(100) NOT NULL COMMENT '修改者',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '画像-实体';


INSERT INTO `profile_meta_entity` (`status`, `entity_id`, `entity_name`, `source_type`, `creator`, `modifier`)
VALUES (1, '02P08QFV60', '用户', 1, '100000', '100000');


-- 6. 实体类型
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
    PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '画像-实体类型';

INSERT INTO `profile_meta_entity_type` (`status`, `entity_type_id`, `entity_type_name`, `source_type`, `creator`, `modifier`)
VALUES (1, '03W199ZY5Z', 'uid', 1, '100000', '100000')
;

select version();

select now();


-- 7. 标签类目
DROP Table `profile_meta_label_category`;
CREATE TABLE IF NOT EXISTS `profile_meta_label_category`(
    `id` BIGINT UNSIGNED AUTO_INCREMENT COMMENT '自增ID',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态:启用-1,删除-2',
    `is_default` INT NOT NULL DEFAULT 2 COMMENT '是否是默认兜底类目:是-1,否-2',
    `category_id` VARCHAR(40) NOT NULL COMMENT '标签类目ID',
    `category_name` VARCHAR(100) NOT NULL COMMENT '标签类目名称',
    `category_level` INT NOT NULL COMMENT '标签类目层级',
    `parent_category_id` VARCHAR(40) NOT NULL COMMENT '父标签类目ID',
    `category_seq` INT NOT NULL COMMENT '标签类目同级展示序列, 从1开始',
    `source_type` INT NOT NULL DEFAULT 1 COMMENT '创建方式: 系统内置-1,自定义-2',
    `creator` VARCHAR(100) NOT NULL COMMENT '创建者',
    `modifier` VARCHAR(100) NOT NULL COMMENT '修改者',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '画像-标签类目';


INSERT INTO `profile_meta_label_category` (`status`, `is_default`, `category_id`, `category_name`, `category_level`, `parent_category_id`, `category_seq`, `source_type`, `creator`, `modifier`)
VALUES (1, 1, '08F631JOD5', '未分类', 1, '0', 1, 1, '100000', '100000');


-- 8. 标签
DROP Table `profile_meta_label`;
CREATE TABLE IF NOT EXISTS `profile_meta_label`(
    `id` BIGINT UNSIGNED AUTO_INCREMENT COMMENT '自增ID',
    `label_id` VARCHAR(40) NOT NULL COMMENT '标签ID',
    `label_name` VARCHAR(100) NOT NULL COMMENT '标签名称',
    `label_status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态:1-待上架,2-上架中,3-已上架,4-已冻结,5-下架中,6-已下架',
    `label_type` VARCHAR(100) NOT NULL COMMENT '标签类型: 1-属性标签,2-行为标签',
    `label_desc` VARCHAR(100) NOT NULL COMMENT '标签描述',
    `label_category_id` VARCHAR(100) NOT NULL COMMENT '标签类目ID',

    `label_data_type` INT NOT NULL COMMENT '标签数据类型: 1-string,2-bigint,3-double,4-datetime',
    `label_distribute_type` INT NOT NULL COMMENT '标签数据分布类型: 1-枚举,2-连续',
    `label_organize_type` INT NOT NULL COMMENT '标签组织类型',
    `label_stats_type` INT NOT NULL COMMENT '标签加工类型: 1-事实标签,2-统计标签,3-预测标签',

    `source_type` INT NOT NULL DEFAULT 1 COMMENT '创建方式: 系统内置-1,自定义-2',
    `is_office` INT NOT NULL COMMENT '是否是官方标签:是-1,否-2',

    `owner` VARCHAR(100) NOT NULL COMMENT '标签负责人',
    `creator` VARCHAR(100) NOT NULL COMMENT '标签创建者',
    `modifier` VARCHAR(100) NOT NULL COMMENT '标签最后修改者',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '标签创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '标签最后修改时间',
    PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '画像-标签类目';


-- 9. 任务
DROP Table `profile_meta_task`;
CREATE TABLE IF NOT EXISTS `profile_meta_task`(
    `id` BIGINT UNSIGNED AUTO_INCREMENT COMMENT '自增ID',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态:启用-1,停用-2',
    `category_id` VARCHAR(40) NOT NULL COMMENT '标签类目ID',
    `category_name` VARCHAR(100) NOT NULL COMMENT '标签类目名称',
    `category_level` INT NOT NULL COMMENT '标签类目层级',
    `parent_category_id` VARCHAR(40) NOT NULL COMMENT '父标签类目ID',
    `source_type` INT NOT NULL DEFAULT 1 COMMENT '创建方式: 系统内置-1,自定义-2',
    `creator` VARCHAR(100) NOT NULL COMMENT '创建者',
    `modifier` VARCHAR(100) NOT NULL COMMENT '修改者',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '画像-任务';


-- 10. 任务实例
DROP Table `profile_meta_instance`;
CREATE TABLE IF NOT EXISTS `profile_meta_instance`(
    `id` BIGINT UNSIGNED AUTO_INCREMENT COMMENT '自增ID',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态:未运行-1,运行中-2,运行失败-3,运行成功-4',
    `instance_id` VARCHAR(40) NOT NULL COMMENT '实例ID',
    `instance_name` VARCHAR(100) NOT NULL COMMENT '实例名称',
    `task_id` VARCHAR(100) NOT NULL COMMENT '任务ID',
    `begin_time` BIGINT NOT NULL COMMENT '实例运行的开始时间:毫秒时间戳',
    `finish_time` BIGINT NOT NULL COMMENT '实例运行的结束时间:毫秒时间戳',
    `duration` BIGINT NOT NULL COMMENT '实例运行时长:毫秒',
    `message` VARCHAR(200) NOT NULL COMMENT '实例运行信息',
    `creator` VARCHAR(100) NOT NULL COMMENT '创建者',
    `modifier` VARCHAR(100) NOT NULL COMMENT '修改者',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '画像-任务';


-- 11. 登录表
DROP Table `profile_meta_user_login`;
CREATE TABLE IF NOT EXISTS `profile_meta_user_login`(
    `id` BIGINT UNSIGNED AUTO_INCREMENT COMMENT '自增ID',
    `user_id` VARCHAR(40) NOT NULL COMMENT '用户ID',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '画像-用户登录';