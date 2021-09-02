package com.example.psr.debug;

import com.example.psr.utils.ByteBufUtils;
import com.example.psr.utils.ByteBufVisiable;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OutboundPrintHandler extends ChannelOutboundHandlerAdapter {
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (log.isDebugEnabled()) {
            ByteBuf byteBuf = (ByteBuf) msg;
            byte[] bytes = ByteBufUtils.readAllAndReset(byteBuf);
            log.debug("{}", ByteBufVisiable.toString("<< ", bytes));
            // 下游继续写入
            ctx.writeAndFlush(msg, promise);
        }
    }
}
