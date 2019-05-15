/*
Date: 05/13,2019, 15:36
*/
package com.fq.rpc.commom.codec;

import com.fq.rpc.commom.util.SerializationUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class RpcEncoder extends MessageToByteEncoder {
    private Class<?> clazz;

    public RpcEncoder(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        if (clazz.isInstance(msg)) {
            byte[] bytes = SerializationUtil.serialize(msg);
            out.writeInt(bytes.length);
            out.writeBytes(bytes);
        }
    }
}
