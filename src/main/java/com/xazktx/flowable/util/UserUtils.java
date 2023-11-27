package com.xazktx.flowable.util;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;

@Slf4j
public class UserUtils {

    public static String getUserId(HttpServletRequest request) throws Exception {
        String token = request.getHeader("Authorization");
        token = token.substring(7);
        return TokenUtils.getUserId(token);
    }
}
