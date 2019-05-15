/*
Date: 05/15,2019, 09:26
*/
package com.fq.rpc.example.client;

import com.fq.rpc.client.RpcProxy;
import com.fq.rpc.example.api.Hello;
import com.fq.rpc.example.api.Person;
import com.fq.rpc.registry.zookeeper.ZookeeperServiceDiscovery;

public class ClientExample {

    public static void main(String[] args) {
        ZookeeperServiceDiscovery ServiceDiscovery = new ZookeeperServiceDiscovery("120.78.193.198:2181", 5000, 1000, "/rpc-registry");
        RpcProxy rpcProxy = new RpcProxy(ServiceDiscovery);
        Hello hello = rpcProxy.create(Hello.class);
        String res = hello.sayHello(new Person("fq", 18));
        System.out.println(res);
    }
}
