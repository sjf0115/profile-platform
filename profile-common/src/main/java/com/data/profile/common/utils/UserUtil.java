package com.data.profile.common.utils;

import java.security.SecureRandom;
import java.util.Random;

/**
 * 功能：用户工具类
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2024/7/2 07:13
 */
public class UserUtil {

    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL_CHARS = "!@#$%^&*";
    private static final String ALL_CHARS = LOWERCASE + UPPERCASE + DIGITS + SPECIAL_CHARS;
    private static final SecureRandom RANDOM = new SecureRandom();

    // 中文昵称形容词
    private static final String[] ADJECTIVES = {
        "快乐的", "聪明的", "勇敢的", "温柔的", "帅气的", "美丽的", "神秘的", "可爱的",
        "调皮的", "安静的", "活泼的", "酷酷的", "萌萌的", "呆呆的", "疯疯的", "懒懒的",
        "甜甜的", "酸酸的", "辣辣的", "咸咸的", "香香的", "臭臭的", "高高的", "矮矮的",
        "胖胖的", "瘦瘦的", "大大的", "小小的", "圆圆的", "方方的", "长长的", "短短的",
        "红红的", "绿绿的", "蓝蓝的", "黄黄的", "白白的", "黑黑的", "紫紫的", "粉粉的"
    };

    // 中文昵称名词
    private static final String[] NOUNS = {
        "小猫", "小狗", "小兔", "小熊", "小猪", "小鹿", "小马", "小牛",
        "小鸟", "小鱼", "小虫", "小猴", "小虎", "小龙", "小蛇", "小龟",
        "星星", "月亮", "太阳", "云朵", "彩虹", "雨滴", "雪花", "风儿",
        "花儿", "草儿", "树儿", "叶儿", "果儿", "瓜儿", "豆儿", "米粒",
        "奶茶", "咖啡", "可乐", "雪碧", "果汁", "蛋糕", "面包", "饼干",
        "糖果", "巧克力", "冰淇淋", "布丁", "果冻", "薯片", "坚果", "水果",
        "程序猿", "产品汪", "运营喵", "设计狮", "测试兔", "运维牛", "架构狮", "算法鱼",
        "码农", "键盘侠", "鼠标手", "屏幕党", "熬夜王", "加班狂", "摸鱼达人", "划水高手"
    };

    /**
     * 生成随机密码
     * 密码长度默认为12位，包含大小写字母、数字和特殊字符
     *
     * @return 随机密码
     */
    public static String generateRandomPassword() {
        return generateRandomPassword(12);
    }

    /**
     * 生成指定长度的随机密码
     * 密码包含大小写字母、数字和特殊字符
     *
     * @param length 密码长度
     * @return 随机密码
     */
    public static String generateRandomPassword(int length) {
        if (length < 4) {
            throw new IllegalArgumentException("密码长度至少为4位");
        }

        StringBuilder password = new StringBuilder(length);

        // 确保至少包含每种类型的一个字符
        password.append(LOWERCASE.charAt(RANDOM.nextInt(LOWERCASE.length())));
        password.append(UPPERCASE.charAt(RANDOM.nextInt(UPPERCASE.length())));
        password.append(DIGITS.charAt(RANDOM.nextInt(DIGITS.length())));
        password.append(SPECIAL_CHARS.charAt(RANDOM.nextInt(SPECIAL_CHARS.length())));

        // 填充剩余长度
        for (int i = 4; i < length; i++) {
            password.append(ALL_CHARS.charAt(RANDOM.nextInt(ALL_CHARS.length())));
        }

        // 打乱字符顺序
        char[] passwordArray = password.toString().toCharArray();
        for (int i = passwordArray.length - 1; i > 0; i--) {
            int j = RANDOM.nextInt(i + 1);
            char temp = passwordArray[i];
            passwordArray[i] = passwordArray[j];
            passwordArray[j] = temp;
        }

        return new String(passwordArray);
    }

    /**
     * 生成中文随机用户昵称
     * 格式为：形容词 + 名词，生成有趣的非正式昵称
     *
     * @return 中文随机昵称
     */
    public static String generateRandomChineseNickname() {
        Random random = new Random();
        String adjective = ADJECTIVES[random.nextInt(ADJECTIVES.length)];
        String noun = NOUNS[random.nextInt(NOUNS.length)];
        
        // 50%概率添加数字后缀
        if (random.nextBoolean()) {
            int number = random.nextInt(100);
            return adjective + noun + number;
        }
        
        return adjective + noun;
    }

    /**
     * 生成指定数量的中文随机用户昵称
     *
     * @param count 数量
     * @return 中文随机昵称数组
     */
    public static String[] generateRandomChineseNicknames(int count) {
        if (count <= 0) {
            return new String[0];
        }
        
        String[] nicknames = new String[count];
        for (int i = 0; i < count; i++) {
            nicknames[i] = generateRandomChineseNickname();
        }
        return nicknames;
    }
}
