package org.igye.outline2.chess.model;

import org.igye.outline2.common.Randoms;
import org.junit.Test;

import static org.igye.outline2.controllers.chess.CellCoordsConstants.*;
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
                chessBoardBuilder().P(e4).build().encode()
        );
        assertEquals(
                "rnbqkbnrpppppppp9995PPPPPPPPRNBQKBNR",
                chessBoardBuilder()
                        .r(a8).n(b8).b(c8).q(d8).k(e8).b(f8).n(g8).r(h8)
                        .p(a7).p(b7).p(c7).p(d7).p(e7).p(f7).p(g7).p(h7)
                        .P(a2).P(b2).P(c2).P(d2).P(e2).P(f2).P(g2).P(h2)
                        .R(a1).N(b1).B(c1).Q(d1).K(e1).B(f1).N(g1).R(h1)
                        .build().encode()
        );
        assertEquals(
                "r6R1n4N3b2B5qK6Qk5B2b3N4n1R6r",
                chessBoardBuilder()
                        .R(a1).N(b2).B(c3).Q(d4).K(e5).B(f6).N(g7).R(h8)
                        .r(a8).n(b7).b(c6).q(d5).k(e4).b(f3).n(g2).r(h1)
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