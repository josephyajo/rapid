package org.sheep.rapid;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.net.InetSocketAddress;

/**
 * Created by Joseph on 2018/11/14.
 */
public class Server {
    private final int port;

    public Server(int port) {
        this.port = port;
    }

    public void start() throws Exception {
        //定义EventLoop
        EventLoopGroup masterGroup = new NioEventLoopGroup();
        EventLoopGroup slavesGroup = new NioEventLoopGroup();
        try {
            //与Bootstrap类包包含丰富的客户端API一样，ServerBootstrap能够非常方便的实现典型的服务端。
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(masterGroup, slavesGroup)
                    //指定所使用的NIO传输Channel
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 100)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    //使用指定的端口设置套接字地址
                    .localAddress(new InetSocketAddress(port))
                    //添加一个EchoServerHandler 到子Channel的ChannelPipeline
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel socketChannel){
                            socketChannel.pipeline().addLast(new ProtobufVarint32FrameDecoder());
                            socketChannel.pipeline().addLast(new ProtobufDecoder(RichManProto.RichMan.getDefaultInstance()));
                            socketChannel.pipeline().addLast(new ProtoBufServerHandler());
                        }
                    });
            //新建一个future实例,异步地绑定服务器；调用sync()方法阻塞等待直到绑定完成
            ChannelFuture channelFuture = serverBootstrap.bind().sync();
            //获取Channel 的CloseFuture，并且阻塞当前线程直到它完成
            //该应用程序将会阻塞等待直到服务器的Channel关闭（因为你在Channel 的CloseFuture 上调用了sync()方法）。
            channelFuture.channel().closeFuture().sync();
        } finally {
            //关闭EventLoopGroup，释放所有的资源
            masterGroup.shutdownGracefully();
            slavesGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        //设置端口值（如果端口参数的格式不正确，则抛出一个NumberFormatException）
        int port = 8081;
        new Server(port).start();
    }
}
