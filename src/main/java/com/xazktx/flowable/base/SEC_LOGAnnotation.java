package com.xazktx.flowable.base;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface SEC_LOGAnnotation {

    @AliasFor("dowhat")
    String value() default "";

    @AliasFor("value")
    String dowhat() default "";

    String hostaddr() default "";

    String appname() default "电子政务";

    String mdlname() default "";

    String remark() default "系统管理";

    String opertype() default "操作";

    int warntype() default 1;

}
