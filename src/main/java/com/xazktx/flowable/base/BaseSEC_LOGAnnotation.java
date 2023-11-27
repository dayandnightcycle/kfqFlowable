package com.xazktx.flowable.base;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface BaseSEC_LOGAnnotation {

    SEC_LOGAnnotation[] value() default @SEC_LOGAnnotation;

    SEC_LOGErrorAnnotation[] error() default @SEC_LOGErrorAnnotation;

}
