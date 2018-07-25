package com.remoting.cluster.impl;

import com.remoting.cluster.ClusterStrategy;
import com.remoting.helper.IPHelper;
import com.remoting.model.ProviderService;

import java.util.List;

/**
 * 负载均衡算法：哈希
 */
public class HashClusterStrategyImpl implements ClusterStrategy {
    @Override
    public ProviderService select(List<ProviderService> providerServices) {
        //获取调用方ip
        String localIP = IPHelper.localIp();

        //获取源地址对应的hashCode
        int hashCode = localIP.hashCode();

        //获取服务列表大小
        int size = providerServices.size();

        return providerServices.get(hashCode % size);
    }
}
