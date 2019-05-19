/*
Date: 05/18,2019, 09:24
*/
package com.fq.rpc.client;

import com.fq.rpc.commom.bean.RpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class RpcResponseResult {
    private static final Logger logger = LoggerFactory.getLogger(RpcResponseResult.class);
    private String requestId;
    private volatile RpcResponse rpcResponse = null;
    private volatile CountDownLatch countDownLatch = new CountDownLatch(1);

    public RpcResponseResult(String requestId) {
        this.requestId = requestId;
    }

    public RpcResponse getRpcResponse() throws InterruptedException {
        countDownLatch.await(8, TimeUnit.SECONDS);
        return rpcResponse;
    }

    public void setRpcResponse(RpcResponse rpcResponse) {
        this.rpcResponse = rpcResponse;
    }

    public void release() {
        countDownLatch.countDown();
    }
}
