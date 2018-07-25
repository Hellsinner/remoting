package com.remoting.helper;

import com.remoting.serialization.common.SerializeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;

public class PropertyHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyHelper.class);

    private static final String PROPERTY_CLASSPATH = "/remoting.properties";

    private static final Properties PROPERTIES = new Properties();

    //ZK服务地址
    private static String zkService = "";
    //ZK Session超时时间
    private static int zkSessionTimeout;
    //ZK connection超时时间
    private static int zkConnectionTimeout;
    //序列化算法类型
    private static SerializeType serializeType;
    //每个服务端提供者的Netty连接数
    private static int channelConnectSize;

    public static String getZkService() {
        return zkService;
    }

    public static int getZkSessionTimeout() {
        return zkSessionTimeout;
    }

    public static int getZkConnectionTimeout() {
        return zkConnectionTimeout;
    }

    public static SerializeType getSerializeType() {
        return serializeType;
    }

    public static int getChannelConnectSize() {
        return channelConnectSize;
    }

    /**
     * 初始化
     */
    static {
        InputStream is = null;
        try {
            is = PropertyHelper.class.getResourceAsStream(PROPERTY_CLASSPATH);
            if (is ==null)
                throw new RuntimeException(PROPERTY_CLASSPATH + "not found!!!");
            PROPERTIES.load(is);

            zkService = PROPERTIES.getProperty("zk_service");
            zkSessionTimeout = Integer.parseInt(PROPERTIES.getProperty("zk_sessionTimeout"));
            zkConnectionTimeout = Integer.parseInt(PROPERTIES.getProperty("zk_connectionTimeout"));
            channelConnectSize = Integer.parseInt(PROPERTIES.getProperty("channel_connect_size"));

            String seriType = PROPERTIES.getProperty("serialize_type");
            serializeType = SerializeType.queryByType(seriType);
            if (serializeType == null)
                throw new RuntimeException("serializeType is null");
        }catch (Exception e){
            LOGGER.warn("load "+PROPERTY_CLASSPATH + " failed ");
            throw new RuntimeException(e);
        }finally {
            if (is != null){
                try {
                    is.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
}
