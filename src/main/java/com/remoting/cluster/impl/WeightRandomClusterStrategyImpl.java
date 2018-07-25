package com.remoting.cluster.impl;

import com.google.common.collect.Lists;
import com.remoting.cluster.ClusterStrategy;
import com.remoting.model.ProviderService;
import org.apache.commons.lang3.RandomUtils;

import java.util.List;

/**
 * 负载均衡算法：加权随机
 */
public class WeightRandomClusterStrategyImpl implements ClusterStrategy {
    @Override
    public ProviderService select(List<ProviderService> providerServices) {
        //存放加权后的服务提供者列表
        List<ProviderService> providerServiceList = Lists.newArrayList();
        for (ProviderService service : providerServices) {
            int weight = service.getWeight();
            for (int i = 0; i < weight; i++) {
                providerServiceList.add(service.copy());
            }
        }

        int MAX_LEN = providerServiceList.size();
        int index = RandomUtils.nextInt(0,MAX_LEN-1);
        return providerServiceList.get(index);
    }
}
