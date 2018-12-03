package org.igye.outline.htmlforms;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CyclicRandom {
    private int hash;
    private int elemsCnt;
    private List<Integer> rest;
    private Random rnd = new Random();

    public Integer getRandomIndex(int hash, int elemsCnt) {
        if (this.hash != hash || this.elemsCnt != elemsCnt || rest.isEmpty()) {
            reset(hash, elemsCnt);
        }
        return rest.remove(rnd.nextInt(rest.size()));
    }

    public String getCounts() {
        return (elemsCnt - rest.size()) + "/" + elemsCnt;
    }

    private void reset(int hash, int elemsCnt) {
        this.hash = hash;
        this.elemsCnt = elemsCnt;
        rest = Stream.iterate(0, i->i+1).limit(elemsCnt).collect(Collectors.toList());
    }
}
