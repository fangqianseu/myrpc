/*
Date: 05/13,2019, 10:39
*/
package com.fq.roc.commom.util;

import com.alibaba.fastjson.JSONObject;

public class SerializationUtil {
    public static <T> String serialize(T object) {
        return JSONObject.toJSONString(object);
    }

    public static <T> T deserialize(String data, Class<T> clazz) {
        return JSONObject.parseObject(data, clazz);
    }
}
