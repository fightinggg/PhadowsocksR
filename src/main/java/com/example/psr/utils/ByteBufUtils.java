package com.example.psr.utils;

import io.netty.buffer.ByteBuf;

public class ByteBufUtils {
    public static byte[] readAllAndReset(ByteBuf byteBuf) {
        byteBuf.markReaderIndex();
        byte[] bytes = new byte[byteBuf.readableBytes()];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = byteBuf.readByte();
        }
        byteBuf.resetReaderIndex();
        return bytes;
    }


}
