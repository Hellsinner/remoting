package com.remoting.cluster.impl;

import org.apache.commons.lang.StringUtils;

public enum ClusterStrategyEnum {
    //随机算法
    RANDOM("Random"),
    //轮询算法
    Polling("Polling"),
    //哈希算法
    Hash("Hash"),
    //加权随机算法
    WeightRandom("WeightRandom"),
    //加权轮询算法
    WeightPolling("WeightPolling");

    private String code;


    public static ClusterStrategyEnum queryByCode(String code){
        if (StringUtils.isBlank(code))
            return null;
        for (ClusterStrategyEnum strategyEnum : ClusterStrategyEnum.values()){
            if (StringUtils.equals(code,strategyEnum.getCode()))
                return strategyEnum;
        }
        return null;
    }

    private ClusterStrategyEnum(String code){
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
