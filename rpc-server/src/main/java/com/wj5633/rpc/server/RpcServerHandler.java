package com.wj5633.rpc.server;

import com.wj5633.rpc.common.bean.RpcRequest;
import com.wj5633.rpc.common.bean.RpcResponse;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 *
 * @author wangjie
 * @version 1.0.0
 * @create 2019/3/20 2:36
 * @description
 */

@Slf4j
public class RpcServerHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private final Map<String, Object> handlerMap;

    public RpcServerHandler(Map<String, Object> handlerMap) {
        this.handlerMap = handlerMap;
    }

    public void channelRead0(ChannelHandlerContext ctx, RpcRequest request) throws Exception {
        RpcResponse response = new RpcResponse();
        response.setRequestId(request.getRequestId());

        try {
            Object result = handle(request);
            response.setResult(result);
        } catch (Exception e) {
            response.setException(e);
            log.error("handle result failure.", e);
        }
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private Object handle(RpcRequest request) throws Exception {
        String serviceName = request.getInterfaceName();
        Object serviceBean = handlerMap.get(serviceName);

        if (serviceBean == null) {
            throw new RuntimeException(String.format("can not find service bean by key: %s", serviceName));
        }

        Class<?> serviceClass = serviceBean.getClass();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();

        Method method = serviceClass.getMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(serviceBean, parameters);
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("server caught exception", cause);
        ctx.close();
    }
}
