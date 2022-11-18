package com.example.psr.https;

import com.example.psr.debug.ExecptionPrintHandler;
import com.example.psr.debug.InboundPrintHandler;
import com.example.psr.debug.OutboundPrintHandler;
import com.example.psr.https.ssl.SecureSocketSslContextFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslHandler;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLEngine;

@Slf4j
public class HttpsProxyLauncher {
    public static void run(int port,String password) {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) {
                            log.info("connect start at {}", socketChannel.remoteAddress());
                            socketChannel.pipeline().addLast(new ExecptionPrintHandler());
//                            socketChannel.pipeline().addLast(new InboundPrintHandler());
//                            socketChannel.pipeline().addLast(new OutboundPrintHandler());
                            SSLEngine sslEngine = SecureSocketSslContextFactory.getServerContext().createSSLEngine();
                            sslEngine.setUseClientMode(false);
                            socketChannel.pipeline().addLast(new SslHandler(sslEngine));

                            socketChannel.pipeline().addLast(new InboundPrintHandler());
                            socketChannel.pipeline().addLast(new OutboundPrintHandler());

                            socketChannel.pipeline().addLast(new HttpServerCodec());
                            socketChannel.pipeline().addLast(new HttpObjectAggregator(1024 * 1024));
                            socketChannel.pipeline().addLast(new HttpsProxyServerHandler(password));
                            socketChannel.pipeline().addLast(new ExecptionPrintHandler());
                        }
                    });
            ChannelFuture channelFuture = bootstrap.bind(port).syncUninterruptibly();
            log.info("psr using https proxy,  start at {} ...", port);
            channelFuture.channel().closeFuture().syncUninterruptibly();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
