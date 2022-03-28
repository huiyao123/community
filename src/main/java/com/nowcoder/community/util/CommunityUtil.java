package com.nowcoder.community.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class CommunityUtil {

    //生成随机字符串
    public static String generateUUID() {
        return UUID.randomUUID().toString().replaceAll("-","");
    }

    //MD5加密(只能加密，不能解密)，将key传入其中进行加密
    public static String md5(String key) {
        //空格，空值都被isBlank认为为空
        if (StringUtils.isBlank(key)) {
            return null;
        }
        //此工具会将传入的结果加密成16进制的字符串返回，需要数据为byte类型
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

}
