package com.fq.rpc.test.inter;

import org.springframework.stereotype.Component;

@Component
public interface Hello {
    String sayHello(String msg);
}
