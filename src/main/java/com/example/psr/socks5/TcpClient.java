package com.example.psr.socks5;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;

@Slf4j
public class TcpClient implements Closeable {
    EventLoopGroup group;
    ChannelFuture channelFuture;

    public TcpClient(String host, int port, SimpleChannelInboundHandler<?> simpleChannelInboundHandler) {
        try {
            Bootstrap clientBootstrap = new Bootstrap();
            group = new NioEventLoopGroup();
            clientBootstrap.group(group);
            clientBootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 60000);
            clientBootstrap.channel(NioSocketChannel.class);
            clientBootstrap.remoteAddress(new InetSocketAddress(host, port));
            clientBootstrap.handler(new ChannelInitializer<SocketChannel>() {
                protected void initChannel(SocketChannel socketChannel) {
                    socketChannel.pipeline().addLast(simpleChannelInboundHandler);
                }
            });

            channelFuture = clientBootstrap.connect().sync();
        } catch (InterruptedException e) {
            log.error("", e);
        }
    }

    public void send(ByteBuf byteBuf) {
        channelFuture.channel().writeAndFlush(byteBuf);
    }

    @Override
    public void close() throws IOException {
        try {
            channelFuture.channel().closeFuture().sync();
            group.shutdownGracefully();
        } catch (InterruptedException e) {
            log.error("", e);
        }
    }

}
