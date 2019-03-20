package com.wj5633.rpc.server;

import org.springframework.stereotype.Service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created with IntelliJ IDEA.
 *
 * @author wangjie
 * @version 1.0.0
 * @create 2019/3/20 1:59
 * @description
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Service
public @interface RpcService {

    Class<?> value();
}
