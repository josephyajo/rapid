package org.sheep.rapid;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

import java.net.InetSocketAddress;

/**
 * Created by Joseph on 2018/11/14.
 */
public class Client {
    private final String host;
    private final int port;

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() throws Exception {
        //定义EventLoop
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            //Bootstrap类包提供包含丰富API的帮助类，能够非常方便的实现典型的服务器端和客户端通道初始化功能。
            Bootstrap bootstrap = new Bootstrap();
            //绑定EventLoop
            bootstrap.group(group)
                    //使用默认的channelFactory创建一个channel
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    //定义远程地址
                    .remoteAddress(new InetSocketAddress(host, port))
                    //绑定自定义的EchoClientHandler到ChannelPipeline上
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
                            socketChannel.pipeline().addLast(new ProtobufEncoder());
                            socketChannel.pipeline().addLast(new ProtoBufClientHandler());
                        }
                    });
            //同步式的链接
            ChannelFuture channelFuture = bootstrap.connect().sync();
            channelFuture.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        new Client("localhost", 8081).start();
    }
}
