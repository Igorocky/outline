package org.igye.outline2.controllers;

import org.igye.outline2.pm.Image;
import org.igye.outline2.pm.Node;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Randoms {
    private static Random rnd = new Random();
    private static String[] symbols = new String[]{"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q",
            "R","S","T","U","V","W","X","Y","Z","a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q",
            "r","s","t","u","v","w","x","y","z","0","1","2","3","4","5","6","7","8","9"};

    public static int integer(int from, int to) {
        return from + rnd.nextInt(to - from + 1);
    }

    public static long lonng(long from, long to) {
        return from + Math.abs(rnd.nextLong()) % (to - from + 1);
    }

    public static <T> T element(List<T> list) {
        return list.get(integer(0, list.size()-1));
    }

    public static <T> T element(T[] arr) {
        return arr[integer(0, arr.length-1)];
    }

    public static String string(int lengthFrom, int lengthTo) {
        StringBuilder sb = new StringBuilder();
        int len = integer(lengthFrom, lengthTo);
        for (int i = 0; i < len; i++) {
            sb.append(symbols[integer(0, symbols.length-1)]);
        }
        return sb.toString();
    }

    public static Instant instant(Instant from, Instant to) {
        return Instant.ofEpochSecond(lonng(from.getEpochSecond(), to.getEpochSecond()));
    }

    public static Instant instant() {
        return instant(Instant.now().minus(365, ChronoUnit.DAYS), Instant.now());
    }

    public static <T> List<T> list(int length, Supplier<T> supplier) {
        return list(length, length, supplier);
    }

    public static <T> List<T> list(int lengthFrom, int lengthTo, Supplier<T> supplier) {
        int len = integer(lengthFrom, lengthTo);
        List<T> list = new ArrayList<>();
        for (int i = 0; i < len; i++) {
            list.add(supplier.get());
        }
        return list;
    }

    public static <T> T either(int prob, T left, T right) {
        return integer(1,100) <= prob ? left : right;
    }

    public static Consumer<Node> randomNode(List<Image> images) {
        return node -> {
            node.setName(either(10, null, string(70,120)));
            node.setIcon(either(10, null, element(images)));
            node.setCreatedWhen(instant());
        };
    }

    public static Image image() {
        Image image = new Image();
        image.setId(UUID.randomUUID());
        image.setCreatedWhen(instant());
        return image;
    }
}
