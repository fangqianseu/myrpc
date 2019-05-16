/*
Date: 05/15,2019, 09:26
*/
package com.fq.rpc.example.client;

import com.fq.rpc.client.RpcProxy;
import com.fq.rpc.example.api.Hello;
import com.fq.rpc.example.api.Person;
import com.fq.rpc.registry.zookeeper.ZookeeperServiceDiscovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientExample {
    private static final Logger logger = LoggerFactory.getLogger(ClientExample.class);

    public static void main(String[] args) {
        ZookeeperServiceDiscovery ServiceDiscovery = new ZookeeperServiceDiscovery("120.78.193.198:2181", 5000, 1000, "/rpc-registry");
        RpcProxy rpcProxy = new RpcProxy(ServiceDiscovery);
        Hello hello = rpcProxy.create(Hello.class);

        for (int i = 0; i < 100; i++) {
            String res = hello.sayHello(new Person("fq", 18));
            System.out.println(res);
        }
    }
}
