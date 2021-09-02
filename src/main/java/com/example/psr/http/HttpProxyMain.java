package com.example.psr.http;

import com.example.psr.debug.ExecptionPrintHandler;
import com.example.psr.debug.InboundPrintHandler;
import com.example.psr.debug.OutboundPrintHandler;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

@Slf4j
public class HttpProxyMain {
    public static void main(String[] args) throws InterruptedException {
        CommandLineParser parser = new GnuParser();

        Options options = new Options();
        options.addOption("p", "port", true, "port");

        CommandLine commandLine = null;
        try {
            commandLine = parser.parse(options, args);
        } catch (ParseException e) {
            String info = """
                    could not parse your command line, please use
                    java -jar psr.jar -port 1080
                    java -jar psr.jar
                    """;
            log.info(info);
            System.exit(-1);
        }
        int port = Integer.parseInt(commandLine.getOptionValue("port", "1080"));

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
                            socketChannel.pipeline().addLast(new InboundPrintHandler());
                            socketChannel.pipeline().addLast(new OutboundPrintHandler());
                            socketChannel.pipeline().addLast(new HttpServerCodec());
                            socketChannel.pipeline().addLast(new HttpProxyServerHandler());
                            socketChannel.pipeline().addLast(new ExecptionPrintHandler());
                        }
                    });
            ChannelFuture channelFuture = bootstrap.bind(port).sync();
            log.info("psr start at {} ...", port);
            channelFuture.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
