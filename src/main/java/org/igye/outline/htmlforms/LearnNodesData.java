package org.igye.outline.htmlforms;

import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;
import org.igye.outline.data.NodeDao;
import org.igye.outline.exceptions.OutlineException;
import org.igye.outline.model.*;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Comparator.comparingInt;
import static org.igye.outline.common.OutlineUtils.*;

public class LearnNodesData {
    private final NodeDao nodeDao;
    private UUID rootNodeId;
    private List<List<NodeWrapper>> nodes;
    private Random rnd = new Random();
    private int nodesTotalCnt;
    private Set<Set<UUID>> unions;
    private int numberOfCycles;

    public LearnNodesData(NodeDao nodeDao) {
        this.nodeDao = nodeDao;
    }

    public NodesToLearnDto getNodesToLearn(UUID rootNodeId) {
        if (!Objects.equals(this.rootNodeId, rootNodeId)) {
            this.rootNodeId = rootNodeId;
            reset();
            resetStat(true);
        }
        List<NodeWrapper> line = findLine();
        if (line == null) {
            reset();
            line = findLine();
        }
        Pair<Integer, Integer> maxWindow = getMaxWindow(line);

        List<NodeDto> nodesToLearn = extractSeq(line, maxWindow, 2);
        updateStat(nodesToLearn);
        return NodesToLearnDto.builder()
                .nodesToLearn(nodesToLearn)
                .path(buildPath(nodesToLearn))
                .nodesTotalCnt(nodesTotalCnt)
                .numberOfUnions(unions.size())
                .numberOfCycles(numberOfCycles)
                .build();
    }

    public void resetStat() {
        resetStat(true);
    }

    private void resetStat(boolean resetCyclesCnt) {
        nodesTotalCnt = calcTotalNodesCnt();
        if (nodes != null) {
            unions = nodes.stream().flatMap(line->line.stream().map(e->setF(e.node.getId()))).collect(Collectors.toSet());
        }
        if (resetCyclesCnt) {
            numberOfCycles = 0;
        }
    }

    private int calcTotalNodesCnt() {
        return nodes == null ? 0 : nodes.stream().map(List::size).reduce(0, (l,r)->l+r);
    }

    private void updateStat(List<NodeDto> nodesToLearn) {
        if (unions.size() == 1) {
            resetStat(false);
            numberOfCycles++;
        }
        Set<UUID> newUnion = new HashSet<>(
                nodesToLearn.stream()
                        .filter(e->e!=null)
                        .map(NodeDto::getId)
                        .collect(Collectors.toSet())
        );
        Set<Set<UUID>> affectedUnions = new HashSet<>();
        for (Set<UUID> union : unions) {
            for (NodeDto nodeDto : nodesToLearn) {
                if (nodeDto.getId() != null && union.contains(nodeDto.getId())) {
                    affectedUnions.add(union);
                    newUnion.addAll(union);
                    break;
                }
            }
        }
        unions.removeAll(affectedUnions);
        unions.add(newUnion);
    }

    private List<NodeDto> buildPath(List<NodeDto> nodesToLearn) {
        NodeDto childNode = nodesToLearn.stream().filter(n -> n.getId() != null).findFirst().get();
        List<NodeDto> path = new ArrayList<>();

        Node lastNode = nodeDao.loadNodeById(childNode.getId()).getParentNode();
        path.add(nodeToNodeDto(lastNode));
        while (!Objects.equals(rootNodeId, lastNode.getId())) {
            lastNode = lastNode.getParentNode();
            path.add(nodeToNodeDto(lastNode));
        }
        Collections.reverse(path);
        return path;
    }

    private List<NodeDto> extractSeq(List<NodeWrapper> line, Pair<Integer, Integer> maxWindow, int padding) {
        int winSize = maxWindow.getRight() - maxWindow.getLeft() + 1;
        int centerIdx = maxWindow.getLeft() + rnd.nextInt(winSize);
        int startIdx = centerIdx - padding;
        int endIdx = centerIdx + padding;
        List<NodeDto> res = new ArrayList<>();
        for (int i = startIdx; i <= endIdx; i++) {
            res.add(getNodeDto(line,i));
        }
        return res;
    }

    private NodeDto getNodeDto(List<NodeWrapper> line, int idx) {
        if (idx < 0 || idx >= line.size()) {
            return NodeDto.builder()
                    .id(null)
                    .iconId(null)
                    .title("EMPTY")
                    .url(null)
                    .build();
        } else {
            NodeWrapper nodeWrapper = line.get(idx);
            nodeWrapper.setWasReturned(true);
            Node node = nodeWrapper.getNode();
            return nodeToNodeDto(node);
        }
    }

    private NodeDto nodeToNodeDto(Node node) {
        return NodeDto.builder()
                .id(node.getId())
                .iconId(getIconId(node))
                .title(node.getName())
                .url(getUrl(node))
                .build();
    }

    private UUID getIconId(Node node) {
        if (node instanceof Paragraph) {
            Icon icon = ((Paragraph) node).getIcon();
            return icon != null ? icon.getId() : null;
        } else if (node instanceof Topic) {
            Icon icon = ((Topic)node).getIcon();
            return icon != null ? icon.getId() : null;
        } else if (node instanceof EngText) {
            return null;
        } else {
            throw new OutlineException("Unexpected type of node: " + node.getClass());
        }
    }

    private String getUrl(Node node) {
        if (node instanceof Paragraph) {
            return "paragraph?id=" + node.getId() + "&showContent=true#main-title";
        } else if (node instanceof Topic) {
            return "topic?id=" + node.getId() + "&showContent=true#main-title";
        } else if (node instanceof EngText) {
            return "words/prepareText?id=" + node.getId() + "&showContent=true#main-title";
        } else {
            throw new OutlineException("Unexpected type of node: " + node.getClass());
        }
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
        }
        if (wStart >= 0) {
            allWindows.add(Pair.of(wStart, line.size()-1));
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
    private static class NodeWrapper {
        private Node node;
        private boolean wasReturned;

        public NodeWrapper(Node node) {
            this.node = node;
        }
    }
}
