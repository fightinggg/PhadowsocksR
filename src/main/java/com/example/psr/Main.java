package com.example.psr;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.socksx.v5.DefaultSocks5InitialRequest;
import io.netty.handler.codec.socksx.v5.DefaultSocks5InitialResponse;
import io.netty.handler.codec.socksx.v5.Socks5AuthMethod;
import io.netty.handler.codec.socksx.v5.Socks5CommandRequest;
import io.netty.handler.codec.socksx.v5.Socks5CommandRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5InitialRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5ServerEncoder;

public class Main {
    public static void main(String[] args) throws Exception {
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
                            socketChannel.pipeline().addLast(new NoAuthenticationRequiredChannel());

//                            socketChannel.pipeline()
//                                    .addLast(Socks5ServerEncoder.DEFAULT)
//                                    .addLast(new Socks5InitialRequestDecoder())
//                                    .addLast(new SimpleChannelInboundHandler<DefaultSocks5InitialRequest>() {
//
//                                        @Override
//                                        protected void channelRead0(ChannelHandlerContext ctx, DefaultSocks5InitialRequest msg) throws Exception {
//                                            if (msg.authMethods().contains(Socks5AuthMethod.NO_AUTH)) {
//                                                ctx.writeAndFlush(new DefaultSocks5InitialResponse(Socks5AuthMethod.NO_AUTH));
//                                                ctx.pipeline().remove(Socks5InitialRequestDecoder.class);
//                                                ctx.pipeline().remove(this);
//                                            }
//                                        }
//                                    })
//                                    //  (  5)(  1) (  0)(  1) (220)(181) ( 38)(239) (191)(189) (  0)( 80)
//                                    .addLast(new Socks5CommandRequestDecoder())
//                                    .addLast(new SimpleChannelInboundHandler<Socks5CommandRequest>() {
//                                        @Override
//                                        protected void channelRead0(ChannelHandlerContext ctx, Socks5CommandRequest msg) throws Exception {
//                                            System.out.println(msg);
//                                        }
//                                    });

                        }
                    });
            ChannelFuture channelFuture = bootstrap.bind(1080).sync();
            System.out.println("psr start at 1080 ...");
            channelFuture.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

}

/*
 * curl -x http://localhost:1080 baidu.com
 * curl -x https://localhost:1080 baidu.com
 * curl -x socks5://localhost:1080 baidu.com
 */