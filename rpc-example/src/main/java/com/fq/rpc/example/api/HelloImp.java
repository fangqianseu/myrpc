/*
Date: 05/15,2019, 09:40
*/
package com.fq.rpc.example.api;

import com.fq.rpc.example.server.RpcService;
import org.springframework.stereotype.Component;

@Component
@RpcService(Hello.class)
public class HelloImp implements Hello {

    @Override
    public String sayHello(Person person) {
        return "hello, " + person.getName();
    }
}
