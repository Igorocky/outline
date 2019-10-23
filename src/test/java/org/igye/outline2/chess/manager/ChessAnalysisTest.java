package org.igye.outline2.chess.manager;

import org.igye.outline2.chess.model.Move;
import org.igye.outline2.chess.model.Node;
import org.junit.Test;

import java.util.Arrays;

import static org.igye.outline2.chess.model.ChessmanColor.WHITE;
import static org.igye.outline2.controllers.chess.CellCoordsConstants.*;
import static org.igye.outline2.controllers.chess.ChessTestUtils.initialPosition;

public class ChessAnalysisTest {
    @Test
    public void buildTree_buildsTree() {
        ChessAnalysis ca = new ChessAnalysis();

        Move initialPosition = initialPosition(WHITE, b->b
                ._(a8)._(b8)._(c8)._(d8)._(e8)._(f8)._(g8)._(h8)
                ._(a7)._(b7)._(c7).k(d7).r(e7)._(f7)._(g7)._(h7)
                ._(a6)._(b6)._(c6)._(d6)._(e6)._(f6)._(g6)._(h6)
                ._(a5)._(b5)._(c5).K(d5).Q(e5)._(f5)._(g5)._(h5)
                ._(a4)._(b4)._(c4)._(d4)._(e4)._(f4)._(g4)._(h4)
                ._(a3)._(b3)._(c3)._(d3)._(e3)._(f3)._(g3)._(h3)
                ._(a2)._(b2)._(c2)._(d2)._(e2)._(f2)._(g2)._(h2)
                ._(a1)._(b1)._(c1)._(d1)._(e1)._(f1)._(g1)._(h1)
        );

        Node<Move> tree = new Node<>(null, initialPosition);
        ca.buildTree(tree, 3);
//        ca.printToConsole(Arrays.asList(tree), 0);
    }
}
