package com.remoting.serialization.serializer.impl;

import com.remoting.serialization.serializer.ISerializer;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class XmlSerializer implements ISerializer {
    private static final XStream X_STREAM = new XStream(new DomDriver());
    @Override
    public <T> byte[] serialize(T obj) {
        return X_STREAM.toXML(obj).getBytes();
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        return (T) X_STREAM.fromXML(new String(data));
    }
}
