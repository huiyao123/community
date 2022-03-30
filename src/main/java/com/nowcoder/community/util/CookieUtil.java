package com.nowcoder.community.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class CookieUtil {
    public static String getValue(HttpServletRequest request,String name) {
        if (request == null || name == null) {
            throw new IllegalArgumentException("参数为空!");
        }
        //获取Cookie对象
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            //遍历cookie集合中的cookie
            for (Cookie cookie:cookies) {
                //判断cookie的name是否等于传入的参数
                if (cookie.getName().equals(name)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
