package com.remoting.serialization.engine;

import com.google.common.collect.Maps;
import com.remoting.serialization.common.SerializeType;
import com.remoting.serialization.serializer.ISerializer;
import com.remoting.serialization.serializer.impl.*;

import java.util.Map;

public class SerializerEngine {
    public static final Map<SerializeType,ISerializer> SERIALIZER_MAP = Maps.newConcurrentMap();

    static {
        SERIALIZER_MAP.put(SerializeType.DefaultJavaSerializer,new DefaultJavaSerializer());

        SERIALIZER_MAP.put(SerializeType.HessionSerializer,new HessionSerializer());

        SERIALIZER_MAP.put(SerializeType.XMLSerializer,new XmlSerializer());

        SERIALIZER_MAP.put(SerializeType.JSONSerializer,new JsonSerializer());

        SERIALIZER_MAP.put(SerializeType.MarshallingSerializer,new MarshallingSerializer());
    }

    public static <T> byte[] serialize(T obj,String serializeType){
        SerializeType serialize = SerializeType.queryByType(serializeType);

        if (serialize == null)
            throw new RuntimeException("serializer is null");

        ISerializer serializer = SERIALIZER_MAP.get(serialize);

        try {
            return serializer.serialize(obj);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public static <T> T deserialize(byte[] data,Class<T> clazz,String serializeType){
        SerializeType serialize = SerializeType.queryByType(serializeType);
        if (serialize == null)
            throw new RuntimeException("serializer is null");

        ISerializer serializer = SERIALIZER_MAP.get(serialize);

        if (serializer == null)
            throw new RuntimeException("serializer error");

        try {
            return serializer.deserialize(data,clazz);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
