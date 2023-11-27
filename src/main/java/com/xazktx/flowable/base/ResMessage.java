package com.xazktx.flowable.base;

public enum ResMessage {

    SUCCESS(0, "操作成功"),

    ACTIVATE(1, "活动"),

    SUSPENDED(2, "暂停"),

    BACK(2, "退回"),

    FAIL(-1, "操作失败"),

    ERROR_PARAM(-2, "参数错误"),

    NO_RESULT(-3, "无查询结果"),

    ERROR_DB(-4, "数据库错误"),

    ERROR_FILE(-5, "文件错误"),

    ERROR_THIRD(-6, "第三方接口错误"),

    ERROR_OTHER(-7, "其他错误"),

    ERROR_UNAUTH(-8, "没有权限");

    private int code;

    private String message;

    ResMessage(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
