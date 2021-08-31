package com.example.psr.utils;

import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ByteBufVisiable {
    public static String toReqString(ByteBuf byteBuf) {
        String msgString = HexUtils.toString(byteBuf.toString(StandardCharsets.UTF_8).getBytes(StandardCharsets.UTF_8), 2, 8);
        return Arrays.stream(msgString.split("\n")).map(">>>%s"::formatted).collect(Collectors.joining());
    }

    public static String toResString(ByteBuf byteBuf) {
        String msgString = HexUtils.toString(byteBuf.toString(StandardCharsets.UTF_8).getBytes(StandardCharsets.UTF_8), 2, 8);
        return Arrays.stream(msgString.split("\n")).map("<<<%s"::formatted).collect(Collectors.joining());
    }
}
