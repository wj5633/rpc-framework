package com.wj5633.rpc.server;

import com.wj5633.rpc.common.bean.RpcRequest;
import com.wj5633.rpc.common.bean.RpcResponse;
import com.wj5633.rpc.common.codec.RpcDecoder;
import com.wj5633.rpc.common.codec.RpcEncoder;
import com.wj5633.rpc.registry.ServiceRegistry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 *
 * @author wangjie
 * @version 1.0.0
 * @create 2019/3/20 1:57
 * @description
 */

@Component
@Slf4j
public class RpcServer implements ApplicationContextAware, InitializingBean {

    private Map<String, Object> handlerMap = new ConcurrentHashMap<>();

    @Value("${rpc.port}")
    private int port;

    private final ServiceRegistry serviceRegistry;

    @Autowired
    public RpcServer(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, Object> serviceBeanMap = applicationContext.getBeansWithAnnotation(RpcService.class);
        if (CollectionUtils.isNotEmpty(serviceBeanMap.entrySet())) {
            for (Object serviceBean : serviceBeanMap.values()) {
                RpcService rpcService = serviceBean.getClass().getAnnotation(RpcService.class);

                String serviceName = rpcService.value().getName();
                handlerMap.put(serviceName, serviceBean);
                log.info("handlerMap: {} => {}", serviceName, serviceBean.getClass().getName());
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        EventLoopGroup childGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(group, childGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();

                            pipeline.addLast(new RpcDecoder(RpcRequest.class));
                            pipeline.addLast(new RpcEncoder(RpcResponse.class));
                            pipeline.addLast(new RpcServerHandler(handlerMap));
                        }
                    });
            ChannelFuture future = bootstrap.bind(port).sync();

            log.debug("server started, listening on {}", port);

            String serviceAddress = InetAddress.getLocalHost().getHostAddress() + ":" + port;

            for (String interfaceName : handlerMap.keySet()) {
                serviceRegistry.register(interfaceName, serviceAddress);
                log.debug("register service: {} => {}", interfaceName, serviceAddress);
            }
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            log.error("server exception ", e);
        } finally {
            childGroup.shutdownGracefully();
            group.shutdownGracefully();
        }
    }
}
