package com.remoting.revoker;

import com.remoting.cluster.ClusterStrategy;
import com.remoting.cluster.engine.ClusterEngine;
import com.remoting.model.AresRequest;
import com.remoting.model.AresResponse;
import com.remoting.model.ProviderService;
import com.remoting.zookeeper.IRegisterCenter4Invoker;
import com.remoting.zookeeper.RegisterCenter;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class RevokerProxyBeanFactory implements InvocationHandler {
    private ExecutorService fixedThreadPool = null;

    //服务接口
    private Class<?> targetInterface;
    //超时时间
    private int consumeTimeout;
    //调用者线程数
    private static int threadWorkerNumber = 10;
    //负载均衡策略
    private String clusterStrategy;

    public RevokerProxyBeanFactory(Class<?> targetInterface,int consumeTimeout,String clusterStrategy){
        this.targetInterface = targetInterface;
        this.consumeTimeout = consumeTimeout;
        this.clusterStrategy = clusterStrategy;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //服务接口名称
        String serviceKey = targetInterface.getName();
        //获取某个接口的服务提供者列表
        IRegisterCenter4Invoker registerCenter4Invoker = RegisterCenter.singleton();
        List<ProviderService> providerServiceList =
                registerCenter4Invoker.getServiceMetaDataMap4Consume().get(serviceKey);
        //根据软负载策略，从服务提供者列表选取本次调研的服务提供者
        ClusterStrategy clusterStrategy = ClusterEngine.queryClusterStrategy(this.clusterStrategy);
        ProviderService providerService = clusterStrategy.select(providerServiceList);
        //复制一份提供者信息
        ProviderService newProvider = providerService.copy();
        //设置本次调用服务的方法以及接口
        newProvider.setServiceMethod(method);
        newProvider.setServiceItf(targetInterface);

        //使用AresRequest对象发起一次请求
        final AresRequest request = new AresRequest();
        //设置本次调用的唯一标识
        request.setUniqueKey(UUID.randomUUID().toString() + "-" + Thread.currentThread());
        //设置本次调用的服务调用者信息
        request.setProviderService(newProvider);
        //设置本次调用的方法名称
        request.setInvokedMethodName(method.getName());
        //设置本次调用的超时时间
        request.setInvokeTimeout(consumeTimeout);
        //设置本次调用的方法参数信息
        request.setArgs(args);

        try {
            //构建用来发起调用的线程池
            if (fixedThreadPool == null){
                synchronized (RevokerProxyBeanFactory.class){
                    if (null == fixedThreadPool) {
                        fixedThreadPool = Executors.newFixedThreadPool(threadWorkerNumber);
                    }
                }
            }
            //根据服务调用者的ip,port，构建InetSocketAddress对象，标识服务提供者地址
            String serverIp = request.getProviderService().getServerIp();
            int serverPort = request.getProviderService().getServerPort();
            InetSocketAddress socketAddress = new InetSocketAddress(serverIp,serverPort);
            //提交本次调用信息到线程池，发起调用
            Future<AresResponse> responseFuture = fixedThreadPool.submit(RevokerServiceCallable.of(socketAddress,request));
            //获取调用的返回结果
            AresResponse response = responseFuture.get(request.getInvokeTimeout(), TimeUnit.MILLISECONDS);
            if (response!=null)
                return response.getResult();
        }catch (Exception e){
            throw new RuntimeException();
        }
        return null;
    }

    public Object getProxy(){
        return Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(),new Class[]{targetInterface},this);
    }

    public static volatile RevokerProxyBeanFactory singleton;

    public static RevokerProxyBeanFactory singleton(
            Class<?> targetInterface,int consumeTimeout,String clusterStrategy){
        if (singleton == null){
            synchronized (RevokerProxyBeanFactory.class){
                if (singleton == null)
                    singleton = new RevokerProxyBeanFactory(targetInterface,consumeTimeout,clusterStrategy);
            }
        }
        return singleton;
    }
}
