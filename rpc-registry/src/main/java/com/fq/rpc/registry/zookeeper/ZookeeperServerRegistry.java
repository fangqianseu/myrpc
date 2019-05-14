/*
Date: 05/14,2019, 08:40
*/
package com.fq.rpc.registry.zookeeper;

import com.fq.rpc.registry.ServiceRegistry;
import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class ZookeeperServerRegistry implements ServiceRegistry, InitializingBean {
    private static final Logger logger = LoggerFactory.getLogger(ZookeeperServerRegistry.class);
    private ZkClient zkClient;

    private String zkAddress;
    private int zkSession_timeout;
    private int zkConnection_time;
    private String zkRegistry_path;

    public ZookeeperServerRegistry(String zkAddress, int zkSession_timeout, int zkConnection_time, String zkRegistry_path) {
        this.zkAddress = zkAddress;
        this.zkSession_timeout = zkSession_timeout;
        this.zkConnection_time = zkConnection_time;
        this.zkRegistry_path = zkRegistry_path;
    }

    @Override
    public boolean register(String serviceName, String serviceAddress) {
        try {
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
        zkClient = new ZkClient(zkAddress, zkSession_timeout);
        logger.debug("connect zookeeper");
    }

    public void close() throws Exception {
        if (zkClient != null)
            zkClient.close();
    }
}
