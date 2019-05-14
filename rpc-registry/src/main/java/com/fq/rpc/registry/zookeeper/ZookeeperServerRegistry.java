/*
Date: 05/14,2019, 08:40
*/
package com.fq.rpc.registry.zookeeper;

import com.fq.rpc.registry.ServiceRegistry;
import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@PropertySource({"classpath:zookeeper-setting.properties"})
public class ZookeeperServerRegistry implements ServiceRegistry, InitializingBean {
    private static final Logger logger = LoggerFactory.getLogger(ZookeeperServerRegistry.class);
    private ZkClient zkClient;

    @Value("${zookeeper.address}")
    private String zkAddress;
    @Value("${zookeeper.session_timeout}")
    private int zkSession_timeout;
    @Value("${zookeeper.connection_time}")
    private int zkConnection_time;
    @Value("${zookeeper.registry_path}")
    private String zkRegistry_path;

    public void setZkAddress(String zkAddress) {
        this.zkAddress = zkAddress;
    }

    public void setZkSession_timeout(int zkSession_timeout) {
        this.zkSession_timeout = zkSession_timeout;
    }

    public void setZkConnection_time(int zkConnection_time) {
        this.zkConnection_time = zkConnection_time;
    }

    public void setZkRegistry_path(String zkRegistry_path) {
        this.zkRegistry_path = zkRegistry_path;
    }

    @Override
    public boolean register(String serviceName, String serviceAddress) {
        try {
            zkClient = new ZkClient(zkAddress, zkSession_timeout);
            logger.debug("connect zookeeper");

            if (!zkClient.exists(zkRegistry_path)) {
                zkClient.createPersistent(zkRegistry_path);
                zkClient.createPersistent(zkRegistry_path);
                logger.debug("create registry node: {}", zkRegistry_path);
            }

            String servicePath = zkRegistry_path + "/" + serviceName;
            if (!zkClient.exists(servicePath)) {
                zkClient.createPersistent(servicePath);
                logger.debug("create service node: {}", servicePath);
            }

            String addressPath = servicePath + "/address-";
            String addressNode = zkClient.createEphemeralSequential(addressPath, serviceAddress);
            logger.debug("create address node: {}", addressNode);
            return true;
        } catch (Exception e) {
            logger.error(String.format("something wrong [%s] happen at [%s] [%s]", e.toString(), serviceName, serviceAddress));
            return false;
        }

    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }

    public void close() throws Exception {
        if (zkClient != null)
            zkClient.close();
    }
}
