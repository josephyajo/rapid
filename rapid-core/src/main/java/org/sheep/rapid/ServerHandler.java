package org.sheep.rapid;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

/**
 * Created by Joseph on 2018/11/14.
 */
//标示一个ChannelHandler 可以被多个Channel 安全地共享
@ChannelHandler.Sharable
public class ServerHandler extends ChannelInboundHandlerAdapter {
    //对于每个传入的消息都会被调用；
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        //将消息记录到控制台
        ByteBuf in = (ByteBuf) msg;
        System.out.println(
                "Server received: " + in.toString(CharsetUtil.UTF_8));
        ctx.write(in);
    }

    //通知ChannelInboundHandler最后一次对channelRead()
    //的调用是当前批量读取中的最后一条消息；
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER)
                .addListener(ChannelFutureListener.CLOSE);
    }

    //在读取操作期间，有异常抛出时会调用。
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx,
                                Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
