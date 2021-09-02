package com.example.psr.utils;

public class TextUtils {
    private static boolean visiableByte(byte c) {
        return (c >= 32 && c <= 126) || c == '\n' || c == '\r' || c == '\t';
    }

    public static String toString(byte[] bytes) {
        StringBuilder res = new StringBuilder();
        for (byte aByte : bytes) {
            res.append(visiableByte(aByte) ? ((char) aByte) : '.');
        }
        return res.toString();
    }
}
