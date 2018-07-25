package com.remoting.serialization.serializer.impl;

import com.remoting.serialization.serializer.ISerializer;
import org.jboss.marshalling.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class MarshallingSerializer implements ISerializer {

    final static MarshallingConfiguration CONFIGURATION = new MarshallingConfiguration();
    //获取序列化工厂对象，参数serial标识创建的是java序列化对象
    final static MarshallerFactory MARSHALLER_FACTORY =
            Marshalling.getProvidedMarshallerFactory("serial");
    static {
        CONFIGURATION.setVersion(5);
    }
    @Override
    public <T> byte[] serialize(T obj) {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try {
            final Marshaller marshaller =
                    MARSHALLER_FACTORY.createMarshaller(CONFIGURATION);
            marshaller.start(Marshalling.createByteOutput(byteArrayOutputStream));
            marshaller.writeObject(obj);
            marshaller.finish();
        }catch (Exception e){
            e.printStackTrace();
        }
        return byteArrayOutputStream.toByteArray();
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
            final Unmarshaller unmarshaller =
                    MARSHALLER_FACTORY.createUnmarshaller(CONFIGURATION);
            unmarshaller.start(Marshalling.createByteInput(byteArrayInputStream));
            Object object = unmarshaller.readObject();
            unmarshaller.finish();
            return (T) object;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
