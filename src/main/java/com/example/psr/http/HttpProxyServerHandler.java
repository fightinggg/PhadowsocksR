package com.example.psr.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;

@Slf4j
public class HttpProxyServerHandler extends ChannelInboundHandlerAdapter {
    HttpClient httpClient;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msgObj) {
        if (msgObj instanceof FullHttpRequest msg) {
            if (msg.headers().contains("Proxy-Connection")) {
                msg.headers().remove("Proxy-Connection");
                URI uri = URI.create(msg.uri());
                msg.setUri("/");
                ChannelInboundHandlerAdapter handler = new ChannelInboundHandlerAdapter() {
                    @Override
                    public void channelRead(final ChannelHandlerContext proxyCtx, final Object proxyMsg) {
                        if (proxyMsg instanceof FullHttpResponse) {
                            ctx.writeAndFlush(proxyMsg);
                        }
                    }
                };
                httpClient = new HttpClient(uri.getHost(), uri.getPort() == -1 ? 80 : uri.getPort(), handler);
                httpClient.writeAndFlush(msg);
            }
        }
    }
}
