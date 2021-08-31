package com.example.psr;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.Closeable;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class TcpClient {

    public static ChannelFuture connect(String host, int port, SimpleChannelInboundHandler<?> simpleChannelInboundHandler) {
        try {
            Bootstrap clientBootstrap = new Bootstrap();
            EventLoopGroup group = new NioEventLoopGroup();
            clientBootstrap.group(group);
            clientBootstrap.channel(NioSocketChannel.class);
            clientBootstrap.remoteAddress(new InetSocketAddress(host, port));
            clientBootstrap.handler(new ChannelInitializer<SocketChannel>() {
                protected void initChannel(SocketChannel socketChannel) {
                    socketChannel.pipeline().addLast(simpleChannelInboundHandler);
                }
            });

            return clientBootstrap.connect().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
