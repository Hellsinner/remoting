package com.remoting.cluster.impl;

import com.google.common.collect.Lists;
import com.remoting.cluster.ClusterStrategy;
import com.remoting.model.ProviderService;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 负载均衡算法：加权轮询
 */
public class WeightPollingClusterStrategyImpl implements ClusterStrategy {
    //计数器
    private int index = 0;
    private Lock lock = new ReentrantLock();
    @Override
    public ProviderService select(List<ProviderService> providerServices) {
        ProviderService service = null;
        try {
            lock.tryLock(10,TimeUnit.MILLISECONDS);
            //存放加权后的服务提供者列表
            List<ProviderService> providerServiceList = Lists.newArrayList();
            for (ProviderService provider : providerServices){
                int weight = provider.getWeight();
                for (int i=0;i<weight;i++){
                    providerServiceList.add(provider.copy());
                }
            }

            //若计数器大于提供者个数，则归零
            if (index >= providerServiceList.size()){
                index = 0;
            }
            service = providerServiceList.get(index);
            index++;
            return service;
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            lock.unlock();
        }

        //保证程序健壮性，若未取到服务，则返回第一个
        return providerServices.get(0);
    }
}
