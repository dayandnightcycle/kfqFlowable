package com.xazktx.flowable.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SEC_LOG {

    private String LOGID;
    private String USERNAME;//用户明
    private String HOSTADDR;//ip
    private String APPNAME;//应用程序名
    private String MDLNAME;//模块名
    private String DOWHAT;//操作内容
    private Date LOGINTIME;//登录时间
    private String REMARK;//备注
    private String OPERTYPE;//操作类型
    private Integer WARNTYPE;//警告级别

}
