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

public class TcpClient implements Closeable {
    private final ChannelFuture channelFuture;
    private volatile ChannelHandlerContext ctx;

    TcpClient(SimpleChannelInboundHandler simpleChannelInboundHandler) {
        Bootstrap clientBootstrap = new Bootstrap();
        EventLoopGroup group = new NioEventLoopGroup();
        clientBootstrap.group(group);
        clientBootstrap.channel(NioSocketChannel.class);
        clientBootstrap.remoteAddress(new InetSocketAddress(80));
        clientBootstrap.handler(new ChannelInitializer<SocketChannel>() {
            protected void initChannel(SocketChannel socketChannel) {
                socketChannel.pipeline().addLast(simpleChannelInboundHandler);
                socketChannel.pipeline().addLast(new SimpleChannelInboundHandler() {
                    @Override
                    public void channelActive(ChannelHandlerContext ctx2) throws Exception {
                        super.channelActive(ctx2);
                        ctx = ctx2;
                    }

                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {

                    }
                });
            }
        });
        channelFuture = clientBootstrap.connect();
        new Thread(()-> {
            try {
                channelFuture.sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    public void send(ByteBuf byteBuf) {
        while (ctx == null) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        ctx.writeAndFlush(byteBuf);
    }

    @Override
    public void close() {
        try {
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
