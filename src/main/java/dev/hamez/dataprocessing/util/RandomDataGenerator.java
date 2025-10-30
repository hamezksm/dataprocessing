package dev.hamez.dataprocessing.util;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;

public class RandomDataGenerator {

    private static final SecureRandom RAND = new SecureRandom();
    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";

    public static String randomAlphaString(int minLen, int maxLen) {
        int len = RAND.nextInt(maxLen - minLen + 1) + minLen;
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(ALPHABET.charAt(RAND.nextInt(ALPHABET.length())));
        }
        // capitalize first letter
        if (sb.length() > 0) {
            sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        }
        return sb.toString();
    }

    public static LocalDate randomDateBetween(LocalDate startInclusive, LocalDate endInclusive) {
        long startEpochDay = startInclusive.toEpochDay();
        long endEpochDay = endInclusive.toEpochDay();
        long randomDay = ThreadLocalRandom.current().longs(startEpochDay, endEpochDay + 1).findFirst().getAsLong();
        return LocalDate.ofEpochDay(randomDay);
    }

    public static int randomInt(int minInclusive, int maxInclusive) {
        return RAND.nextInt(maxInclusive - minInclusive + 1) + minInclusive;
    }
}
