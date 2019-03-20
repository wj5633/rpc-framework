package com.wj5633.rpc.client;

import com.wj5633.rpc.common.bean.RpcRequest;
import com.wj5633.rpc.common.bean.RpcResponse;
import com.wj5633.rpc.common.codec.RpcDecoder;
import com.wj5633.rpc.common.codec.RpcEncoder;
import com.wj5633.rpc.registry.ServiceDiscovery;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 *
 * @author wangjie
 * @version 1.0.0
 * @create 2019/3/20 3:01
 * @description
 */

@Component
@Slf4j
public class RpcClient {

    @Autowired
    private ServiceDiscovery serviceDiscovery;

    private Map<String, RpcResponse> responseMap = new ConcurrentHashMap<String, RpcResponse>();

    @SuppressWarnings("unchecked")
    public <T> T create(final Class<?> interfaceClass) {
        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class[]{interfaceClass},
                new InvocationHandler() {

                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        RpcRequest request = new RpcRequest();
                        request.setRequestId(UUID.randomUUID().toString());
                        request.setInterfaceName(method.getDeclaringClass().getName());
                        request.setMethodName(method.getName());
                        request.setParameterTypes(method.getParameterTypes());
                        request.setParameters(args);

                        String serviceName = interfaceClass.getName();
                        String serviceAddress = serviceDiscovery.discover(serviceName);

                        log.debug("discover service: {} => {}", serviceName, serviceAddress);

                        if (StringUtils.isEmpty(serviceAddress)) {
                            throw new RuntimeException("server address is empty.");
                        }
                        String[] array = StringUtils.split(serviceAddress, ":");
                        String host = array[0];
                        int port = Integer.parseInt(array[1]);

                        RpcResponse response = send(request, host, port);
                        if (response == null) {
                            log.error("send request failure.", new IllegalStateException("response is null"));
                            return null;
                        }
                        if (response.hasException()) {
                            log.error("response has exception.", response.getException());
                            return null;
                        }
                        return response.getResult();
                    }

                    private RpcResponse send(RpcRequest request, String host, int port) {
                        EventLoopGroup group = new NioEventLoopGroup();

                        try {
                            Bootstrap bootstrap = new Bootstrap();
                            bootstrap.group(group)
                                    .channel(NioSocketChannel.class)
                                    .handler(new ChannelInitializer<SocketChannel>() {
                                        protected void initChannel(SocketChannel ch) throws Exception {
                                            ChannelPipeline pipeline = ch.pipeline();

                                            pipeline.addLast(new RpcEncoder(RpcRequest.class));
                                            pipeline.addLast(new RpcDecoder(RpcResponse.class));
                                            pipeline.addLast(new RpcClientHandler(responseMap));
                                        }
                                    });

                            ChannelFuture future = bootstrap.connect(host, port).sync();

                            Channel channel = future.channel();
                            channel.writeAndFlush(request).sync();
                            channel.closeFuture().sync();

                            return responseMap.get(request.getRequestId());
                        } catch (Exception e) {
                            log.error("client exception", e);
                            return null;
                        } finally {
                            group.shutdownGracefully();
                            responseMap.remove(request.getRequestId());
                        }
                    }
                }
        );
    }
}
