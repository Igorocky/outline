package org.igye.outline.htmlforms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.igye.outline.common.OutlineUtils.listF;
import static org.igye.outline.common.OutlineUtils.map;

public class LearnTextData {
    private int hash;
    private int elemsCnt;
    private List<Integer> lastCounts = new ArrayList<>();
    private Random rnd = new Random();

    public int[] getCountsStat(int elemsCnt, int hash) {
        int min = Integer.MAX_VALUE;
        int max = 0;
        if (elemsCnt > 0) {
            resetCountsIfNecessary(elemsCnt, hash);
            for (Integer cnt : lastCounts) {
                if (cnt < min) {
                    min = cnt;
                }
                if (cnt > max) {
                    max = cnt;
                }
            }
        } else {
            min = 0;
            max = 0;
        }
        return new int[] {min, max};
    }

    public Set<Integer> getIndicesToHide(int elemsCnt, int pct, int hash) {
        resetCountsIfNecessary(elemsCnt, hash);
        Set<Integer> result = null;
        if (pct <= 50) {
            result = getRandomIndicesUnder50(pct, lastCounts);
        } else if (pct < 100) {
            Set<Integer> invRes = getRandomIndicesUnder50(100 - pct, map(lastCounts, i -> -i));
            result = getAllIndices(elemsCnt);
            result.removeAll(invRes);
        } else {
            result = getAllIndices(elemsCnt);
        }
        for (Integer idx : result) {
            lastCounts.set(idx, lastCounts.get(idx) + 1);
        }
        return result;
    }

    private void resetCounts(int hash, int elemsCnt) {
        this.hash = hash;
        this.elemsCnt = elemsCnt;
        lastCounts = Stream.generate(() -> 0).limit(elemsCnt).collect(Collectors.toList());
    }

    private Set<Integer> getAllIndices(int hiddableWordsCnt) {
        return Stream.iterate(0, i -> i + 1).limit(hiddableWordsCnt).collect(Collectors.toSet());
    }

    private Set<Integer> getRandomIndicesUnder50(int pct, List<Integer> lastCounts) {
        int resLength = Math.toIntExact(Math.round(lastCounts.size() * pct / 100.0));
        resLength = resLength == 0 ? 1 : resLength;

        int step = Math.toIntExact(Math.round(elemsCnt * 1.0 / resLength));
        step = step == 0 ? 1 : step;

        Set<Integer> res = new HashSet<>();
        int lastIdx = findIdxWithMinCnt(lastCounts, Collections.emptySet());
        res.add(lastIdx);
        while (res.size() < resLength) {
            int baseIdx = (lastIdx + step) % elemsCnt;
            lastIdx = findNextIdx(baseIdx, lastCounts, res);
            res.add(lastIdx);
        }
        return res;
    }

    private int findNextIdx(int baseIdx, List<Integer> lastCounts, Set<Integer> usedIndices) {
        int res = (elemsCnt + baseIdx + calcShift(baseIdx, lastCounts)) % elemsCnt;
        if (!usedIndices.contains(res)) {
            return res;
        } else {
            return findIdxWithMinCnt(lastCounts, usedIndices);
        }
    }

    private int calcShift(int baseIdx, List<Integer> lastCounts) {
        int leftIdx = baseIdx > 0 ? baseIdx - 1 : elemsCnt - 1;
        int rightIdx = baseIdx < elemsCnt - 1 ? baseIdx + 1 : 0;
        return findIdxWithMinCnt(
                listF(
                        lastCounts.get(leftIdx),
                        lastCounts.get(baseIdx),
                        lastCounts.get(rightIdx)
                ),
                Collections.emptySet()
        ) - 1;
    }

    private int findIdxWithMinCnt(List<Integer> counts, Set<Integer> alreadySelectedIndices) {
        Integer min = counts.stream().filter(i->!alreadySelectedIndices.contains(i)).min(Integer::compareTo).get();
        List<Integer> idxs = new LinkedList<>();
        for (int i = 0; i < counts.size(); i++) {
            if (counts.get(i) == min) {
                idxs.add(i);
            }
        }
        return idxs.get(rnd.nextInt(idxs.size()));
    }

    private void resetCountsIfNecessary(int elemsCnt, int hash) {
        if (this.hash != hash || this.elemsCnt != elemsCnt) {
            resetCounts(hash, elemsCnt);
        }
    }
}
