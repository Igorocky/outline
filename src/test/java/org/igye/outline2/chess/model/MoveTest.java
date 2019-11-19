package org.igye.outline2.chess.model;

import org.junit.Test;

import static org.igye.outline2.chess.model.ChessmanColor.BLACK;
import static org.igye.outline2.chess.model.ChessmanColor.WHITE;
import static org.igye.outline2.controllers.chess.CellCoordsConstants.a1;
import static org.igye.outline2.controllers.chess.CellCoordsConstants.a2;
import static org.igye.outline2.controllers.chess.CellCoordsConstants.a3;
import static org.igye.outline2.controllers.chess.CellCoordsConstants.a4;
import static org.igye.outline2.controllers.chess.CellCoordsConstants.a5;
import static org.igye.outline2.controllers.chess.CellCoordsConstants.a6;
import static org.igye.outline2.controllers.chess.CellCoordsConstants.a7;
import static org.igye.outline2.controllers.chess.CellCoordsConstants.a8;
import static org.igye.outline2.controllers.chess.CellCoordsConstants.b1;
import static org.igye.outline2.controllers.chess.CellCoordsConstants.b2;
import static org.igye.outline2.controllers.chess.CellCoordsConstants.b3;
import static org.igye.outline2.controllers.chess.CellCoordsConstants.b4;
import static org.igye.outline2.controllers.chess.CellCoordsConstants.b5;
import static org.igye.outline2.controllers.chess.CellCoordsConstants.b6;
import static org.igye.outline2.controllers.chess.CellCoordsConstants.b7;
import static org.igye.outline2.controllers.chess.CellCoordsConstants.b8;
import static org.igye.outline2.controllers.chess.CellCoordsConstants.c1;
import static org.igye.outline2.controllers.chess.CellCoordsConstants.c2;
import static org.igye.outline2.controllers.chess.CellCoordsConstants.c3;
import static org.igye.outline2.controllers.chess.CellCoordsConstants.c4;
import static org.igye.outline2.controllers.chess.CellCoordsConstants.c5;
import static org.igye.outline2.controllers.chess.CellCoordsConstants.c6;
import static org.igye.outline2.controllers.chess.CellCoordsConstants.c7;
import static org.igye.outline2.controllers.chess.CellCoordsConstants.c8;
import static org.igye.outline2.controllers.chess.CellCoordsConstants.d1;
import static org.igye.outline2.controllers.chess.CellCoordsConstants.d2;
import static org.igye.outline2.controllers.chess.CellCoordsConstants.d3;
import static org.igye.outline2.controllers.chess.CellCoordsConstants.d4;
import static org.igye.outline2.controllers.chess.CellCoordsConstants.d5;
import static org.igye.outline2.controllers.chess.CellCoordsConstants.d6;
import static org.igye.outline2.controllers.chess.CellCoordsConstants.d7;
import static org.igye.outline2.controllers.chess.CellCoordsConstants.d8;
import static org.igye.outline2.controllers.chess.CellCoordsConstants.e1;
import static org.igye.outline2.controllers.chess.CellCoordsConstants.e2;
import static org.igye.outline2.controllers.chess.CellCoordsConstants.e3;
import static org.igye.outline2.controllers.chess.CellCoordsConstants.e4;
import static org.igye.outline2.controllers.chess.CellCoordsConstants.e5;
import static org.igye.outline2.controllers.chess.CellCoordsConstants.e6;
import static org.igye.outline2.controllers.chess.CellCoordsConstants.e7;
import static org.igye.outline2.controllers.chess.CellCoordsConstants.e8;
import static org.igye.outline2.controllers.chess.CellCoordsConstants.f1;
import static org.igye.outline2.controllers.chess.CellCoordsConstants.f2;
import static org.igye.outline2.controllers.chess.CellCoordsConstants.f3;
import static org.igye.outline2.controllers.chess.CellCoordsConstants.f4;
import static org.igye.outline2.controllers.chess.CellCoordsConstants.f5;
import static org.igye.outline2.controllers.chess.CellCoordsConstants.f6;
import static org.igye.outline2.controllers.chess.CellCoordsConstants.f7;
import static org.igye.outline2.controllers.chess.CellCoordsConstants.f8;
import static org.igye.outline2.controllers.chess.CellCoordsConstants.g1;
import static org.igye.outline2.controllers.chess.CellCoordsConstants.g2;
import static org.igye.outline2.controllers.chess.CellCoordsConstants.g3;
import static org.igye.outline2.controllers.chess.CellCoordsConstants.g4;
import static org.igye.outline2.controllers.chess.CellCoordsConstants.g5;
import static org.igye.outline2.controllers.chess.CellCoordsConstants.g6;
import static org.igye.outline2.controllers.chess.CellCoordsConstants.g7;
import static org.igye.outline2.controllers.chess.CellCoordsConstants.g8;
import static org.igye.outline2.controllers.chess.CellCoordsConstants.h1;
import static org.igye.outline2.controllers.chess.CellCoordsConstants.h2;
import static org.igye.outline2.controllers.chess.CellCoordsConstants.h3;
import static org.igye.outline2.controllers.chess.CellCoordsConstants.h4;
import static org.igye.outline2.controllers.chess.CellCoordsConstants.h5;
import static org.igye.outline2.controllers.chess.CellCoordsConstants.h6;
import static org.igye.outline2.controllers.chess.CellCoordsConstants.h7;
import static org.igye.outline2.controllers.chess.CellCoordsConstants.h8;
import static org.igye.outline2.controllers.chess.ChessTestUtils.initialPosition;
import static org.junit.Assert.assertEquals;

