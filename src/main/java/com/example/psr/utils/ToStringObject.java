package com.example.psr.utils;

import lombok.AllArgsConstructor;

import java.util.function.Supplier;

@AllArgsConstructor
public class ToStringObject {
    private Supplier<String> supplier;

    @Override
    public String toString() {
        return supplier.get();
    }
}
