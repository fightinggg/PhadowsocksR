package com.example.psr.https;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpVersion;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpsProxyServerHandler extends ChannelInboundHandlerAdapter {
    HttpsClient httpClient;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msgObj) {
        if (msgObj instanceof FullHttpRequest msg) {
            if (msg.method().equals(HttpMethod.CONNECT)) {
                String host = msg.uri();
                int port = 443;
                if (host.contains(":")) {
                    String[] split = host.split(":");
                    host = split[0];
                    port = Integer.parseInt(split[1]);
                }

                ChannelInboundHandlerAdapter handler = new ChannelInboundHandlerAdapter() {

                    @Override
                    public void channelRead(ChannelHandlerContext proxyCtx, Object proxyMsg) {
                        ctx.writeAndFlush(proxyMsg);
                    }
                };
                httpClient = new HttpsClient(host, port, handler);
                ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK));
                ctx.pipeline().remove(HttpServerCodec.class);
                ctx.pipeline().remove(HttpObjectAggregator.class);
            }
        } else if (msgObj instanceof ByteBuf byteBuf) {
            httpClient.writeAndFlush(byteBuf);
        }
    }
}
