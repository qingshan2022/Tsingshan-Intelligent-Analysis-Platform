package com.qingshan.qsbi;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * 主类（项目启动入口）
 *
 * @author <a href="https://github.com/liqingshan">青山</a>
 * 
 */
// todo 如需关闭 Redis，须在后面添加后面的内容(exclude = {RedisAutoConfiguration.class})
@SpringBootApplication
@MapperScan("com.qingshan.qsbi.mapper")
@EnableScheduling
@EnableWebMvc
@EnableRetry
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
public class MainApplication {

    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }

}
