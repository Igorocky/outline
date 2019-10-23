package org.igye.outline2.chess.manager;

import org.igye.outline2.chess.model.CellCoords;
import org.igye.outline2.chess.model.ChessmanColor;
import org.igye.outline2.chess.model.Move;
import org.igye.outline2.chess.model.Node;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ChessAnalysis {
    public void findMate(Move move, int depth) {
        Node<Move> root = new Node<>(null, move);
        ChessmanColor colorToMove = move.getColorOfWhoMadeMove().invert();
        Set<CellCoords> piecesToMove = move.findAll(ct -> ct.getPieceColor().equals(colorToMove));

    }

    public void buildTree(Node<Move> root, int depth) {
        if (depth == 0) {
            return;
        }
        Move position = root.getValue();
        ChessmanColor colorToMove = position.getColorOfWhoMadeMove().invert();
        Set<CellCoords> piecesToMove = position.findAll(ct -> ct.getPieceColor().equals(colorToMove));
        List<Node<Move>> children = piecesToMove.stream()
                .flatMap(from -> position.getPossibleNextMoves(from).stream())
                .map(nextPos -> new Node<>(root, nextPos))
                .collect(Collectors.toList());
        if (children.isEmpty()) {
            return;
        }
        root.setChildren(children);
        children.forEach(child -> buildTree(child, depth-1));
    }

    public void printToConsole(List<Node<Move>> nodes, int depth) {
        String prefix = createPrefix(depth);
        for (Node<Move> node : nodes) {
            System.out.println(prefix + node.getValue().getShortNotation());
            printToConsole(node.getChildren(), depth+1);
        }
    }

    private String createPrefix(int depth) {
        if (depth == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            sb.append("   |");
        }
        sb.append("--");
        return sb.toString();
    }
}
