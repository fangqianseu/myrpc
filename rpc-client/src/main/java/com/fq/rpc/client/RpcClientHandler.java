/*
Date: 05/13,2019, 16:26
*/
package com.fq.rpc.client;

import com.fq.rpc.commom.bean.RpcRequest;
import com.fq.rpc.commom.bean.RpcResponse;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * rpc客户端
 * 发送 rpc 请求
 * 每次请求建立一个 netty连接
 */
public class RpcClientHandler extends SimpleChannelInboundHandler<RpcResponse> {
    private static final Logger logger = LoggerFactory.getLogger(RpcClientHandler.class);
    private Map<String, RpcResponseResult> responseMap = new ConcurrentHashMap<>();
    private volatile Channel channel;


    public RpcResponse send(RpcRequest rpcRequest) throws InterruptedException {
        channel.writeAndFlush(rpcRequest);
        RpcResponseResult rpcResponseResult = new RpcResponseResult(rpcRequest.getRequestId());
        responseMap.put(rpcRequest.getRequestId(), rpcResponseResult);

        return rpcResponseResult.getRpcResponse();
    }

    public void close() {
        if (channel != null)
            channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        this.channel = ctx.channel();
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse msg) throws Exception {
        RpcResponseResult rpcResponseResult = responseMap.get(msg.getRequestId());
        rpcResponseResult.setRpcResponse(msg);
        rpcResponseResult.release();
    }
}
