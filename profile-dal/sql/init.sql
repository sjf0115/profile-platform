
-- 用户
DROP Table `profile_meta_user`;
CREATE TABLE IF NOT EXISTS `profile_meta_user`(
    `id` BIGINT UNSIGNED AUTO_INCREMENT COMMENT '自增ID',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态:启用-1,停用-2',
    `user_id` VARCHAR(40) NOT NULL COMMENT '用户ID',
    `user_name` VARCHAR(100) NOT NULL COMMENT '用户名称',
    `source_type` VARCHAR(100) NOT NULL DEFAULT 1 COMMENT '创建方式: 系统内置-1,自定义-2',
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
   `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态:启用-1,停用-2',
   `entity_id` VARCHAR(40) NOT NULL COMMENT '实体ID',
   `entity_name` VARCHAR(100) NOT NULL COMMENT '实体名称',
   `source_type` VARCHAR(100) NOT NULL DEFAULT 1 COMMENT '创建方式: 系统内置-1,自定义-2',
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
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态:启用-1,停用-2',
    `entity_type_id` VARCHAR(40) NOT NULL COMMENT '实体类型ID',
    `entity_type_name` VARCHAR(100) NOT NULL COMMENT '实体类型名称',
    `source_type` VARCHAR(100) NOT NULL DEFAULT 1 COMMENT '创建方式: 系统内置-1,自定义-2',
    `creator` VARCHAR(100) NOT NULL COMMENT '创建者',
    `modifier` VARCHAR(100) NOT NULL COMMENT '修改者',
    `gmt_create` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `gmt_modified` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '画像-实体类型';