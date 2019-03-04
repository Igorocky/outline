package org.igye.outline.htmlforms;

import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;
import org.igye.outline.data.NodeDao;
import org.igye.outline.model.Node;
import org.igye.outline.model.Paragraph;

import java.util.*;

import static java.util.Comparator.comparingInt;
import static org.igye.outline.common.OutlineUtils.filter;
import static org.igye.outline.common.OutlineUtils.map;

public class LearnNodesData {
    private final NodeDao nodeDao;
    private UUID rootNodeId;
    private List<List<NodeWrapper>> nodes;
    private Random rnd = new Random();

    public LearnNodesData(NodeDao nodeDao) {
        this.nodeDao = nodeDao;
    }

    public List<Node> getNodesToLearn(UUID rootNodeId) {
        if (this.rootNodeId != rootNodeId) {
            this.rootNodeId = rootNodeId;
            reset();
        }
        List<NodeWrapper> line = findLine();
        if (line == null) {
            reset();
            line = findLine();
        }
        Pair<Integer, Integer> maxWindow = getMaxWindow(line);
        return extractSeq(line, maxWindow, 2);
    }

    private List<Node> extractSeq(List<NodeWrapper> line, Pair<Integer, Integer> maxWindow, int padding) {
        int seqSize = padding * 2 + 1;
        int winSize = maxWindow.getRight() - maxWindow.getLeft() + 1;
        int startIdx;
        if (winSize <= seqSize) {
            startIdx = maxWindow.getLeft();
        } else {
            startIdx = maxWindow.getLeft() + rnd.nextInt(winSize - seqSize + 1);
        }
        List<Node> res = new ArrayList<>();
        int endIdx = startIdx + seqSize - 1;
        for (int i = startIdx; i <= endIdx; i++) {
            NodeWrapper nodeWrapper = line.get(i);
            res.add(nodeWrapper.getNode());
            nodeWrapper.setWasReturned(true);
        }
        return res;
    }

    private Pair<Integer, Integer> getMaxWindow(List<NodeWrapper> line) {
        List<Pair<Integer, Integer>> allWindows = new ArrayList<>();
        int wStart = -1;
        for (int i = 0; i < line.size(); i++) {
            if (wStart == -1) {
                if (!line.get(i).isWasReturned()) {
                    wStart = i;
                }
            } else {
                if (line.get(i).isWasReturned()) {
                    allWindows.add(Pair.of(wStart, i-1));
                    wStart = -1;
                }
            }
            if (wStart >= 0) {
                allWindows.add(Pair.of(wStart, line.size()-1));
            }
        }
        Pair<Integer, Integer> maxWindowExample =
                allWindows.stream().max(comparingInt(p -> (p.getRight() - p.getLeft()))).get();
        int maxWindowSize = maxWindowExample.getRight() - maxWindowExample.getLeft();
        List<Pair<Integer, Integer>> maxWindows = filter(allWindows, w -> (w.getRight() - w.getLeft()) == maxWindowSize);
        return maxWindows.get(rnd.nextInt(maxWindows.size()));
    }

    private List<NodeWrapper> findLine() {
        List<List<NodeWrapper>> availableLines = filter(nodes, l -> l.stream().anyMatch(n -> !n.wasReturned));
        if (availableLines.isEmpty()) {
            return null;
        } else {
            return availableLines.get(rnd.nextInt(availableLines.size()));
        }
    }

    private void reset() {
        nodes = new ArrayList<>();
        Node rootNode = nodeDao.loadAllNodesRecursively(rootNodeId);
        Queue<Node> unprocessedParagraphs = new ArrayDeque<>();
        unprocessedParagraphs.add(rootNode);
        while (!unprocessedParagraphs.isEmpty()) {
            Paragraph par = (Paragraph) unprocessedParagraphs.remove();
            if (!par.getChildNodes().isEmpty()) {
                nodes.add(map(par.getChildNodes(), NodeWrapper::new));
                for (Node childNode : par.getChildNodes()) {
                    if (childNode instanceof Paragraph) {
                        unprocessedParagraphs.add(childNode);
                    }
                }
            }
        }
    }

    @Data
    @Builder
    private static class NodeWrapper {
        private Node node;
        private boolean wasReturned;

        public NodeWrapper(Node node) {
            this.node = node;
        }
    }
}
