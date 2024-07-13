
-- 数据源
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

-- 数据源类型
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

INSERT INTO profile_meta_datasource_type (status, datasource_type_id, datasource_type_name, source_type, config_template, creator, modifier)
values (1, '100001', 'MySQL', 1, '[{"show_name":"Host","key":"host","value":"请输入","required":"1","encrypt":"0","tip":""},{"show_name":"端口号","key":"port","value":"3306","required":"1","encrypt":"0","tip":"port"},{"show_name":"数据库","key":"database","value":"请输入","required":"1","encrypt":"0","tip":"此处填写的数据库是导入或者导出的数据库"},{"show_name":"用户名","key":"user","value":"请输入","required":"1","encrypt":"0","tip":""},{"show_name":"密码","key":"password","value":"请输入","required":"1","encrypt":"1","tip":""}]', 100000, 100000)
     ,(1, '100002', 'ClickHouse', 1, '[{"show_name":"Host","key":"host","value":"请输入","required":"1","encrypt":"0","tip":""},{"show_name":"端口号","key":"port","value":"3306","required":"1","encrypt":"0","tip":"port"},{"show_name":"数据库","key":"database","value":"请输入","required":"1","encrypt":"0","tip":"此处填写的数据库是导入或者导出的数据库"},{"show_name":"用户名","key":"user","value":"请输入","required":"1","encrypt":"0","tip":""},{"show_name":"密码","key":"password","value":"请输入","required":"1","encrypt":"1","tip":""}]', 100000, 100000)
;

-- 数据集
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

-- 用户
DROP Table `profile_meta_user`;
CREATE TABLE IF NOT EXISTS `profile_meta_user`(
                                                  `id` BIGINT UNSIGNED AUTO_INCREMENT COMMENT '自增ID',
                                                  `status` INT NOT NULL DEFAULT 1 COMMENT '状态:启用-1,停用-2',
                                                  `user_id` VARCHAR(40) NOT NULL COMMENT '用户ID',
                                                  `user_name` VARCHAR(100) NOT NULL COMMENT '用户名称',
                                                  `source_type` INT NOT NULL DEFAULT 1 COMMENT '创建方式: 系统内置-1,自定义-2',
                                                  `creator` VARCHAR(100) NOT NULL COMMENT '创建者',
                                                  `modifier` VARCHAR(100) NOT NULL COMMENT '修改者',
                                                  `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                                  `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                                  PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '画像-用户';


-- 实体
DROP Table `profile_meta_entity`;
CREATE TABLE IF NOT EXISTS `profile_meta_entity`(
                                                    `id` BIGINT UNSIGNED AUTO_INCREMENT COMMENT '自增ID',
                                                    `status` INT NOT NULL DEFAULT 1 COMMENT '状态:启用-1,停用-2',
                                                    `entity_id` VARCHAR(40) NOT NULL COMMENT '实体ID',
                                                    `entity_name` VARCHAR(100) NOT NULL COMMENT '实体名称',
                                                    `source_type` INT NOT NULL DEFAULT 1 COMMENT '创建方式: 系统内置-1,自定义-2',
                                                    `creator` VARCHAR(100) NOT NULL COMMENT '创建者',
                                                    `modifier` VARCHAR(100) NOT NULL COMMENT '修改者',
                                                    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                                    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                                    PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '画像-实体';


-- 实体类型
DROP Table `profile_meta_entity_type`;
CREATE TABLE IF NOT EXISTS `profile_meta_entity_type`(
                                                         `id` BIGINT UNSIGNED AUTO_INCREMENT COMMENT '自增ID',
                                                         `status` INT NOT NULL DEFAULT 1 COMMENT '状态:启用-1,停用-2',
                                                         `entity_type_id` VARCHAR(40) NOT NULL COMMENT '实体类型ID',
                                                         `entity_type_name` VARCHAR(100) NOT NULL COMMENT '实体类型名称',
                                                         `source_type` INT NOT NULL DEFAULT 1 COMMENT '创建方式: 系统内置-1,自定义-2',
                                                         `creator` VARCHAR(100) NOT NULL COMMENT '创建者',
                                                         `modifier` VARCHAR(100) NOT NULL COMMENT '修改者',
                                                         `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                                         `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
                                                         PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '画像-实体类型';


-- 标签类目 https://help.aliyun.com/document_detail/432290.html?spm=a2c4g.443644.0.0.35a22449cBafax#section-n1j-vbg-4hj
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

-- 标签
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


-- 任务
DROP Table `profile_meta_task`;
CREATE TABLE IF NOT EXISTS `profile_meta_task`(
                                                  `id` BIGINT UNSIGNED AUTO_INCREMENT COMMENT '自增ID',
                                                  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态:启用-1,停用-2',
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


-- 任务实例
DROP Table `profile_meta_instance`;
CREATE TABLE IF NOT EXISTS `profile_meta_instance`(
                                                      `id` BIGINT UNSIGNED AUTO_INCREMENT COMMENT '自增ID',
                                                      `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态:未运行-1,运行中-2,运行失败-3,运行成功-4',
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