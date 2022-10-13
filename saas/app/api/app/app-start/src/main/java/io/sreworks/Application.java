package io.sreworks;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


/**
 * @author qiuqiang.qq@alibaba-inc.com
 */
@ComponentScan(basePackages = {"com.alibaba.sreworks", "io.sreworks"})
@EntityScan(basePackages = {"com.alibaba.sreworks.domain.DO"})
@EnableJpaRepositories(basePackages = "com.alibaba.sreworks.domain.repository")
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
