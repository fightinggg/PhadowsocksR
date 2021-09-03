package com.example.psr.http;

import com.example.psr.debug.ExecptionPrintHandler;
import com.example.psr.debug.InboundPrintHandler;
import com.example.psr.debug.OutboundPrintHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequest;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;

@Slf4j
public class HttpClient implements Closeable {
    EventLoopGroup group;
    ChannelFuture channelFuture;

    public HttpClient(String host, int port, ChannelInboundHandlerAdapter simpleChannelInboundHandler) {
        try {
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
                    socketChannel.pipeline().addLast(new HttpClientCodec());
                    socketChannel.pipeline().addLast(new HttpObjectAggregator(1024 * 1024));
                    socketChannel.pipeline().addLast(simpleChannelInboundHandler);
                    socketChannel.pipeline().addLast(new ExecptionPrintHandler());
                }
            });

            channelFuture = clientBootstrap.connect().sync();
        } catch (InterruptedException e) {
            log.error("", e);
        }
    }

    public void writeAndFlush(HttpRequest request) {
        channelFuture.channel().writeAndFlush(request);
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
