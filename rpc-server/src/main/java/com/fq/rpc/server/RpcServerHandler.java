/*
Date: 05/14,2019, 19:40
*/
package com.fq.rpc.server;

import com.fq.rpc.commom.bean.RpcRequest;
import com.fq.rpc.commom.bean.RpcResponse;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class RpcServerHandler extends SimpleChannelInboundHandler<RpcRequest> {
    private static final Logger logger = LoggerFactory.getLogger(RpcServerHandler.class);

    private RpcServer rpcServer;
    private Map<String, Object> handlerMap;

    public RpcServerHandler(Map<String, Object> handlerMap, RpcServer rpcServer) {
        this.handlerMap = handlerMap;
        this.rpcServer = rpcServer;
    }

    /**
     * 异步相应rpc调用，提升系统性能
     *
     * @param ctx
     * @param rpcRequest
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest rpcRequest) {
        rpcServer.submit(new Runnable() {
            @Override
            public void run() {
                RpcResponse rpcResponse = new RpcResponse();
                rpcResponse.setRequestId(rpcRequest.getRequestId());
                try {
                    Object result = handle(rpcRequest);
                    rpcResponse.setResult(result);
                } catch (Exception e) {
                    logger.error(e.toString());
                    rpcResponse.setException(e);
                }
                ctx.writeAndFlush(rpcResponse).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        logger.debug("Send response for request " + rpcRequest.getRequestId());
                    }
                });
            }
        });
    }

    private Object handle(RpcRequest request) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // 获取服务对象
        String serviceName = request.getInterfaceName();

        Object serviceBean = handlerMap.get(serviceName);
        if (serviceBean == null) {
            throw new RuntimeException(String.format("can not find service bean by key: %s", serviceName));
        }

        // 获取反射调用所需的参数
        Class<?> serviceClass = serviceBean.getClass();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();

        // 执行反射调用
        Method method = serviceClass.getMethod(methodName, parameterTypes);
        method.setAccessible(true);
        Object res = method.invoke(serviceBean, parameters);
        logger.debug("Invoke rpc for request [%s]", request.getRequestId());
        return res;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("server caught exception", cause);
        ctx.close();
    }
}
