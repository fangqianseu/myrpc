/*
Date: 05/13,2019, 16:26
*/
package com.fq.rpc.client;

import com.fq.rpc.commom.bean.RpcRequest;
import com.fq.rpc.commom.bean.RpcResponse;
import com.fq.rpc.commom.codec.RpcDecoder;
import com.fq.rpc.commom.codec.RpcEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * rpc客户端
 * 发送 rpc 请求
 * 每次请求建立一个 netty连接
 */
public class RpcClient extends SimpleChannelInboundHandler<RpcResponse> {
    private static final Logger logger = LoggerFactory.getLogger(RpcClient.class);
    private String host;
    private int port;
    private RpcResponse rpcResponse;

    public RpcClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse msg) throws Exception {
        this.rpcResponse = msg;
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("api caught exception", cause);
        ctx.close();
    }

    public RpcResponse send(RpcRequest rpcRequest) {
        NioEventLoopGroup grop = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(grop).channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new RpcEncoder(RpcRequest.class));
                        ch.pipeline().addLast(new RpcDecoder(RpcResponse.class));
                        ch.pipeline().addLast((RpcClient.this));
                    }
                });

        try {
            ChannelFuture future = bootstrap.connect(host, port).sync();

            future.channel().writeAndFlush(rpcRequest).sync();
            future.channel().closeFuture().sync();

            // 关闭连接之后 才会返回
            return rpcResponse;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            grop.shutdownGracefully();
        }
        return null;
    }
}
