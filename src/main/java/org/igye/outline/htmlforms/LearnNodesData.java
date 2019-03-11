package org.igye.outline.htmlforms;

import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;
import org.igye.outline.data.NodeDao;
import org.igye.outline.exceptions.OutlineException;
import org.igye.outline.model.*;

import java.util.*;

import static java.util.Comparator.comparingInt;
import static org.igye.outline.common.OutlineUtils.filter;
import static org.igye.outline.common.OutlineUtils.map;

public class LearnNodesData {
    private final NodeDao nodeDao;
    private UUID rootNodeId;
    private List<List<NodeWrapper>> nodes;
    private Random rnd = new Random();
    private int numberOfCycles;
    private static final int PADDING = 2;
    private static final int ELEMS_IN_RESPONSE = PADDING*2+1;

    public LearnNodesData(NodeDao nodeDao) {
        this.nodeDao = nodeDao;
    }

    public NodesToLearnDto getNodesToLearn(UUID rootNodeId) {
        if (!Objects.equals(this.rootNodeId, rootNodeId)) {
            this.rootNodeId = rootNodeId;
            reset(true);
        }
        List<NodeWrapper> line = findLine();
        if (line == null) {
            reset(false);
            line = findLine();
        }
        Pair<Integer, Integer> maxWindow = getMaxWindow(line);

        List<NodeDto> nodesToLearn = extractSeq(line, maxWindow, PADDING);
        return NodesToLearnDto.builder()
                .nodesToLearn(nodesToLearn)
                .path(buildPath(nodesToLearn))
                .transitionsTotalCnt(countTransitionsTotalCnt())
                .numberOfConnectedTransitions(countConnectedTransitions())
                .numberOfCycles(numberOfCycles)
                .build();
    }

    private int countConnectedTransitions() {
        int res = nodes.stream()
                .map(list->(int)list.stream().filter(NodeWrapper::isConnectedToTheNext).count())
                .reduce(0, (l,r)->l+r);
        if (res < ELEMS_IN_RESPONSE) {
            return -1;
        } else {
            return res;
        }
    }

    private int countTransitionsTotalCnt() {
        return nodes.stream().map(list->list.size()).reduce(0, (l,r)->l+r);
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
            res.add(getNodeDto(line, i, i == endIdx));
        }
        return res;
    }

    private NodeDto getNodeDto(List<NodeWrapper> line, int idx, boolean isLast) {
        if (idx < 0 || idx >= line.size()) {
            return NodeDto.builder()
                    .id(null)
                    .iconId(null)
                    .title("EMPTY")
                    .url(null)
                    .build();
        } else {
            NodeWrapper nodeWrapper = line.get(idx);
            nodeWrapper.setConnectedToTheNext(nodeWrapper.isConnectedToTheNext() || !isLast);
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
                if (!line.get(i).isConnectedToTheNext()) {
                    wStart = i;
                }
            } else {
                if (line.get(i).isConnectedToTheNext()) {
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
        List<List<NodeWrapper>> availableLines = filter(nodes, l -> l.stream().anyMatch(n -> !n.isConnectedToTheNext()));
        if (availableLines.isEmpty()) {
            return null;
        } else {
            return availableLines.get(rnd.nextInt(availableLines.size()));
        }
    }

    private void reset(boolean resetNumberOfCycles) {
        if (resetNumberOfCycles) {
            numberOfCycles = 0;
        } else {
            numberOfCycles++;
        }
        nodes = new ArrayList<>();
        Node rootNode = nodeDao.loadAllNodesRecursively(rootNodeId);
        Queue<Node> unprocessedParagraphs = new ArrayDeque<>();
        unprocessedParagraphs.add(rootNode);
        while (!unprocessedParagraphs.isEmpty()) {
            Paragraph par = (Paragraph) unprocessedParagraphs.remove();
            if (!par.getChildNodes().isEmpty()) {
                Node firstFakeNode = new Topic();
                firstFakeNode.setId(null);
                firstFakeNode.setName("EMPTY");

                ArrayList<Node> notMappedNodes = new ArrayList<>();
                notMappedNodes.add(firstFakeNode);
                notMappedNodes.addAll(par.getChildNodes());

                nodes.add(map(notMappedNodes, NodeWrapper::new));
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
        private boolean connectedToTheNext;

        public NodeWrapper(Node node) {
            this.node = node;
        }
    }
}
