package com.example.psr.https;

import com.example.psr.client.HttpClient;
import com.example.psr.client.TcpClient;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpVersion;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.util.Base64;
import java.util.Map;
import java.util.function.BiConsumer;

@Slf4j
public class HttpsProxyServerHandler extends ChannelInboundHandlerAdapter {
    private TcpClient client;

    private HttpClient httpClient;
    String password;

    private String state = "init";

    Map<String, BiConsumer<ChannelHandlerContext, Object>> map = Map.ofEntries(
            Map.entry("init", (ctx, msgObj) -> {
                if (msgObj instanceof FullHttpRequest msg) {
                    String authorization = msg.headers().get("Proxy-Authorization");
                    String code = authorization == null ? null : new String(Base64.getDecoder().decode(authorization.split(" ")[1]));

                    msg.headers().remove("Proxy-Authorization");
                    msg.headers().remove("Proxy-Connection");


                    log.info("{} {} {}", code, msg.method().name(), msg.uri());
                    if (msg.method().equals(HttpMethod.CONNECT) && password.equals(code)) {


                        String host = msg.uri();

                        int port = 443;
                        if (host.contains(":")) {
                            String[] split = host.split(":");
                            host = split[0];
                            port = Integer.parseInt(split[1]);
                        }

                        client = new TcpClient(host, port, new SimpleChannelInboundHandler<>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext proxyCtx, Object proxyMsg) throws Exception {
//                                byte[] bytes = ByteBufUtils.readAllAndReset((ByteBuf) proxyMsg);
//                                log.debug("{}", new ToStringObject(() -> ByteBufVisiable.toString("server -> client ", bytes)));
                                ctx.writeAndFlush(Unpooled.copiedBuffer((ByteBuf) proxyMsg));
//                                ctx.writeAndFlush(proxyMsg);
                            }
                        });
                        ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK));
                        ctx.pipeline().remove(HttpServerCodec.class);
                        ctx.pipeline().remove(HttpObjectAggregator.class);
                        state = "proxy";
                    } else if (password.equals(code)) {
                        ChannelInboundHandlerAdapter handler = new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(final ChannelHandlerContext proxyCtx, final Object proxyMsg) {
                                if (proxyMsg instanceof FullHttpResponse) {
                                    ctx.writeAndFlush(proxyMsg);
                                }
                            }
                        };

                        URI uri = URI.create(msg.uri());
                        String host = uri.getHost();
                        int port = 80;
                        if (host.contains(":")) {
                            String[] split = host.split(":");
                            host = split[0];
                            port = Integer.parseInt(split[1]);
                        }
                        httpClient = new HttpClient(host, port, handler);
                        httpClient.writeAndFlush(msg);


                    } else {
                        ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN));
                        ctx.close();
                    }
                } else {
                    ctx.close();
                }

            }),
            Map.entry("proxy", (ctx, msg) -> {
                if (msg instanceof ByteBuf byteBuf) {
//                    byte[] bytes = ByteBufUtils.readAllAndReset(byteBuf);
//                    log.debug("{}", new ToStringObject(() -> ByteBufVisiable.toString("client -> server ", bytes)));
                    client.send(byteBuf);
//                    client.send(byteBuf);
                } else {
                    log.debug("error type={}", msg.getClass());
                }
            })
    );


    public HttpsProxyServerHandler(String password) {
        this.password = password;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msgObj) {
        map.get(state).accept(ctx, msgObj);
    }
}
