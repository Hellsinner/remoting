package com.remoting.serialization.serializer.impl;

import com.alibaba.fastjson.JSON;
import com.remoting.serialization.serializer.ISerializer;

public class JsonSerializer implements ISerializer {
    @Override
    public <T> byte[] serialize(T obj) {
        JSON.DEFFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
        return JSON.toJSONString(obj).getBytes();
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        return (T) JSON.parseObject(new String(data),clazz);
    }
}
