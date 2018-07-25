package com.remoting.zookeeper;

import com.remoting.model.ProviderService;

import java.util.List;
import java.util.Map;

/**
 * 服务端注册中心接口
 */
public interface IRegisterCenter4Provider {
    /**
     * 服务端将服务提供者信息注册到zk对应的节点下
     */
    public void registerProvider(final List<ProviderService> serviceMetaData);

    /**
     * 服务端获取服务提供者信息
     */
    public Map<String,List<ProviderService>> getProviderServiceMap();
}
