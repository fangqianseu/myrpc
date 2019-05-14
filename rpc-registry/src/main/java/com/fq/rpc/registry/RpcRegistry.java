/*
Date: 05/14,2019, 08:56
*/
package com.fq.rpc.registry;

import com.fq.rpc.registry.zookeeper.ZookeeperServerRegistry;
import com.fq.rpc.registry.zookeeper.ZookeeperServiceDiscovery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class RpcRegistry implements CommandLineRunner {
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private ZookeeperServiceDiscovery zookeeperServiceDiscovery;

    public static void main(String[] args) {
        SpringApplication.run(RpcRegistry.class, args);
    }

    @Override
    public void run(String... strings) throws Exception {
        ZookeeperServerRegistry zookeeperServerRegistry1 = applicationContext.getBean(ZookeeperServerRegistry.class);
        zookeeperServerRegistry1.register("service1", "address1");
        ZookeeperServerRegistry zookeeperServerRegistry2 = applicationContext.getBean(ZookeeperServerRegistry.class);
        zookeeperServerRegistry2.register("service1", "address2");
        ZookeeperServerRegistry zookeeperServerRegistry3 = applicationContext.getBean(ZookeeperServerRegistry.class);
        zookeeperServerRegistry3.register("service1", "address3");

        for (int i = 0; i < 10; i++)
            System.out.println("fq " + zookeeperServiceDiscovery.discover("service1"));

        zookeeperServerRegistry1.close();
        zookeeperServerRegistry2.close();
        zookeeperServerRegistry3.close();
    }
}
