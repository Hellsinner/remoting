package com.remoting.revoker;

import com.google.common.collect.Maps;
import com.remoting.model.AresResponse;
import com.remoting.model.AresResponseWrapper;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 *返回结果的容器
 */
public class RevokerResponseHolder {
    //服务返回结果
    private static final Map<String,AresResponseWrapper> responseMap = Maps.newConcurrentMap();
    //清除过期的返回结果
    private static final ExecutorService removeExpireKeyExecutor = Executors.newSingleThreadExecutor();

    static {
        //删除超时未获得到结果的key，防止内存泄漏
        removeExpireKeyExecutor.execute(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try {
                        for (Map.Entry<String,AresResponseWrapper> entry : responseMap.entrySet()){
                            boolean isExpire = entry.getValue().isExpire();
                            if (isExpire){
                                responseMap.remove(entry.getKey());
                            }
                            Thread.sleep(10);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    //初始化返回结果容器
    public static void initResponseData(String requestUniqunKey){
        responseMap.put(requestUniqunKey,AresResponseWrapper.of());
    }

    /**
     * 将Netty调用异步返回结果放入阻塞队列
     */
    public static void putResultValue(AresResponse response){
        long currentTime = System.currentTimeMillis();
        AresResponseWrapper responseWrapper = responseMap.get(response.getUniqueKey());

        responseWrapper.setResponseTime(currentTime);
        responseWrapper.getResponseQueue().add(response);

        responseMap.put(response.getUniqueKey(),responseWrapper);
    }

    /**
     * 从阻塞队列中获取Netty异步返回的结果
     */
    public static AresResponse getValue(String requestUniqueKey,long timeout){
        AresResponseWrapper responseWrapper = responseMap.get(requestUniqueKey);

        try {
            return responseWrapper.getResponseQueue().poll(timeout,TimeUnit.MILLISECONDS);
        }catch (Exception e){
            throw new RuntimeException(e);
        }finally {
            responseMap.remove(requestUniqueKey);
        }
    }
}
