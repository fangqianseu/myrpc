/*
Date: 05/13,2019, 16:18
*/
package com.fq.rpc.client;

import com.fq.roc.commom.bean.RpcRequest;
import com.fq.roc.commom.bean.RpcResponse;
import com.fq.roc.commom.codec.RpcDecoder;
import com.fq.roc.commom.codec.RpcEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@PropertySource({"classpath:user.properties"})
public class Client {
    @Value("${netty.client.port}")
    private int port;
    @Value("${netty.client.host}")
    private String ip;

    public void run() {
        NioEventLoopGroup grop = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(grop).channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new RpcEncoder(RpcRequest.class));
                        ch.pipeline().addLast(new RpcDecoder(RpcResponse.class));
                        ch.pipeline().addLast(new ClientHander());
                    }
                });

        try {
            ChannelFuture future = bootstrap.connect(ip, port).sync();

            RpcRequest rpcRequest = new RpcRequest();
            rpcRequest.setRequestId("123456789");

            future.channel().writeAndFlush(rpcRequest).sync();

            future.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            grop.shutdownGracefully();
        }
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    private class ClientHander extends SimpleChannelInboundHandler<RpcResponse> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, RpcResponse msg) throws Exception {
            System.out.println(msg.getRequestId());
            System.out.println(msg.getResult());
        }
    }
}
