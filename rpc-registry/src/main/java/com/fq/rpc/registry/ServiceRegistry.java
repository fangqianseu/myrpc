/*
Date: 05/14,2019, 08:02
*/
package com.fq.rpc.registry;

/**
 * 服务注册接口
 */
public interface ServiceRegistry {
    /**
     * 服务注册函数
     * @param serviceName 服务名称
     * @param serviceAddress 服务地址
     * @return
     */
    boolean register(String serviceName, String serviceAddress);
}
