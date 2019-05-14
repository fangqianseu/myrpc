/*
Date: 05/14,2019, 08:08
*/
package com.fq.rpc.registry.zookeeper;

import com.fq.rpc.registry.ServiceDiscovery;
import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
@PropertySource({"classpath:zookeeper-setting.properties"})
public class ZookeeperServiceDiscovery implements ServiceDiscovery {
    private static final Logger logger = LoggerFactory.getLogger(ZookeeperServiceDiscovery.class);

    @Value("${zookeeper.address}")
    private String zkAddress;
    @Value("${zookeeper.session_timeout}")
    private int zkSession_timeout;
    @Value("${zookeeper.connection_time}")
    private int zkConnection_time;
    @Value("${zookeeper.registry_path}")
    private String zkRegistry_path;

    public void setZkRegistry_path(String zkRegistry_path) {
        this.zkRegistry_path = zkRegistry_path;
    }

    public void setZkConnection_time(int zkConnection_time) {
        this.zkConnection_time = zkConnection_time;
    }

    public void setZkSession_timeout(int zkSession_timeout) {

        this.zkSession_timeout = zkSession_timeout;
    }

    public void setZkAddress(String zkAddress) {
        this.zkAddress = zkAddress;
    }

    @Override
    public String discover(String serviceName) {
        ZkClient zkClient = new ZkClient(zkAddress, zkSession_timeout, zkConnection_time);
        logger.debug("connect zookeeper");

        String servicePath = zkRegistry_path + "/" + serviceName;
        try {
            // 服务节点没有建立
            if (!zkClient.exists(servicePath))
                throw new RuntimeException(String.format("can not find any service node on path: %s", servicePath));

            List<String> addresslist = zkClient.getChildren(servicePath);

            // 服务节点没有服务地址
            if (addresslist.size() == 0)
                throw new RuntimeException(String.format("can not find any address node on path: %s", servicePath));

            // 从子节点中 随机返回一个服务节点
            String serviceAddressPath = servicePath + "/"+ addresslist.get(ThreadLocalRandom.current().nextInt(addresslist.size()));
            String serviceAddress = zkClient.readData(serviceAddressPath);

            logger.debug(String.format("get {%s} on node path{%s}", serviceAddress, serviceAddressPath));

            return serviceAddress;
        } finally {
            zkClient.close();
        }
    }
}
