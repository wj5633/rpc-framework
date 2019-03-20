package com.wj5633.rpc.hello;

import com.wj5633.rpc.client.RpcClient;
import com.wj5633.rpc.hello.api.HelloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

/**
 * Created with IntelliJ IDEA.
 *
 * @author wangjie
 * @version 1.0.0
 * @create 2019/3/20 3:23
 * @description
 */

@SpringBootApplication(scanBasePackages = "com.wj5633.rpc")
public class HelloClientApplication {

    @Autowired
    private RpcClient rpcClient;

    @PostConstruct
    public void run() {
        HelloService helloService = rpcClient.create(HelloService.class);
        System.out.println(helloService.say("world"));
    }

    public static void main(String[] args) {
        SpringApplication.run(HelloClientApplication.class, args);
    }
}
