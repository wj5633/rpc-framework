package com.wj5633.rpc.hello.service;

import com.wj5633.rpc.hello.api.HelloService;
import com.wj5633.rpc.server.RpcService;

/**
 * Created with IntelliJ IDEA.
 *
 * @author wangjie
 * @version 1.0.0
 * @create 2019/3/20 1:58
 * @description
 */

@RpcService(value = HelloService.class)
public class HelloServiceImpl implements HelloService {

    @Override
    public String say(String name) {
        return "Hello " + name;
    }
}
