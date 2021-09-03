package com.example.psr.http;

import com.example.psr.https.HttpsClient;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpProxyServerHandler extends ChannelInboundHandlerAdapter {
    HttpClient httpClient;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msgObj) {
        if (msgObj instanceof FullHttpRequest msg) {
            if (msg.headers().contains("Proxy-Connection")) {
                msg.headers().remove("Proxy-Connection");
                String host = msg.uri();
                int port = 80;
                if (host.contains(":")) {
                    String[] split = host.split(":");
                    host = split[0];
                    port = Integer.parseInt(split[1]);
                }

                msg.setUri("/");
                ChannelInboundHandlerAdapter handler = new ChannelInboundHandlerAdapter() {
                    @Override
                    public void channelRead(final ChannelHandlerContext proxyCtx, final Object proxyMsg) {
                        if (proxyMsg instanceof FullHttpResponse) {
                            ctx.writeAndFlush(proxyMsg);
                        }
                    }
                };
                httpClient = new HttpClient(host, port, handler);
                httpClient.writeAndFlush(msg);
            }
        }
    }
}
