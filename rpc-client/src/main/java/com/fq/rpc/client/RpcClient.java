/*
Date: 05/13,2019, 16:26
*/
package com.fq.rpc.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RpcClient implements CommandLineRunner {
    @Autowired
    private Client client;

    public static void main(String[] args) {
        SpringApplication.run(RpcClient.class, args);
    }

    @Override
    public void run(String... strings) throws Exception {
        client.run();
    }
}
