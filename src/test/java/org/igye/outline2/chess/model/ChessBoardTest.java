package org.igye.outline2.chess.model;

import org.igye.outline2.controllers.Randoms;
import org.junit.Test;

import static org.igye.outline2.controllers.chess.ChessTestUtils.assertEqualsByChessmenTypes;
import static org.igye.outline2.controllers.chess.ChessTestUtils.chessBoardBuilder;
import static org.junit.Assert.assertEquals;

public class ChessBoardTest {
    @Test
    public void encode_createsCorrectValues() {
        assertEquals(
                "99999991",
                chessBoardBuilder().build().encode()
        );
        assertEquals(
                "9999P999",
                chessBoardBuilder().e4().whitePawn().build().encode()
        );
        assertEquals(
                "rnbqkbnrpppppppp9995PPPPPPPPRNBQKBNR",
                chessBoardBuilder()
                        .a1().whiteRook()
                        .b1().whiteKnight()
                        .c1().whiteBishop()
                        .d1().whiteQueen()
                        .e1().whiteKing()
                        .f1().whiteBishop()
                        .g1().whiteKnight()
                        .h1().whiteRook()
                        .a2().whitePawn()
                        .b2().whitePawn()
                        .c2().whitePawn()
                        .d2().whitePawn()
                        .e2().whitePawn()
                        .f2().whitePawn()
                        .g2().whitePawn()
                        .h2().whitePawn()
                        .a8().blackRook()
                        .b8().blackKnight()
                        .c8().blackBishop()
                        .d8().blackQueen()
                        .e8().blackKing()
                        .f8().blackBishop()
                        .g8().blackKnight()
                        .h8().blackRook()
                        .a7().blackPawn()
                        .b7().blackPawn()
                        .c7().blackPawn()
                        .d7().blackPawn()
                        .e7().blackPawn()
                        .f7().blackPawn()
                        .g7().blackPawn()
                        .h7().blackPawn()
                        .build().encode()
        );
        assertEquals(
                "r6R1n4N3b2B5qK6Qk5B2b3N4n1R6r",
                chessBoardBuilder()
                        .a1().whiteRook()
                        .b2().whiteKnight()
                        .c3().whiteBishop()
                        .d4().whiteQueen()
                        .e5().whiteKing()
                        .f6().whiteBishop()
                        .g7().whiteKnight()
                        .h8().whiteRook()
                        .a8().blackRook()
                        .b7().blackKnight()
                        .c6().blackBishop()
                        .d5().blackQueen()
                        .e4().blackKing()
                        .f3().blackBishop()
                        .g2().blackKnight()
                        .h1().blackRook()
                        .build().encode()
        );
    }

    @Test
    public void decode_createsSameChessboardAsWasEncoded() {
        decode_createsSameChessboardAsWasEncoded(30);
        decode_createsSameChessboardAsWasEncoded(60);
        decode_createsSameChessboardAsWasEncoded(90);
    }

    private void decode_createsSameChessboardAsWasEncoded(int emptyCellProb) {
        for (int i = 0; i < 30; i++) {
            ChessBoard before = Randoms.randomChessBoard(emptyCellProb);
            String encoded = before.encode();
            ChessBoard after = new ChessBoard(encoded);
            assertEqualsByChessmenTypes(before, after);
        }
    }

}