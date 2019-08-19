package org.igye.outline2.controllers;

import org.igye.outline2.OutlineUtils;
import org.igye.outline2.common.Randoms;
import org.junit.Test;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RandomsTest {
    @Test
    public void integer_should_return_all_numbers_from_the_range_specified() {
        //given
        int from = 10;
        int to = 18;

        //when
        Map<Integer, Integer> distribution = Stream.iterate(1, i -> i).limit(1000)
                .map(i -> Randoms.integer(from, to))
                .collect(Collectors.toMap(i -> i, i -> 1, (l, r) -> l + r));

        //then
        assertEquals(OutlineUtils.setOf(10,11,12,13,14,15,16,17,18), distribution.keySet());
        distribution.forEach((k,v)-> assertTrue(70 < v));
    }

    @Test
    public void lonng_should_return_all_numbers_from_the_range_specified() {
        //given
        long from = 10;
        long to = 18;

        //when
        Map<Long, Long> distribution = Stream.iterate(1L, i -> i).limit(1000)
                .map(i -> Randoms.lonng(from, to))
                .collect(Collectors.toMap(i -> i, i -> 1L, (l, r) -> l + r));

        //then
        assertEquals(OutlineUtils.setOf(10L,11L,12L,13L,14L,15L,16L,17L,18L), distribution.keySet());
        distribution.forEach((k,v)-> assertTrue(70 < v));
    }

}