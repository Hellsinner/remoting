package com.remoting.model;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class AresResponseWrapper {
    //存储返回结果的阻塞队列
    private BlockingQueue<AresResponse> responseQueue = new ArrayBlockingQueue<AresResponse>(1);

    //结果返回时间
    private long responseTime;

    /**
     * 计算返回结果是否已经过期
     * @return
     */
    public boolean isExpire(){
        AresResponse aresResponse = responseQueue.peek();

        if (aresResponse == null)
            return false;

        long timeout = aresResponse.getInvokeTimeout();

        if ((System.currentTimeMillis() - responseTime) > timeout){
            return true;
        }

        return false;
    }

    public static AresResponseWrapper of(){
        return new AresResponseWrapper();
    }

    public BlockingQueue<AresResponse> getResponseQueue() {
        return responseQueue;
    }

    public void setResponseQueue(BlockingQueue<AresResponse> responseQueue) {
        this.responseQueue = responseQueue;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }
}
