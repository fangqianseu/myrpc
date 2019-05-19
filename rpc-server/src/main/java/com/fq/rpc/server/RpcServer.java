package com.fq.rpc.server;/*
Date: 05/13,2019, 16:09
*/

import com.fq.rpc.commom.bean.RpcRequest;
import com.fq.rpc.commom.bean.RpcResponse;
import com.fq.rpc.commom.codec.RpcDecoder;
import com.fq.rpc.commom.codec.RpcEncoder;
import com.fq.rpc.registry.ServiceRegistry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import org.jboss.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RpcServer implements ApplicationContextAware, InitializingBean {
    private static final Logger logger = LoggerFactory.getLogger(RpcServer.class);

    private String serviceAddress;
    private ServiceRegistry serviceRegistry;

    private Map<String, Object> handleMap = new HashMap<>();

    private volatile ThreadPoolExecutor threadPoolExecutor;

    public RpcServer(String serviceAddress, ServiceRegistry serviceRegistry) {
        this.serviceAddress = serviceAddress;
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            // 创建并初始化 Netty 服务端 Bootstrap 对象
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel channel) throws Exception {
                    ChannelPipeline pipeline = channel.pipeline();
                    pipeline
                            .addLast(new LengthFieldBasedFrameDecoder(65535, 0, 2, 0, 2)) //解决半包
                            .addLast(new LengthFieldPrepender(2))

                            .addLast(new RpcDecoder(RpcRequest.class))   // request response 编解码
                            .addLast(new RpcEncoder(RpcResponse.class))

                            .addLast(new RpcServerHandler(handleMap, RpcServer.this)); // 处理 RPC 请求
                }
            });

            // 获取 RPC 服务器的 IP 地址与端口号
            String[] addressArray = StringUtil.split(serviceAddress, ':');
            String ip = addressArray[0];
            int port = Integer.parseInt(addressArray[1]);

            // 启动 RPC 服务器
            ChannelFuture future = bootstrap.bind(ip, port).sync();

            // 注册 RPC 服务地址
            if (serviceRegistry != null) {
                for (String interfaceName : handleMap.keySet()) {
                    try {

                        serviceRegistry.register(interfaceName, serviceAddress);
                    } catch (Exception e) {
                        logger.error(e.toString());
                    }
                    logger.debug("register service: {} => {}", interfaceName, serviceAddress);
                }
            }

            logger.debug("server started on port {}", port);

            // 异步关闭 RPC 服务器
//            future.channel().closeFuture().sync();
        } finally {
//            workerGroup.shutdownGracefully();
//            bossGroup.shutdownGracefully();
        }
    }

    /**
     * 扫描带有 RpcService 注解的类并初始化 handlerMap 对象
     *
     * @param applicationContext
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, Object> serviceBeanMap = applicationContext.getBeansWithAnnotation(RpcService.class);
        if (!serviceBeanMap.isEmpty()) {
            for (Object serviceBean : serviceBeanMap.values()) {
                String serviceName = serviceBean.getClass().getAnnotation(RpcService.class).value().getName();
                handleMap.put(serviceName, serviceBean);
                logger.debug("Loading service: {}", serviceName);
            }
        }
    }

    /**
     * 手动添加 服务调用
     *
     * @param interfaceName
     * @param serviceBean
     */
    public void addService(String interfaceName, Object serviceBean) {
        if (!handleMap.containsKey(interfaceName)) {
            logger.info("Add service: {}", interfaceName);
            handleMap.put(interfaceName, serviceBean);
        }
    }

    /**
     * 提交任务 异步执行
     *
     * @param task
     */
    public void submit(Runnable task) {
        if (threadPoolExecutor == null) {
            synchronized (this) {
                if (threadPoolExecutor == null) {
                    threadPoolExecutor = new ThreadPoolExecutor(8, 16, 600L,
                            TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(65536));
                }
            }
        }
        threadPoolExecutor.submit(task);
    }
}
