package com.example.psr.https;

import com.example.psr.debug.ExecptionPrintHandler;
import com.example.psr.debug.InboundPrintHandler;
import com.example.psr.debug.OutboundPrintHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.net.InetSocketAddress;

@Slf4j
public class HttpsClient implements Closeable {
    EventLoopGroup group;
    ChannelFuture channelFuture;

    public HttpsClient(String host, int port, ChannelInboundHandlerAdapter channelInboundHandlerAdapter) {
        log.info("connect to {}:{}", host, port);
        Bootstrap clientBootstrap = new Bootstrap();
        group = new NioEventLoopGroup();
        clientBootstrap.group(group);
        clientBootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 60000);
        clientBootstrap.channel(NioSocketChannel.class);
        clientBootstrap.remoteAddress(new InetSocketAddress(host, port));
        clientBootstrap.handler(new ChannelInitializer<SocketChannel>() {
            protected void initChannel(SocketChannel socketChannel) {
                socketChannel.pipeline().addLast(new ExecptionPrintHandler());
                socketChannel.pipeline().addLast(new InboundPrintHandler());
                socketChannel.pipeline().addLast(new OutboundPrintHandler());
                socketChannel.pipeline().addLast(channelInboundHandlerAdapter);
                socketChannel.pipeline().addLast(new ExecptionPrintHandler());
            }
        });

        channelFuture = clientBootstrap.connect().syncUninterruptibly();
    }

    public void writeAndFlush(ByteBuf byteBuf) {
        channelFuture.channel().writeAndFlush(byteBuf);
    }

    @Override
    public void close() {
        channelFuture.channel().closeFuture().syncUninterruptibly();
        group.shutdownGracefully();
    }

}
