package com.remoting.zookeeper;

import com.remoting.model.InvokerService;
import com.remoting.model.ProviderService;

import java.util.List;
import java.util.Map;

/**
 * 消费端注册中心接口
 */
public interface IRegisterCenter4Invoker {
    /**
     * 消费端初始化服务提供者信息本地缓存
     */
    public void initProviderMap(String remoteAppKey,String groupName);

    /**
     * 消费端获取服务提供者信息
     */
    public Map<String,List<ProviderService>> getServiceMetaDataMap4Consume();

    /**
     * 消费端将消费者信息注册到zk对应的节点下
     */
    public void registerInvoker(final InvokerService invoker);
}
