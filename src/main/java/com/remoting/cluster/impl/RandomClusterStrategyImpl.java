package com.remoting.cluster.impl;

import com.remoting.cluster.ClusterStrategy;
import com.remoting.model.ProviderService;
import org.apache.commons.lang3.RandomUtils;

import java.util.List;

/**
 * 随机的负载均衡算法
 */
public class RandomClusterStrategyImpl implements ClusterStrategy {
    @Override
    public ProviderService select(List<ProviderService> providerServices) {
        int MAX_LEN = providerServices.size();
        int index = RandomUtils.nextInt(0,MAX_LEN-1);
        return providerServices.get(index);
    }
}
