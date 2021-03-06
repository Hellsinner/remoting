package com.remoting.revoker;

import com.remoting.model.InvokerService;
import com.remoting.model.ProviderService;
import com.remoting.zookeeper.IRegisterCenter4Invoker;
import com.remoting.zookeeper.RegisterCenter;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.List;
import java.util.Map;

public class RevokerFactoryBean implements FactoryBean,InitializingBean {

    //服务接口
    private Class<?> targetInterface;
    //超时时间
    private int timeout;
    //服务bean
    private Object serviceObject;
    //负载均衡策略
    private String clusterStrategy;
    //服务提供者唯一标识
    private String remoteAppKey;
    //服务分组组名
    private String groupName = "default";

    @Override
    public Object getObject() throws Exception {
        return serviceObject;
    }

    @Override
    public Class<?> getObjectType() {
        return targetInterface;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        //获取服务注册中心
        IRegisterCenter4Invoker registerCenter4Invoker = RegisterCenter.singleton();
        //初始化服务提供者列表到缓存
        registerCenter4Invoker.initProviderMap(remoteAppKey,groupName);

        //初始化Netty Channel
        Map<String,List<ProviderService>> provideMap =
                registerCenter4Invoker.getServiceMetaDataMap4Consume();
        if (MapUtils.isEmpty(provideMap))
            throw new RuntimeException("service provider list is empty");
        NettyChannelPoolFactory.channelPoolFactoryInstance().initChannelPoolFactory(provideMap);
        //获取服务提供者代理对象
        RevokerProxyBeanFactory proxyBeanFactory =
                RevokerProxyBeanFactory.singleton(targetInterface,timeout,clusterStrategy);
        this.serviceObject = proxyBeanFactory.getProxy();

        //将消费者信息注册到注册中心
        InvokerService invoker = new InvokerService();
        invoker.setServiceItf(targetInterface);
        invoker.setRemoteAppkey(remoteAppKey);
        invoker.setGroupName(groupName);
        registerCenter4Invoker.registerInvoker(invoker);
    }

    public Class<?> getTargetInterface() {
        return targetInterface;
    }

    public void setTargetInterface(Class<?> targetInterface) {
        this.targetInterface = targetInterface;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public Object getServiceObject() {
        return serviceObject;
    }

    public void setServiceObject(Object serviceObject) {
        this.serviceObject = serviceObject;
    }

    public String getClusterStrategy() {
        return clusterStrategy;
    }

    public void setClusterStrategy(String clusterStrategy) {
        this.clusterStrategy = clusterStrategy;
    }

    public String getRemoteAppKey() {
        return remoteAppKey;
    }

    public void setRemoteAppKey(String remoteAppKey) {
        this.remoteAppKey = remoteAppKey;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