public class MoveTest {
    @Test public void toFen_doesntShowCastlingsIfTheyAreNotPossible() {
        //given
        Move move = initialPosition(WHITE, b->b
                ._(a8)._(b8)._(c8)._(d8)._(e8).K(f8)._(g8)._(h8)
                ._(a7)._(b7).R(c7)._(d7)._(e7)._(f7)._(g7).b(h7)
                ._(a6).B(b6)._(c6)._(d6)._(e6)._(f6)._(g6)._(h6)
                ._(a5)._(b5)._(c5)._(d5)._(e5)._(f5)._(g5)._(h5)
                ._(a4)._(b4)._(c4)._(d4)._(e4).n(f4).B(g4)._(h4)
                ._(a3)._(b3)._(c3)._(d3)._(e3)._(f3)._(g3)._(h3)
                ._(a2).k(b2)._(c2)._(d2)._(e2)._(f2)._(g2)._(h2)
                ._(a1)._(b1)._(c1)._(d1).b(e1)._(f1)._(g1)._(h1)
        );

        //then
        assertEquals("5K2/2R4b/1B6/8/5nB1/8/1k6/4b3 w - - 0 1", move.toFen());
    }
    @Test public void toFen_showsCastlingsIfTheyArePossible() {
        //given
        Move move = initialPosition(WHITE, b->b
                .r(a8)._(b8)._(c8)._(d8).k(e8)._(f8)._(g8)._(h8)
                ._(a7)._(b7)._(c7)._(d7)._(e7)._(f7)._(g7)._(h7)
                ._(a6)._(b6)._(c6)._(d6)._(e6)._(f6)._(g6)._(h6)
                ._(a5)._(b5)._(c5)._(d5)._(e5)._(f5)._(g5)._(h5)
                ._(a4)._(b4)._(c4)._(d4)._(e4)._(f4)._(g4)._(h4)
                ._(a3)._(b3)._(c3)._(d3)._(e3)._(f3)._(g3)._(h3)
                ._(a2)._(b2)._(c2)._(d2)._(e2)._(f2)._(g2)._(h2)
                ._(a1)._(b1)._(c1)._(d1).K(e1)._(f1)._(g1).R(h1)
        );

        //then
        assertEquals("r3k3/8/8/8/8/8/8/4K2R w Kq - 0 1", move.toFen());
    }
    @Test public void moveConstructor_correctly_determines_color_to_move() {
        //then
        assertEquals(WHITE, new Move("r3k3/8/8/8/8/8/8/4K2R w Kq - 0 1").getColorOfWhoToMove());
        assertEquals(BLACK, new Move("r3k3/8/8/r3k3/8/8/8/4K2R b Kq - 0 1").getColorOfWhoToMove());
    }
}