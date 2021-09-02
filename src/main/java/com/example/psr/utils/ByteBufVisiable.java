package com.example.psr.utils;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ByteBufVisiable {
    public static String toString(String prefix, byte[] bytes) {
        String textString = TextUtils.toString(bytes);
        String hexString = HexUtils.toString(bytes, 2, 8);
        return Arrays.stream("%s\n%s".formatted(textString, hexString).split("\n"))
                .map(o -> prefix + o)
                .collect(Collectors.joining("\n")) + "\n";
    }
}
