package com.receiptofi.utils;

import java.util.Random;

/**
 * Auth keys generator
 *
 * User: hitender
 * Date: 4/15/13
 * Time: 2:02 AM
 * http://stackoverflow.com/questions/41107/how-to-generate-a-random-alpha-numeric-string-in-java
 *
 * You can tweak the "symbols" if you want to use more characters.
 */
public final class RandomString {

    private static int CHAR_SIZE = 32;
    private static final char[] symbols = new char[36];

    static {
        for (int idx = 0; idx < 10; ++idx)
            symbols[idx] = (char) ('0' + idx);
        for (int idx = 10; idx < 36; ++idx)
            symbols[idx] = (char) ('a' + idx - 10);
    }

    private final Random random = new Random();
    private final char[] buf;

    private RandomString(int length) {
        if (length < 1)  {
            throw new IllegalArgumentException("length < 1: " + length);
        }
        buf = new char[length];
    }

    public static RandomString newInstance() {
        return new RandomString(CHAR_SIZE);
    }

    public static RandomString newInstance(int sizeOfString) {
        return new RandomString(sizeOfString);
    }

    public String nextString() {
        for (int idx = 0; idx < buf.length; ++idx) {
            buf[idx] = symbols[random.nextInt(symbols.length)];
        }
        return new String(buf);
    }
}
