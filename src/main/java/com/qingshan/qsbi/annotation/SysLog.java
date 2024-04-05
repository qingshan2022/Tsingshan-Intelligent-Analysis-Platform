package com.qingshan.qsbi.annotation;

import java.lang.annotation.*;

/**
 * @author 18432
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SysLog {
    /**
     * 执行的内容
     * @return
     */
    String value() default "";
}
