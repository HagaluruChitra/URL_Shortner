package com.chitra.urlshortener.service;

public final class Base62 {
    private static final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int RADIX = ALPHABET.length();

    private Base62() { }

    public static String encode(long value) {
        if (value < 0) throw new IllegalArgumentException("Value must be non-negative");
        if (value == 0) return "0";
        StringBuilder result = new StringBuilder();
        while (value > 0) {
            result.append(ALPHABET.charAt((int) (value % RADIX)));
            value /= RADIX;
        }
        return result.reverse().toString();
    }

    public static long decode(String encoded) {
        if (encoded == null || encoded.isBlank()) throw new IllegalArgumentException("Code must not be blank");
        long result = 0;
        for (char character : encoded.toCharArray()) {
            int digit = ALPHABET.indexOf(character);
            if (digit < 0) throw new IllegalArgumentException("Invalid Base62 character");
            result = Math.addExact(Math.multiplyExact(result, RADIX), digit);
        }
        return result;
    }
}