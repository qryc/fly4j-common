package fly4j.common.util;


import org.apache.commons.lang3.RandomStringUtils;

import java.util.Random;

public class RandomUtil {
    public static String getRandomString(int length) { //length表示生成字符串的长度
        String base = "abcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }
    public static String random(int count, boolean letters, boolean numbers) {
        return RandomStringUtils.random(count, letters, numbers);
    }
}
