package com.fq.rpc.registry;

/**
 * 服务发现接口
 */
public interface ServiceDiscovery {
    /**
     * 调用服务
     *
     * @param serviceName
     * @return 服务地址
     */
    String discover(String serviceName);

    /**
     * 订阅rpc服务
     *
     * @param serviceName
     */
    void subscribe(String serviceName);
}
