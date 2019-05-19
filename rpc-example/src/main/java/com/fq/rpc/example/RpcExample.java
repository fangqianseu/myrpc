/*
Date: 05/15,2019, 09:41
*/
package com.fq.rpc.example;

import com.fq.rpc.registry.ServiceRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RpcExample implements CommandLineRunner {
    @Autowired
    private ServiceRegistry serviceRegistry;

    public static void main(String[] args) {
        SpringApplication.run(RpcExample.class, args);
    }

    @Override
    public void run(String... strings) throws Exception {
        System.in.read();
    }
}
