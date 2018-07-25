package com.remoting.cluster;

import com.remoting.model.ProviderService;

import java.util.List;

public interface ClusterStrategy {
    /**
     * 负载均衡算法
     */
    public ProviderService select(List<ProviderService> providerServices);
}
