/*
Date: 05/17,2019, 09:32
*/
package com.fq.rpc.client;

import com.fq.rpc.commom.bean.RpcRequest;
import com.fq.rpc.commom.bean.RpcResponse;
import com.fq.rpc.commom.codec.RpcDecoder;
import com.fq.rpc.commom.codec.RpcEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

public class ConnectionManager {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionManager.class);
    private volatile Map<String, RpcClientHandler> clientMap = new ConcurrentHashMap<>();
    private NioEventLoopGroup grop = new NioEventLoopGroup();

    public RpcClientHandler getRpcClient(String address) {
        if (!clientMap.containsKey(address)) {
            logger.error("No such address: %s", address);
            connectToServer(address);
        }
        return clientMap.get(address);
    }

    public synchronized void updateRpcClients(List<String> list) {
        for (String address : list) {
            if (!clientMap.containsKey(address)) {
                connectToServer(address);
            }
        }

        for (String address : clientMap.keySet()) {
            if (!list.contains(address))
                disconnectToServer(address);
        }
    }

    private synchronized void connectToServer(String address) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(grop).channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast(new LengthFieldBasedFrameDecoder(65535, 0, 2, 0, 2)) //解决半包
                                .addLast(new LengthFieldPrepender(2))

                                .addLast(new RpcDecoder(RpcResponse.class))   // request response 编解码
                                .addLast(new RpcEncoder(RpcRequest.class))

                                .addLast(new RpcClientHandler());
                    }
                });

        String[] array = StringUtils.split(address, ":");
        String host = array[0];
        int port = Integer.parseInt(array[1]);

        try {
            ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
            CountDownLatch countDownLatch = new CountDownLatch(1);
            channelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        RpcClientHandler handler = channelFuture.channel().pipeline().get(RpcClientHandler.class);
                        clientMap.put(address, handler);
                    }
                    countDownLatch.countDown();
                }
            });
            countDownLatch.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized void disconnectToServer(String address) {
        RpcClientHandler rpcClient = clientMap.get(address);
        if (rpcClient != null)
            rpcClient.close();
        clientMap.remove(address);
    }

    public void close() {
        grop.shutdownGracefully();
    }
}
