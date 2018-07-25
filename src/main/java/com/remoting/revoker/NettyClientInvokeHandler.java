package com.remoting.revoker;

import com.remoting.model.AresResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class NettyClientInvokeHandler extends SimpleChannelInboundHandler<AresResponse> {

    public NettyClientInvokeHandler(){
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, AresResponse aresResponse) throws Exception {
        //将异步调用的返回结果存储阻塞队列，以便调用端同步获取
        RevokerResponseHolder.putResultValue(aresResponse);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
