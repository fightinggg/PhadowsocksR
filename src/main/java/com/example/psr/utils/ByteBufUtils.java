package com.example.psr.utils;

import io.netty.buffer.ByteBuf;

public class ByteBufUtils {
    public static byte[] readAll(ByteBuf byteBuf) {
        byte[] bytes = new byte[byteBuf.readableBytes()];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = byteBuf.readByte();
        }
        return bytes;
    }


}
