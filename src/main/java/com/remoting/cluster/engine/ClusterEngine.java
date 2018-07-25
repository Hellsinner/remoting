package com.remoting.cluster.engine;

import com.google.common.collect.Maps;
import com.remoting.cluster.ClusterStrategy;
import com.remoting.cluster.impl.*;

import java.util.Map;

public class ClusterEngine {
    private static final Map<ClusterStrategyEnum,ClusterStrategy> CLUSTER_STRATEGY_MAP =
            Maps.newConcurrentMap();

    static {
        CLUSTER_STRATEGY_MAP.put(ClusterStrategyEnum.RANDOM,new RandomClusterStrategyImpl());
        CLUSTER_STRATEGY_MAP.put(ClusterStrategyEnum.Polling,new PollingClusterStrategyImpl());
        CLUSTER_STRATEGY_MAP.put(ClusterStrategyEnum.WeightPolling,new WeightPollingClusterStrategyImpl());
        CLUSTER_STRATEGY_MAP.put(ClusterStrategyEnum.WeightRandom,new WeightRandomClusterStrategyImpl());
        CLUSTER_STRATEGY_MAP.put(ClusterStrategyEnum.Hash,new HashClusterStrategyImpl());
    }

    public static ClusterStrategy queryClusterStrategy(String clusterStrategy){
        ClusterStrategyEnum clusterStrategyEnum = ClusterStrategyEnum.queryByCode(clusterStrategy);
        if (clusterStrategyEnum == null){
            //默认选择随机算法
            return new RandomClusterStrategyImpl();
        }
        return CLUSTER_STRATEGY_MAP.get(clusterStrategyEnum);
    }
}
