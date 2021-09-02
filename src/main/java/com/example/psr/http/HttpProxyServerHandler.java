package com.example.psr.http;

import com.example.psr.utils.ByteBufUtils;
import com.example.psr.utils.ByteBufVisiable;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;

@Slf4j
public class HttpProxyServerHandler extends SimpleChannelInboundHandler<HttpRequest> {
    HttpClient httpClient;


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpRequest msg) {
        if (msg.headers().contains("Proxy-Connection")) {
            msg.headers().remove("Proxy-Connection");
            URI uri = URI.create(msg.uri());
            msg.setUri("/");
            SimpleChannelInboundHandler<Object> handler = new SimpleChannelInboundHandler<>() {
                @Override
                protected void channelRead0(ChannelHandlerContext proxyCtx, Object proxyMsg) {
                    if (proxyMsg instanceof DefaultHttpResponse) {
                        ctx.writeAndFlush(proxyMsg);
                    } else if (proxyMsg instanceof DefaultHttpContent) {
                        ByteBuf content = ((DefaultHttpContent) proxyMsg).content();
                        log.info("{}", ByteBufVisiable.toString("", ByteBufUtils.readAllAndReset(content)));
                        log.info("{}", ByteBufVisiable.toString("", ByteBufUtils.readAllAndReset(content)));
                        ctx.writeAndFlush(content);
                    } else {
                        log.error("{}", proxyMsg);
                    }
                }


            };
            httpClient = new HttpClient(uri.getHost(), uri.getPort() == -1 ? 80 : uri.getPort(), handler);
            httpClient.writeAndFlush(msg);
        }
    }
}
