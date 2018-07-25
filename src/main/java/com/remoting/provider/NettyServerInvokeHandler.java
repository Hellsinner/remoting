package com.remoting.provider;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;
import com.remoting.model.AresRequest;
import com.remoting.model.AresResponse;
import com.remoting.model.ProviderService;
import com.remoting.zookeeper.IRegisterCenter4Provider;
import com.remoting.zookeeper.RegisterCenter;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
@ChannelHandler.Sharable
public class NettyServerInvokeHandler extends SimpleChannelInboundHandler<AresRequest> {


    private static final Logger LOGGER = LoggerFactory.getLogger(NettyServerInvokeHandler.class);

    //服务端限流
    private static final Map<String,Semaphore> serviceKeySemaphoreMap = Maps.newConcurrentMap();

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        //发生异常，关闭链路
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AresRequest request)
            throws Exception {
        if (ctx.channel().isWritable()){
            //从服务调用对象中获取信息
            ProviderService providerService = request.getProviderService();
            long comsumeTime = request.getInvokeTimeout();
            final String methodName = request.getInvokedMethodName();

            //定位服务提供者
            String serviceKey = providerService.getServiceItf().getName();
            //获取限流工具
            int workerThreads = providerService.getWorkerThreads();

            Semaphore semaphore = serviceKeySemaphoreMap.get(serviceKey);
            if (semaphore == null){
                synchronized (serviceKeySemaphoreMap){
                    semaphore = serviceKeySemaphoreMap.get(serviceKey);
                    if (semaphore == null){
                        semaphore = new Semaphore(workerThreads);
                        serviceKeySemaphoreMap.put(serviceKey,semaphore);
                    }
                }
            }


            //获取注册中心服务
            IRegisterCenter4Provider registerCenter4Provider = RegisterCenter.singleton();
            List<ProviderService> localProviderCaches =
                    registerCenter4Provider.getProviderServiceMap().get(serviceKey);
            Object result = null;
            boolean acquire = false;

            try {
                ProviderService localProviderCache = Collections2.filter(localProviderCaches, new Predicate<ProviderService>() {
                    @Override
                    public boolean apply(ProviderService providerService) {
                        return StringUtils.equals(providerService.getServiceMethod().getName(), methodName);
                    }
                }).iterator().next();

                Object serviceObject = localProviderCache.getServiceObject();

                //使用反射调用方法
                Method method = localProviderCache.getServiceMethod();
                //使用Semaphore限流
                acquire = semaphore.tryAcquire(comsumeTime,TimeUnit.MILLISECONDS);
                if (acquire){
                    result = method.invoke(serviceObject,request.getArgs());
                }
            }catch (Exception e){
                System.out.println(JSON.toJSONString(localProviderCaches) + "  " + methodName+" "+e.getMessage());
                result = e;
            }finally {
                if (acquire){
                    semaphore.release();
                }
            }

            //根据服务调用结果组装调用返回对象
            AresResponse response = new AresResponse();
            response.setInvokeTimeout(comsumeTime);
            response.setUniqueKey(request.getUniqueKey());
            response.setResult(result);

            //写回客户端
            ctx.writeAndFlush(response);
        }else {
            LOGGER.error("------------channel closed!---------------");
        }
    }
}
