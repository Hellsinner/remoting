package com.remoting.cluster.impl;

import com.remoting.cluster.ClusterStrategy;
import com.remoting.model.ProviderService;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 负载均衡算法：轮询
 */
public class PollingClusterStrategyImpl implements ClusterStrategy {
    //计数器
    private int index = 0;
    private Lock lock = new ReentrantLock();
    @Override
    public ProviderService select(List<ProviderService> providerServices) {
        ProviderService providerService = null;
        try {
            lock.tryLock(10,TimeUnit.MILLISECONDS);
            //若计数器大于服务提供者个数，将计数器归零
            if (index >= providerServices.size()){
                index = 0;
            }
            providerService = providerServices.get(index);
            index++;
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            lock.unlock();
        }

        //保证程序健壮性，若未取到服务，返回第一个
        if (providerService == null){
            providerService = providerServices.get(0);
        }
        return providerService;
    }
}
