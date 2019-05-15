/*
Date: 05/15,2019, 09:41
*/
package com.fq.rpc.example;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RpcExample implements CommandLineRunner {


    public static void main(String[] args) {
        SpringApplication.run(RpcExample.class, args);
    }

    @Override
    public void run(String... strings) throws Exception {
    }
}
