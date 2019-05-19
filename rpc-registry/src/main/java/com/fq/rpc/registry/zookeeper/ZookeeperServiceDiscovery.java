/*
Date: 05/14,2019, 08:08
*/
package com.fq.rpc.registry.zookeeper;

import com.fq.rpc.registry.ServiceDiscovery;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class ZookeeperServiceDiscovery implements ServiceDiscovery {
    private static final Logger logger = LoggerFactory.getLogger(ZookeeperServiceDiscovery.class);
    public ZkClient zkClient;
    public volatile Map<String, List<String>> serviceAddressMap = new ConcurrentHashMap<>();
    private String zkRegistry_path;

    public ZookeeperServiceDiscovery(String zkAddress, int zkSession_timeout, int zkConnection_time, String zkRegistry_path) {
        this.zkRegistry_path = zkRegistry_path;

        zkClient = new ZkClient(zkAddress, zkSession_timeout, zkConnection_time);
        logger.debug("connect zookeeper");
    }

    /**
     * 初次订阅 远程服务，保存已经注册的 rpc提供方地址
     * 注册 watcher，更新 rpc提供方地址 变动
     */
    public void subscribe(String serviceName) {

        String servicePath = zkRegistry_path + "/" + serviceName;
        // 服务节点没有建立
        if (!zkClient.exists(servicePath))
            zkClient.createPersistent(servicePath);

        // 添加 服务节点的watcher
        zkClient.subscribeChildChanges(servicePath, new IZkChildListener() {
            @Override
            public synchronized void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
                logger.error(parentPath + " child changed ");
                ArrayList<String> list = new ArrayList<>();

                for (String childPath : currentChilds) {
                    String serviceAddress = zkClient.readData(parentPath + "/" + childPath);
                    list.add(serviceAddress);
                }
                String[] splits = parentPath.split("/");
                serviceAddressMap.put(splits[splits.length - 1], list);

                subWatcherHandler(list);
            }
        });

        // 存储当前节点的服务提供方
        updateList(serviceName);
        subWatcherHandler(serviceAddressMap.get(serviceName));
    }

    // 子类继承 编写 serviceAddressMap 改变后的代买
    protected void subWatcherHandler(List<String> list) {

    }

    /**
     * 更新 serviceAddressMap 为最新数据
     *
     * @param serviceName
     */
    private void updateList(String serviceName) {
        String servicePath = zkRegistry_path + "/" + serviceName;
        List<String> list = new ArrayList<>();

        List<String> children = zkClient.getChildren(servicePath);
        for (String childPath : children) {
            String serviceAddress = zkClient.readData(servicePath + "/" + childPath);
            list.add(serviceAddress);
        }

        logger.debug(String.format("serviceAddressMap changed."));
        serviceAddressMap.put(serviceName, list);
    }

    @Override
    public String discover(String serviceName) {
//        // 随机返回一个服务节点
        List<String> addresslist = serviceAddressMap.get(serviceName);
        if (addresslist == null || addresslist.size() == 0)
            return "";

        String serviceAddress = addresslist.get(ThreadLocalRandom.current().nextInt(addresslist.size()));
        logger.debug(String.format("select {%s} for service {%s}", serviceAddress, serviceName));
        return serviceAddress;
    }

    public void close() {
        if (zkClient != null)
            zkClient.close();
    }
}
