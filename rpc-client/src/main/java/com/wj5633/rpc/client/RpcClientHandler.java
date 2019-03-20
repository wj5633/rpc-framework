package com.wj5633.rpc.client;

import com.wj5633.rpc.common.bean.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 *
 * @author wangjie
 * @version 1.0.0
 * @create 2019/3/20 3:13
 * @description
 */

@Slf4j
public class RpcClientHandler extends SimpleChannelInboundHandler<RpcResponse> {

    private Map<String, RpcResponse> responseMap;

    public RpcClientHandler(Map<String, RpcResponse> responseMap) {
        this.responseMap = responseMap;
    }

    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse msg) throws Exception {

        responseMap.put(msg.getRequestId(), msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("client caught exception", cause);
        ctx.close();
    }
}
