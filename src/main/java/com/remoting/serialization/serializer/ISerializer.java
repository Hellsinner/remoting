package com.remoting.serialization.serializer;

public interface ISerializer {

    public <T> byte[] serialize(T obj);


    public <T> T deserialize(byte[] data,Class<T> clazz);
}
