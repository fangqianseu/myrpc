/*
Date: 05/15,2019, 09:26
*/
package com.fq.rpc.example.client;

import com.fq.rpc.client.ConnectionManager;
import com.fq.rpc.client.RpcProxy;
import com.fq.rpc.client.ZKServiceDiscovery;
import com.fq.rpc.example.api.Hello;
import com.fq.rpc.example.api.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientExample {
    private static final Logger logger = LoggerFactory.getLogger(ClientExample.class);

    public static void main(String[] args) throws IOException, InterruptedException {
        ZKServiceDiscovery zkServiceDiscovery = new ZKServiceDiscovery("120.78.193.198:2181", 5000, 1000, "/rpc-registry");
        ConnectionManager connectionManager = new ConnectionManager();
        zkServiceDiscovery.setConnectionManager(connectionManager);

        RpcProxy rpcProxy = new RpcProxy(zkServiceDiscovery, connectionManager);
        Hello hello = rpcProxy.create(Hello.class);

        int threadNum = 10;
        int requestNum = 300;

        ExecutorService executorService = Executors.newFixedThreadPool(threadNum);
        CountDownLatch countDownLatch = new CountDownLatch(threadNum);
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < threadNum; i++) {
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    for (int j = 0; j < requestNum; j++) {
                        System.out.println("hello1 " + hello.sayHello(new Person("fq", 18)));
                    }
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        long timeCost = (System.currentTimeMillis() - startTime);


        String msg = String.format("Async call total-time-cost:%sms, req/s=%s", timeCost, ((double) (requestNum * threadNum)) / timeCost * 1000);
        System.out.println(msg);

        zkServiceDiscovery.close();
        connectionManager.close();
        executorService.shutdown();
    }

}
