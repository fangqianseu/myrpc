/*
Date: 05/15,2019, 09:25
*/
package com.fq.rpc.example.server;

import com.fq.rpc.registry.ServiceRegistry;
import com.fq.rpc.registry.zookeeper.ZookeeperServerRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource({"classpath:user.properties"})
public class ServerConf {
    @Value("${zookeeper.address}")
    private String zk_address;
    @Value("${zookeeper.session_timeout}")
    private int zk_session_timeout;
    @Value("${zookeeper.connection_time}")
    private int zk_connection_time;
    @Value("${zookeeper.registry_path}")
    private String zk_registry_path;
    @Value("${rpc.service_address}")
    private String rpc_service_address;


    @Bean
    public ZookeeperServerRegistry zookeeperServiceRegistry() {
        return new ZookeeperServerRegistry(zk_address, zk_session_timeout, zk_connection_time, zk_registry_path);
    }

    @Bean
    public RpcServer rpcServer(ServiceRegistry serviceRegistry) {
        return new RpcServer(rpc_service_address, serviceRegistry);
    }
}
