package com.remoting.serialization.serializer.impl;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import com.remoting.serialization.serializer.ISerializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class HessionSerializer implements ISerializer {
    @Override
    public <T> byte[] serialize(T obj) {
        if (obj == null)
            throw new NullPointerException();
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            HessianOutput hessianOutput = new HessianOutput(byteArrayOutputStream);

            hessianOutput.writeObject(obj);

            return byteArrayOutputStream.toByteArray();
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        if (data == null)
            throw new NullPointerException();

        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);

            HessianInput hessianInput = new HessianInput(byteArrayInputStream);

            return (T)hessianInput.readObject();
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
