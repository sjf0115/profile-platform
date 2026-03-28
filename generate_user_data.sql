-- 生成 1000 条用户数据插入 tb_user_base 表
-- 使用存储过程批量插入

-- 修改表字符集为 utf8mb4 以支持中文
ALTER TABLE tb_user_base CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

DELIMITER $$

CREATE PROCEDURE IF NOT EXISTS GenerateUserData()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE v_name VARCHAR(30);
    DECLARE v_age INT;
    DECLARE v_sex VARCHAR(30);
    DECLARE v_email VARCHAR(50);
    
    -- 姓氏和名字库
    DECLARE v_surnames VARCHAR(500) DEFAULT '赵钱孙李周吴郑王冯陈褚卫蒋沈韩杨朱秦尤许何吕施张孔曹严华金魏陶姜戚谢邹喻柏水窦章云苏潘葛奚范彭郎鲁韦昌马苗凤花方俞任袁柳酆鲍史唐费廉岑薛雷贺倪汤滕殷罗毕郝邬安常乐于时傅皮卞齐康伍余元卜顾孟平黄和穆萧尹姚邵湛汪祁毛禹狄米贝明臧计伏成戴谈宋茅庞熊纪舒屈项祝董梁杜阮蓝闵席季麻强贾路娄危江童颜郭梅盛林刁钟徐邱骆高夏蔡田樊胡凌霍万柯卢莫房缪干解应宗丁宣邓郁单杭洪包诸左石崔吉钮龚';
    DECLARE v_names VARCHAR(500) DEFAULT '伟芳娜敏静丽强磊军洋勇艳杰娟涛明超秀霞平刚桂英华建文辉玲珍国梅新志龙凯民瑞琴芳洁海燕春冬秀娟英华慧巧美娜静淑惠珠翠雅芝玉萍红娥玲芬芳燕彩春兰凤洁梅琳素云莲真环雪荣爱妹霞香月莺媛艳瑞凡佳嘉琼勤珍贞莉桂娣叶璧璐娅琦晶妍茜秋珊莎锦黛青倩婷姣婉娴瑾颖露瑶怡婵雁蓓纨仪荷丹蓉眉君琴蕊薇菁梦岚苑婕馨瑗琰韵融园艺咏卿聪澜纯毓悦昭冰爽琬茗羽希宁欣飘育滢馥筠柔竹霭凝晓欢霄枫芸菲寒伊亚宜可姬舒影荔枝思丽';
    
    -- 清空已有数据（可选，如需保留数据请注释此行）
    -- TRUNCATE TABLE tb_user_base;
    
    WHILE i <= 1000 DO
        -- 生成随机姓名
        SET v_name = CONCAT(
            SUBSTRING(v_surnames, FLOOR(1 + RAND() * 100), 1),
            SUBSTRING(v_names, FLOOR(1 + RAND() * 150), 1),
            IF(RAND() > 0.5, SUBSTRING(v_names, FLOOR(1 + RAND() * 150), 1), '')
        );
        
        -- 生成随机年龄 (18-60岁)
        SET v_age = FLOOR(18 + RAND() * 43);
        
        -- 生成随机性别
        SET v_sex = IF(RAND() > 0.5, '男', '女');
        
        -- 生成随机邮箱
        SET v_email = CONCAT(
            LOWER(SUBSTRING(v_name, 1, 3)),
            FLOOR(100 + RAND() * 900),
            '@',
            ELT(FLOOR(1 + RAND() * 5), 'qq.com', '163.com', 'gmail.com', 'outlook.com', 'sina.com')
        );
        
        -- 插入数据
        INSERT INTO tb_user_base (id, name, age, sex, email) 
        VALUES (i, v_name, v_age, v_sex, v_email);
        
        SET i = i + 1;
    END WHILE;
END$$

DELIMITER ;

-- 执行存储过程
CALL GenerateUserData();

-- 删除存储过程（可选）
DROP PROCEDURE IF EXISTS GenerateUserData;

-- 验证数据
SELECT COUNT(*) as total_count FROM tb_user_base;
SELECT * FROM tb_user_base LIMIT 10;
