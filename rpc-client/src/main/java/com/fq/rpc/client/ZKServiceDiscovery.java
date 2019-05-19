/*
Date: 05/17,2019, 10:34
*/
package com.fq.rpc.client;

import com.fq.rpc.registry.zookeeper.ZookeeperServiceDiscovery;

import java.util.List;

public class ZKServiceDiscovery extends ZookeeperServiceDiscovery {
    private ConnectionManager connectionManager;

    public ZKServiceDiscovery(String zkAddress, int zkSession_timeout, int zkConnection_time, String zkRegistry_path) {
        super(zkAddress, zkSession_timeout, zkConnection_time, zkRegistry_path);
    }

    public void setConnectionManager(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    /**
     * 服务地址更新时，同步更新 维护的 长连接
     *
     * @param list
     */
    @Override
    protected void subWatcherHandler(List<String> list) {
        connectionManager.updateRpcClients(list);
    }
}
