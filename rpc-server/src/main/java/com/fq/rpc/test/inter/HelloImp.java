/*
Date: 05/14,2019, 19:59
*/
package com.fq.rpc.test.inter;

import com.fq.rpc.server.RpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RpcService(Hello.class)
public class HelloImp implements Hello {
    private static final Logger logger = LoggerFactory.getLogger(HelloImp.class);

    @Override
    public String sayHello(String msg) {
        logger.debug("call hello with msg: ", msg);
        return msg;
    }
}
