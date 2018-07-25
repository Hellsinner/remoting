package com.remoting.zookeeper;

import com.remoting.model.InvokerService;
import com.remoting.model.ProviderService;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * 服务治理接口
 */
public interface IRegisterCenter4Governance {
    /**
     * 获取服务提供者与服务消费者列表
     */
    public Pair<List<ProviderService>,List<InvokerService>>
            queryProviderAndInvokers(String serviceName,String appKey);
}
