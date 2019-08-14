package org.igye.outline2.controllers.chess;

import org.igye.outline2.chess.model.ChessBoard;
import org.junit.Assert;

import java.util.Objects;

import static org.igye.outline2.OutlineUtils.nullSafeGetter;

public class ChessTestUtils {
    public static ChessBoardBuilder chessBoardBuilder() {
        return new ChessBoardBuilder();
    }

    public static void assertEqualsByChessmenTypes(ChessBoard b1, ChessBoard b2) {
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                String msg = "Comparing: x = " + x + " y = " + y + " b1=" + b1.encode() + " b2=" + b2.encode();
                Assert.assertTrue(
                        msg,
                        Objects.equals(
                                nullSafeGetter(b1.getPieceAt(x,y), p->p.getType()),
                                nullSafeGetter(b2.getPieceAt(x,y), p->p.getType())
                        )
                );
            }
        }

    }
}
