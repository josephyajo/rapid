package org.sheep.rapid;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

/**
 * Created by Joseph on 2018/11/14.
 */
@ChannelHandler.Sharable
public class ClientHandler extends
        SimpleChannelInboundHandler<ByteBuf> {
    //重写了channelActive()方法，其将在一个连接建立时被调用
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.writeAndFlush(Unpooled.copiedBuffer("Netty rocks!",
                CharsetUtil.UTF_8));
    }

    //重写了channelRead0()方法。每当接收数据时，都会调用这个方法。
    //需要注意的是，由服务器发送的消息可能会被分块接收。
    // 也就是说，如果服务器发送了5 字节，那么不能保证这5 字节会被一次性接收。
    //即使是对于这么少量的数据，channelRead0()方法也可能
    // 会被调用两次，第一次使用一个持有3 字节的ByteBuf（Netty 的字节容器）
    // 第二次使用一个持有2 字节的ByteBuf。
    @Override
    public void channelRead0(ChannelHandlerContext ctx, ByteBuf in) {
        System.out.println(
                "Client received: " + in.toString(CharsetUtil.UTF_8));
    }

    //发生异常时被调用
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx,
                                Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
