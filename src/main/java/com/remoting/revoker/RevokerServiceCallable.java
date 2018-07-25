package com.remoting.revoker;

import com.remoting.model.AresRequest;
import com.remoting.model.AresResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * 请求发起的任务
 */
public class RevokerServiceCallable implements Callable<AresResponse> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RevokerServiceCallable.class);

    private Channel channel;
    private InetSocketAddress socketAddress;
    private AresRequest request;

    public RevokerServiceCallable(InetSocketAddress socketAddress, AresRequest request) {
        this.socketAddress = socketAddress;
        this.request = request;
    }

    public static RevokerServiceCallable of(InetSocketAddress socketAddress,AresRequest request){
        return new RevokerServiceCallable(socketAddress,request);
    }


    @Override
    public AresResponse call() throws Exception {
        //初始化返回结果容器。将本次调用的唯一标识作为key存入结果的Map
        RevokerResponseHolder.initResponseData(request.getUniqueKey());

        //根据本地调用服务提供者地址获取对应的Netty通道channel队列
        ArrayBlockingQueue<Channel> blockingQueue =
                NettyChannelPoolFactory.channelPoolFactoryInstance().acquire(socketAddress);
        try {
            if (channel == null){
                //从队列中获取本次调用的Netty通道channel
                channel = blockingQueue.poll(request.getInvokeTimeout(),TimeUnit.MILLISECONDS);
            }

            //若获取的channel不可用，则重新获取一个
            while (!channel.isOpen() ||!channel.isActive() ||!channel.isWritable()){
                LOGGER.warn("----------retry get new Channel------------");
                channel = blockingQueue.poll(request.getInvokeTimeout(),TimeUnit.MILLISECONDS);
                if (channel == null){
                    //若队列中没有可用的channel，则重新注册一个
                    channel = NettyChannelPoolFactory.channelPoolFactoryInstance()
                            .registerChannel(socketAddress);
                }
            }

            //将本次调用的信息写入Netty通道，发起异步调用
            ChannelFuture channelFuture = channel.writeAndFlush(request);
            channelFuture.syncUninterruptibly();
            //从返回结果容器中获取返回结果，同时设置等待超时时间为invokeTimeout
            long invokeTimeout = request.getInvokeTimeout();
            return RevokerResponseHolder.getValue(request.getUniqueKey(),invokeTimeout);
        }catch (Exception e){
            LOGGER.error("service invoke error.", e);
        }finally {
            //调用完之后，将Netty的通道重新释放到队列，以便下次使用
            NettyChannelPoolFactory.channelPoolFactoryInstance()
                    .release(blockingQueue,channel,socketAddress);
        }
        return null;
    }
}
