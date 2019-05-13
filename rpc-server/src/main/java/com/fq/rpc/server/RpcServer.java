package com.fq.rpc.server;/*
Date: 05/13,2019, 16:09
*/

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
public class RpcServer implements CommandLineRunner {
    @Autowired
    private Server server;

    public static void main(String[] args) {
        SpringApplication.run(RpcServer.class, args);
    }

    @Override
    public void run(String... strings) throws Exception {
        server.run();
    }
}
