/*
Date: 05/13,2019, 15:55
*/
package com.fq.rpc.server;

import com.fq.roc.commom.bean.RpcRequest;
import com.fq.roc.commom.bean.RpcResponse;
import com.fq.roc.commom.codec.RpcDecoder;
import com.fq.roc.commom.codec.RpcEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@PropertySource({"classpath:user.properties"})
public class Server {
    @Value("${netty.server.port}")
    private int port;

    public void setPort(int port) {
        this.port = port;
    }

    public void run() {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workGroup = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workGroup).channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new RpcEncoder(RpcResponse.class));
                        ch.pipeline().addLast(new RpcDecoder(RpcRequest.class));
                        ch.pipeline().addLast(new ServerHander());
                    }
                });
        try {
            ChannelFuture future = bootstrap.bind(port).sync();
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }

    private class ServerHander extends SimpleChannelInboundHandler<RpcRequest> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, RpcRequest msg) throws Exception {
            System.out.println(msg.getRequestId());

            RpcResponse rpcResponse = new RpcResponse();
            rpcResponse.setRequestId(msg.getRequestId());
            rpcResponse.setResult("success");

            ctx.writeAndFlush(rpcResponse).addListener(ChannelFutureListener.CLOSE);
        }
    }
}
