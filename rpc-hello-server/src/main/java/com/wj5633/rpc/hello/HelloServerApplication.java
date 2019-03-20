package com.wj5633.rpc.hello;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Created with IntelliJ IDEA.
 *
 * @author wangjie
 * @version 1.0.0
 * @create 2019/3/20 2:58
 * @description
 */

@SpringBootApplication(scanBasePackages = "com.wj5633.rpc")
public class HelloServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(HelloServerApplication.class, args);
    }
}
