/*
Date: 05/14,2019, 08:40
*/
package com.fq.rpc.registry.zookeeper;

import com.fq.rpc.registry.ServiceRegistry;
import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZookeeperServerRegistry implements ServiceRegistry {
    private static final Logger logger = LoggerFactory.getLogger(ZookeeperServerRegistry.class);

    private String zkRegistry_path;
    private ZkClient zkClient;

    public ZookeeperServerRegistry(String zkAddress, int zkSession_timeout, int zkConnection_time, String zkRegistry_path) {
        this.zkRegistry_path = zkRegistry_path;

        zkClient = new ZkClient(zkAddress, zkSession_timeout, zkConnection_time);
        logger.debug("connect zookeeper");
    }

    @Override
    public void register(String serviceName, String serviceAddress) {
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
        } catch (Exception e) {
            logger.error(String.format("something wrong [%s] happen at [%s] [%s]", e.toString(), serviceName, serviceAddress));
            throw e;
        }
    }

    public void close() {
        if (zkClient != null)
            zkClient.close();
    }
}
