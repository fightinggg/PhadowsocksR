package com.example.psr.debug;

import com.example.psr.utils.ByteBufUtils;
import com.example.psr.utils.ByteBufVisiable;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InboundPrintHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (log.isDebugEnabled()) {
            ByteBuf byteBuf = (ByteBuf) msg;
            byte[] bytes = ByteBufUtils.readAllAndReset(byteBuf);
            log.debug("{}", ByteBufVisiable.toString(">> ", bytes));
            // 下游继续读取
            ctx.fireChannelRead(Unpooled.copiedBuffer(bytes));
        }
    }

}
