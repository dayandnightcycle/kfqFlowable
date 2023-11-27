package com.xazktx.flowable.util;

import org.mindrot.jbcrypt.BCrypt;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class CodeUtils {

    public static String key;

    private CodeUtils() {
    }

    public static String bcrypt(String str) {
        return BCrypt.hashpw(str, BCrypt.gensalt(10));
    }

    public static boolean bcryptCompare(String str1, String str2) {
        return BCrypt.checkpw(str1, str2);
    }

    public static String uuid() {
        return UUID.randomUUID().toString();
    }

    public static String uuidStr() {
        return key = UUID.randomUUID()
                .toString()
                .trim()
                .replaceAll("-", "");
    }

    public static String LOGID() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMddHHmmss");
        Date date = new Date();
        String format = simpleDateFormat.format(date);
        String uuid = UUID.randomUUID()
                .toString()
                .trim()
                .replaceAll("-", "")
                .substring(10, 20)
                .toUpperCase();

        String LOGID = "LOG-" + format + "-" + uuid;
        return LOGID;
    }

}
