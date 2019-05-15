/*
Date: 05/15,2019, 08:45
*/
package com.fq.rpc.client;

import com.fq.rpc.commom.bean.RpcRequest;
import com.fq.rpc.commom.bean.RpcResponse;
import com.fq.rpc.registry.ServiceDiscovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

public class RpcProxy {
    private static final Logger logger = LoggerFactory.getLogger(RpcProxy.class);

    private ServiceDiscovery serviceDiscovery;

    public RpcProxy() {
    }

    public RpcProxy(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    /**
     * 返回 远程调用代理实例
     * 采用 jdk 动态代理方式
     *
     * @param interfaceClass
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T create(final Class<?> interfaceClass) {
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                new RpcProxyInvocationHandler(interfaceClass.getName())
        );
    }

    public void setServiceDiscovery(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    private class RpcProxyInvocationHandler implements InvocationHandler {
        private String serviceName;

        public RpcProxyInvocationHandler(String interfacename) {
            this.serviceName = interfacename;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // 创建 RPC 请求对象并设置请求属性
            RpcRequest rpcRequest = new RpcRequest();
            rpcRequest.setRequestId(UUID.randomUUID().toString().replace("-", ""));
            rpcRequest.setInterfaceName(method.getDeclaringClass().getName());
            rpcRequest.setMethodName(method.getName());
            rpcRequest.setParameterTypes(method.getParameterTypes());
            rpcRequest.setParameters(args);

            if (serviceDiscovery == null)
                throw new RuntimeException("server service is empty");

            String serviceAddress = serviceDiscovery.discover(serviceName);
            logger.debug("discover service: {} => {}", serviceName, serviceAddress);

            // 从 RPC 服务地址中解析主机名与端口号
            String[] array = StringUtils.split(serviceAddress, ":");
            String host = array[0];
            int port = Integer.parseInt(array[1]);

            // 创建 RPC 客户端对象并发送 RPC 请求
            RpcClient client = new RpcClient(host, port);
            long time = System.currentTimeMillis();
            RpcResponse response = client.send(rpcRequest);
            logger.debug("time: {}ms", System.currentTimeMillis() - time);

            if (response == null) {
                throw new RuntimeException("response is null");
            }

            // 返回 RPC 响应结果
            if (response.hasException()) {
                throw response.getException();
            } else {
                return response.getResult();
            }
        }
    }
}
