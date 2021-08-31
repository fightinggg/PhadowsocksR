package com.example.psr.utils;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ByteBufVisiable {
    public static String toString(String prefix, byte[] bytes) {
        String msgString = HexUtils.toString(bytes, 2, 8);
        return Arrays.stream(msgString.split("\n")).map(o -> prefix + o).collect(Collectors.joining("\n"));
    }

}
