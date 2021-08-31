package com.example.psr.utils;

public class HexUtils {
    public static String toString(byte[] bytes, int blockSize, int groupSize) {

        StringBuilder res = new StringBuilder();
        for (int i = 0; i < bytes.length; i += blockSize * groupSize) {
            StringBuilder hex = new StringBuilder();
            StringBuilder intView = new StringBuilder();
            StringBuilder view = new StringBuilder();
            for (int j = i; j < bytes.length && j - i < blockSize * groupSize; j += blockSize) {
                for (int k = j; k < bytes.length && k - j < blockSize; k++) {
                    String hexString = Integer.toHexString(bytes[k] & 0xff);
                    String intString = "(%3d)".formatted(bytes[k] & 0xff);
                    hex.append(hexString.length() == 2 ? hexString : (0 + hexString));
                    intView.append(intString);
                    view.append(bytes[k] >= 32 && bytes[k] <= 126 ? ((char) bytes[k]) : '.');
                }
                hex.append(" ");
                intView.append(" ");
            }
            while (hex.length() < blockSize * groupSize * 2 + groupSize) {
                hex.append(" ");
            }
            while (intView.length() < blockSize * groupSize * 5 + groupSize) {
                intView.append(" ");
            }
            res.append(hex).append("  ").append(intView).append(" ").append(view).append("\n");
        }
        return res.toString();
    }
}
