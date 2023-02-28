package com.example.psr.socks5;

import com.example.psr.client.TcpClient;
import com.example.psr.utils.ByteBufUtils;
import com.example.psr.utils.ByteBufVisiable;
import com.example.psr.utils.ToStringObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.function.BiConsumer;

@Slf4j
public class NoAuthenticationRequiredChannel extends ChannelInboundHandlerAdapter {

    private String state = "init";
    private TcpClient client;

    Map<String, BiConsumer<ChannelHandlerContext, ByteBuf>> map = Map.ofEntries(
            Map.entry("init", (ctx, msg) -> {

                /*
                 *  The client connects to the server, and sends a version
                 *    identifier/method selection message:
                 *
                 *                    +----+----------+----------+
                 *                    |VER | NMETHODS | METHODS  |
                 *                    +----+----------+----------+
                 *                    | 1  |    1     | 1 to 255 |
                 *                    +----+----------+----------+
                 *
                 *    The VER field is set to X'05' for this version of the protocol.  The
                 *    NMETHODS field contains the number of method identifier octets that
                 *    appear in the METHODS field.
                 */
                byte[] bytes = ByteBufUtils.readAllAndReset(msg);
                log.debug("{}", new ToStringObject(() -> ByteBufVisiable.toString("client >> ", bytes)));

                /*
                 *  The server selects from one of the methods given in METHODS, and
                 *    sends a METHOD selection message:
                 *
                 *                          +----+--------+
                 *                          |VER | METHOD |
                 *                          +----+--------+
                 *                          | 1  |   1    |
                 *                          +----+--------+
                 *
                 *    If the selected METHOD is X'FF', none of the methods listed by the
                 *    client are acceptable, and the client MUST close the connection.
                 *
                 *    The values currently defined for METHOD are:
                 *
                 *           o  X'00' NO AUTHENTICATION REQUIRED
                 *           o  X'01' GSSAPI
                 *           o  X'02' USERNAME/PASSWORD
                 *           o  X'03' to X'7F' IANA ASSIGNED
                 *           o  X'80' to X'FE' RESERVED FOR PRIVATE METHODS
                 *           o  X'FF' NO ACCEPTABLE METHODS
                 */
                byte[] resBytes = new byte[]{5, 0};
                log.debug("{}", new ToStringObject(() -> ByteBufVisiable.toString("client << ", resBytes)));
                ctx.writeAndFlush(Unpooled.copiedBuffer(resBytes));

                state = "command";
            }),
            Map.entry("command", (ctx, msg) -> {

                /*
                 *  The SOCKS request is formed as follows:
                 *
                 *         +----+-----+-------+------+----------+----------+
                 *         |VER | CMD |  RSV  | ATYP | DST.ADDR | DST.PORT |
                 *         +----+-----+-------+------+----------+----------+
                 *         | 1  |  1  | X'00' |  1   | Variable |    2     |
                 *         +----+-----+-------+------+----------+----------+
                 *
                 *      Where:
                 *
                 *           o  VER    protocol version: X'05'
                 *           o  CMD
                 *              o  CONNECT X'01'
                 *              o  BIND X'02'
                 *              o  UDP ASSOCIATE X'03'
                 *           o  RSV    RESERVED
                 *           o  ATYP   address type of following address
                 *              o  IP V4 address: X'01'
                 *              o  DOMAINNAME: X'03'
                 *              o  IP V6 address: X'04'
                 *           o  DST.ADDR       desired destination address
                 *           o  DST.PORT desired destination port in network octet
                 *              order
                 *
                 *    In an address field (DST.ADDR, BND.ADDR), the ATYP field specifies
                 *    the type of address contained within the field:
                 *
                 *           o  X'01'
                 *
                 *    the address is a version-4 IP address, with a length of 4 octets
                 *
                 *           o  X'03'
                 *
                 *    the address field contains a fully-qualified domain name.  The first
                 *    octet of the address field contains the number of octets of name that
                 *    follow, there is no terminating NUL octet.
                 *
                 *           o  X'04'
                 *
                 */
                try {
                    byte[] bytes = ByteBufUtils.readAllAndReset(msg);
                    log.debug("{}", new ToStringObject(() -> ByteBufVisiable.toString("client >> ", bytes)));
                    StringBuilder host = new StringBuilder();
                    int port = 80;
                    if (bytes[3] == 1) {
                        host = new StringBuilder("%d.%d.%d.%d".formatted(bytes[4] & 0xff, bytes[5] & 0xff, bytes[6] & 0xff, bytes[7] & 0xff));
                        port = ((bytes[bytes.length - 2] & 0xff) << 8) | (bytes[bytes.length - 1] & 0xff);
                    } else if (bytes[3] == 3) {
                        for (int i = 0; i < bytes[4]; i++) {
                            host.append((char) (bytes[5 + i]));
                        }
                        port = ((bytes[bytes.length - 2] & 0xff) << 8) | (bytes[bytes.length - 1] & 0xff);
                    } else {
                        throw new RuntimeException("error");
                    }
                    log.debug("proxy to " + host + ":" + port);
                    client = new TcpClient(host.toString(), port, new SimpleChannelInboundHandler<>() {
                        @Override
                        protected void channelRead0(ChannelHandlerContext proxyCtx, Object proxyMsg) throws Exception {
                            byte[] bytes = ByteBufUtils.readAllAndReset((ByteBuf) proxyMsg);
                            log.debug("{}", new ToStringObject(() -> ByteBufVisiable.toString("server -> client ", bytes)));
                            ctx.writeAndFlush(Unpooled.copiedBuffer(bytes));
                        }
                    });
                }catch (Exception e){
                    /*
                     * The SOCKS request information is sent by the client as soon as it has
                     *    established a connection to the SOCKS server, and completed the
                     *    authentication negotiations.  The server evaluates the request, and
                     *    returns a reply formed as follows:
                     *
                     *         +----+-----+-------+------+----------+----------+
                     *         |VER | REP |  RSV  | ATYP | BND.ADDR | BND.PORT |
                     *         +----+-----+-------+------+----------+----------+
                     *         | 1  |  1  | X'00' |  1   | Variable |    2     |
                     *         +----+-----+-------+------+----------+----------+
                     *
                     *      Where:
                     *
                     *           o  VER    protocol version: X'05'
                     *           o  REP    Reply field:
                     *              o  X'00' succeeded
                     *              o  X'01' general SOCKS server failure
                     *              o  X'02' connection not allowed by ruleset
                     *              o  X'03' Network unreachable
                     *              o  X'04' Host unreachable
                     *              o  X'05' Connection refused
                     *              o  X'06' TTL expired
                     *              o  X'07' Command not supported
                     *              o  X'08' Address type not supported
                     *              o  X'09' to X'FF' unassigned
                     *           o  RSV    RESERVED
                     *           o  ATYP   address type of following address
                     *              o  IP V4 address: X'01'
                     *              o  DOMAINNAME: X'03'
                     *              o  IP V6 address: X'04'
                     *           o  BND.ADDR       server bound address
                     *           o  BND.PORT       server bound port in network octet order
                     *
                     *    Fields marked RESERVED (RSV) must be set to X'00'.
                     *
                     *    If the chosen method includes encapsulation for purposes of
                     *    authentication, integrity and/or confidentiality, the replies are
                     *    encapsulated in the method-dependent encapsulation.
                     *
                     */
                    byte[] resBytes = new byte[]{5, 3, 0, 1, (byte) 192, (byte) 168, 1, 1, 0, 80};
                    log.debug("{}", new ToStringObject(() -> ByteBufVisiable.toString("client << ", resBytes)));
                    ctx.writeAndFlush(Unpooled.copiedBuffer(resBytes));
                    ctx.close();
                    return;
                }

                byte[] resBytes = new byte[]{5, 0, 0, 1, (byte) 192, (byte) 168, 1, 1, 0, 80};
                log.debug("{}", new ToStringObject(() -> ByteBufVisiable.toString("client << ", resBytes)));
                ctx.writeAndFlush(Unpooled.copiedBuffer(resBytes));

                state = "proxy";



            }),
            Map.entry("proxy", (ctx, msg) -> {
                byte[] bytes = ByteBufUtils.readAllAndReset(msg);
                log.debug("{}", new ToStringObject(() -> ByteBufVisiable.toString( "client -> server ", bytes)));
                client.send(Unpooled.copiedBuffer(bytes));
            })
    );


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        map.get(state).accept(ctx, (ByteBuf) msg);
    }


    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        log.debug("complete");
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        cause.printStackTrace();
        if (client != null) {
            client.close();
        }
    }
}
