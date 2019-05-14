package com.fq.rpc.registry;

/**
 * 服务发现接口
 */
public interface ServiceDiscovery {
    /**
     *
     * @param serviceName 服务名称
     * @return 服务地址
     */
    String discover(String serviceName);
}
