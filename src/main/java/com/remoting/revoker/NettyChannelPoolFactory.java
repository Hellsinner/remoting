package com.remoting.revoker;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.remoting.helper.PropertyHelper;
import com.remoting.model.AresResponse;
import com.remoting.model.ProviderService;
import com.remoting.serialization.NettyDecoderHandler;
import com.remoting.serialization.NettyEncoderHandler;
import com.remoting.serialization.common.SerializeType;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;

public class NettyChannelPoolFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyChannelPoolFactory.class);

    private static final NettyChannelPoolFactory CHANNEL_POOL_FACTORY = new NettyChannelPoolFactory();

    //Key为服务提供者地址，value为Netty Channel的阻塞队列
    private static final Map<InetSocketAddress,ArrayBlockingQueue<Channel>> channelPoolMap
             = Maps.newConcurrentMap();
    //初始化Netty Channel阻塞队列的长度，该值可配置
    private static final int channelConnectSize = PropertyHelper.getChannelConnectSize();
    //初始化序列化协议类型，配置信息中获取
    private static final SerializeType SERIALIZE_TYPE = PropertyHelper.getSerializeType();
    //服务提供者列表
    private List<ProviderService> serviceMetaDataList = Lists.newArrayList();

    private NettyChannelPoolFactory(){}

    /**
     * 初始化Netty channel 连接队列Map
     */
    public void initChannelPoolFactory(Map<String,List<ProviderService>> providerMap){
        //将服务提供者信息存入serviceMetaDataList中
        Collection<List<ProviderService>> values = providerMap.values();
        for (List<ProviderService> serviceMetaDataModels : values){
            if (CollectionUtils.isEmpty(serviceMetaDataModels))
                continue;
            serviceMetaDataList.addAll(serviceMetaDataModels);
        }

        //获取服务提供者地址列表
        Set<InetSocketAddress> socketAddressSet = Sets.newHashSet();
        for (ProviderService providerService : serviceMetaDataList){
            String serviceIp = providerService.getServerIp();
            int servicePort = providerService.getServerPort();

            InetSocketAddress socketAddress = new InetSocketAddress(serviceIp,servicePort);
            socketAddressSet.add(socketAddress);
        }

        //根据服务提供者地址列表初始化Channel阻塞队列，并以地址key，地址对应的Channel阻塞队列为value，存入PoolMap
        for (InetSocketAddress socketAddress : socketAddressSet){
            try {
                int realChannelConnectionSize = 0;
                while (realChannelConnectionSize < channelConnectSize){
                    Channel channel = null;
                    while (channel == null){
                        //若channel不存在，则注册新的Netty Channel
                        channel = registerChannel(socketAddress);
                    }
                    realChannelConnectionSize++;
                    //将新注册的Netty Channel存入阻塞队列
                    //并将阻塞队列作为value存入PoolFactory
                    ArrayBlockingQueue<Channel> channelArrayBlockingQueue
                                = channelPoolMap.get(socketAddress);
                    if (channelArrayBlockingQueue == null){
                        channelArrayBlockingQueue = new ArrayBlockingQueue<>(channelConnectSize);
                        channelPoolMap.put(socketAddress,channelArrayBlockingQueue);
                    }
                    channelArrayBlockingQueue.offer(channel);
                }
            }catch (Exception e){
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 根据服务提供者地址获取 对应的Netty Channel阻塞队列
     */
    public ArrayBlockingQueue<Channel> acquire(InetSocketAddress socketAddress){
        return channelPoolMap.get(socketAddress);
    }

    /**
     * Channel使用完毕后，回收到阻塞队列
     */
    public void release(ArrayBlockingQueue<Channel> arrayBlockingQueue,Channel channel
                                ,InetSocketAddress socketAddress){
        if (arrayBlockingQueue == null)
            return;
        //回收之前先检查channel是否可用，不可用的话，重新注册一个，添加至阻塞队列
        if (channel == null || !channel.isActive() || !channel.isOpen() || !channel.isWritable()){
            if (channel != null){
                channel.deregister().syncUninterruptibly().awaitUninterruptibly();
                channel.closeFuture().syncUninterruptibly().awaitUninterruptibly();
            }

            Channel newChannel = null;
            while (newChannel == null){
                LOGGER.debug("---------register new Channel ---------");
                newChannel = registerChannel(socketAddress);
            }

            arrayBlockingQueue.offer(newChannel);
            return;
        }
        arrayBlockingQueue.offer(channel);
    }

    /**
     * 为服务提供者地址socketAddress注册新的Channel
     */
    public Channel registerChannel(InetSocketAddress socketAddress){
        try {
            EventLoopGroup group = new NioEventLoopGroup(10);
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.remoteAddress(socketAddress);

            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY,true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            //注册编码器
                            socketChannel.pipeline().addLast(new NettyEncoderHandler(SERIALIZE_TYPE));
                            //注册解码器
                            socketChannel.pipeline().addLast(new NettyDecoderHandler(AresResponse.class,SERIALIZE_TYPE));
                            //注册客户端业务逻辑处理器
                            socketChannel.pipeline().addLast(new NettyClientInvokeHandler());
                        }
                    });
            ChannelFuture channelFuture = bootstrap.connect().sync();
            final Channel newChannel = channelFuture.channel();
            final CountDownLatch countDownLatch = new CountDownLatch(1);

            final List<Boolean> isSuccessHolder = Lists.newArrayListWithCapacity(1);
            //监听Channel是否建立成功
            channelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    //若Channel建立成功，保存建立成功的标记
                    if (future.isSuccess()){
                        isSuccessHolder.add(Boolean.TRUE);
                    }else {
                        //若Channel建立失败，保存建立失败的标记
                        future.cause().printStackTrace();
                        isSuccessHolder.add(Boolean.FALSE);
                    }
                    countDownLatch.countDown();
                }
            });

            countDownLatch.await();
            //如果Channel建立成功，返回新建的Channel
            if (isSuccessHolder.get(0)){
                return newChannel;
            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }
        return null;
    }

    public static NettyChannelPoolFactory channelPoolFactoryInstance(){
        return CHANNEL_POOL_FACTORY;
    }
}
