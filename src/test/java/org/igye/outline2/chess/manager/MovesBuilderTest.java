package org.igye.outline2.chess.manager;

import org.igye.outline2.chess.dto.ChessBoardCellView;
import org.igye.outline2.chess.dto.ChessComponentView;
import org.igye.outline2.chess.model.CellCoords;
import org.igye.outline2.chess.model.ChessmanColor;
import org.igye.outline2.chess.model.ChessmanType;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static org.igye.outline2.OutlineUtils.setOf;
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
import static org.igye.outline2.controllers.chess.ChessTestUtils.assertCellPreparedToMove;
import static org.igye.outline2.controllers.chess.ChessTestUtils.assertCellsAvailableToMoveTo;
import static org.igye.outline2.controllers.chess.ChessTestUtils.assertEquals;
import static org.igye.outline2.controllers.chess.ChessTestUtils.assertEqualsByChessmenTypes;
import static org.igye.outline2.controllers.chess.ChessTestUtils.assertNoCellPreparedToMove;
import static org.igye.outline2.controllers.chess.ChessTestUtils.chessBoard;
import static org.igye.outline2.controllers.chess.ChessTestUtils.chessBoardView;
import static org.igye.outline2.controllers.chess.ChessTestUtils.initialPosition;

public class MovesBuilderTest {

    @Test public void test_highlightingDisappearsIfNextClickOnTheSameCell() {
        //given
        MovesBuilder movesBuilder = new MovesBuilder(initialPosition(WHITE, b->b.N(e4).n(a1)));

        //when
        ChessComponentView view = movesBuilder.cellLeftClicked(e4);
        //then
        assertCellPreparedToMove(view, e4);
        assertCellsAvailableToMoveTo(view, setOf(c3,c5,d6,d2,f6,f2,g3,g5));

        //when
        view = movesBuilder.cellLeftClicked(e4);
        //then
        assertNoCellPreparedToMove(view);
        assertCellsAvailableToMoveTo(view, setOf(a1));
    }
    @Test public void test_highlightingDisappearsIfNextClickOnTheCellNotAvailableToMoveTo() {
        //given
        MovesBuilder movesBuilder = new MovesBuilder(initialPosition(WHITE, b->b.N(e4).n(a1)));

        //when
        ChessComponentView view = movesBuilder.cellLeftClicked(e4);
        //then
        assertCellPreparedToMove(view, e4);
        assertCellsAvailableToMoveTo(view, setOf(c3,c5,d6,d2,f6,f2,g3,g5));

        //when
        view = movesBuilder.cellLeftClicked(c2);
        //then
        assertNoCellPreparedToMove(view);
        assertCellsAvailableToMoveTo(view, setOf(a1));
    }
    @Test public void test_knightMovesFromCenter() {
        //given
        MovesBuilder movesBuilder = new MovesBuilder(initialPosition(WHITE, b->b.N(e4).n(a1)));

        //when
        ChessComponentView view = movesBuilder.cellLeftClicked(e4);
        //then
        assertCellPreparedToMove(view, e4);
        assertCellsAvailableToMoveTo(view, setOf(c3,c5,d6,d2,f6,f2,g3,g5));

        //when
        view = movesBuilder.cellLeftClicked(f2);
        //then
        assertCellPreparedToMove(view, e4);
        assertCellsAvailableToMoveTo(view, setOf(f2));
        assertEqualsByChessmenTypes(chessBoard(b->b.N(f2).n(a1)), view);
    }
    @Test public void test_availableCastlingsAreDeterminedCorrectlyInCaseOfKingsMove() {
        //given
        MovesBuilder movesBuilder = new MovesBuilder(initialPosition(WHITE, b->b
                .r(a8).k(e8).r(h8)
                .p(b7).p(g7)
                .P(b2).P(g2)
                .R(a1).K(e1).R(h1)
        ));

        ChessComponentView view = movesBuilder.cellLeftClicked(e1);
        assertEquals(chessBoardView(b -> b
                ._r(a8).__(b8).__(c8).__(d8)._k(e8).__(f8).__(g8)._r(h8)
                .__(a7)._p(b7).__(c7).__(d7).__(e7).__(f7)._p(g7).__(h7)
                .__(a6).__(b6).__(c6).__(d6).__(e6).__(f6).__(g6).__(h6)
                .__(a5).__(b5).__(c5).__(d5).__(e5).__(f5).__(g5).__(h5)
                .__(a4).__(b4).__(c4).__(d4).__(e4).__(f4).__(g4).__(h4)
                .__(a3).__(b3).__(c3).__(d3).__(e3).__(f3).__(g3).__(h3)
                .__(a2)._P(b2).__(c2).g_(d2).g_(e2).g_(f2)._P(g2).__(h2)
                ._R(a1).__(b1).g_(c1).g_(d1).yK(e1).g_(f1).g_(g1)._R(h1)
        ), view.getChessBoard());

        movesBuilder.cellLeftClicked(e2);
        movesBuilder.cellLeftClicked(b7);
        movesBuilder.cellLeftClicked(b6);
        movesBuilder.cellLeftClicked(e2);
        movesBuilder.cellLeftClicked(e1);
        movesBuilder.cellLeftClicked(b6);
        movesBuilder.cellLeftClicked(b5);
        view = movesBuilder.cellLeftClicked(e1);
        assertEquals(chessBoardView(b -> b
                ._r(a8).__(b8).__(c8).__(d8)._k(e8).__(f8).__(g8)._r(h8)
                .__(a7).__(b7).__(c7).__(d7).__(e7).__(f7)._p(g7).__(h7)
                .__(a6).__(b6).__(c6).__(d6).__(e6).__(f6).__(g6).__(h6)
                .__(a5)._p(b5).__(c5).__(d5).__(e5).__(f5).__(g5).__(h5)
                .__(a4).__(b4).__(c4).__(d4).__(e4).__(f4).__(g4).__(h4)
                .__(a3).__(b3).__(c3).__(d3).__(e3).__(f3).__(g3).__(h3)
                .__(a2)._P(b2).__(c2).g_(d2).g_(e2).g_(f2)._P(g2).__(h2)
                ._R(a1).__(b1).__(c1).g_(d1).yK(e1).g_(f1).__(g1)._R(h1)
        ), view.getChessBoard());

        movesBuilder.cellLeftClicked(e2);
        view = movesBuilder.cellLeftClicked(e8);
        assertEquals(chessBoardView(b -> b
                ._r(a8).__(b8).g_(c8).g_(d8).yk(e8).g_(f8).g_(g8)._r(h8)
                .__(a7).__(b7).__(c7).g_(d7).g_(e7).g_(f7)._p(g7).__(h7)
                .__(a6).__(b6).__(c6).__(d6).__(e6).__(f6).__(g6).__(h6)
                .__(a5)._p(b5).__(c5).__(d5).__(e5).__(f5).__(g5).__(h5)
                .__(a4).__(b4).__(c4).__(d4).__(e4).__(f4).__(g4).__(h4)
                .__(a3).__(b3).__(c3).__(d3).__(e3).__(f3).__(g3).__(h3)
                .__(a2)._P(b2).__(c2).__(d2)._K(e2).__(f2)._P(g2).__(h2)
                ._R(a1).__(b1).__(c1).__(d1).__(e1).__(f1).__(g1)._R(h1)
        ), view.getChessBoard());

        movesBuilder.cellLeftClicked(e7);
        movesBuilder.cellLeftClicked(g2);
        movesBuilder.cellLeftClicked(g3);
        movesBuilder.cellLeftClicked(e7);
        movesBuilder.cellLeftClicked(e8);
        movesBuilder.cellLeftClicked(g3);
        movesBuilder.cellLeftClicked(g4);
        view = movesBuilder.cellLeftClicked(e8);
        assertEquals(chessBoardView(b -> b
                ._r(a8).__(b8).__(c8).g_(d8).yk(e8).g_(f8).__(g8)._r(h8)
                .__(a7).__(b7).__(c7).g_(d7).g_(e7).g_(f7)._p(g7).__(h7)
                .__(a6).__(b6).__(c6).__(d6).__(e6).__(f6).__(g6).__(h6)
                .__(a5)._p(b5).__(c5).__(d5).__(e5).__(f5).__(g5).__(h5)
                .__(a4).__(b4).__(c4).__(d4).__(e4).__(f4)._P(g4).__(h4)
                .__(a3).__(b3).__(c3).__(d3).__(e3).__(f3).__(g3).__(h3)
                .__(a2)._P(b2).__(c2).__(d2)._K(e2).__(f2).__(g2).__(h2)
                ._R(a1).__(b1).__(c1).__(d1).__(e1).__(f1).__(g1)._R(h1)
        ), view.getChessBoard());
    }
    @Test public void test_availableCastlingsAreDeterminedCorrectlyInCaseOfRooksMove() {
        //given
        MovesBuilder movesBuilder = new MovesBuilder(initialPosition(WHITE, b->b
                .r(a8).k(e8).r(h8)
                .p(b7).p(g7)
                .P(b2).P(g2)
                .R(a1).K(e1).R(h1)
        ));

        ChessComponentView view = movesBuilder.cellLeftClicked(e1);
        assertEquals(chessBoardView(b -> b
                ._r(a8).__(b8).__(c8).__(d8)._k(e8).__(f8).__(g8)._r(h8)
                .__(a7)._p(b7).__(c7).__(d7).__(e7).__(f7)._p(g7).__(h7)
                .__(a6).__(b6).__(c6).__(d6).__(e6).__(f6).__(g6).__(h6)
                .__(a5).__(b5).__(c5).__(d5).__(e5).__(f5).__(g5).__(h5)
                .__(a4).__(b4).__(c4).__(d4).__(e4).__(f4).__(g4).__(h4)
                .__(a3).__(b3).__(c3).__(d3).__(e3).__(f3).__(g3).__(h3)
                .__(a2)._P(b2).__(c2).g_(d2).g_(e2).g_(f2)._P(g2).__(h2)
                ._R(a1).__(b1).g_(c1).g_(d1).yK(e1).g_(f1).g_(g1)._R(h1)
        ), view.getChessBoard());

        movesBuilder.cellLeftClicked(a1);
        movesBuilder.cellLeftClicked(a2);
        movesBuilder.cellLeftClicked(b7);
        movesBuilder.cellLeftClicked(b6);
        view = movesBuilder.cellLeftClicked(e1);
        assertEquals(chessBoardView(b -> b
                ._r(a8).__(b8).__(c8).__(d8)._k(e8).__(f8).__(g8)._r(h8)
                .__(a7).__(b7).__(c7).__(d7).__(e7).__(f7)._p(g7).__(h7)
                .__(a6)._p(b6).__(c6).__(d6).__(e6).__(f6).__(g6).__(h6)
                .__(a5).__(b5).__(c5).__(d5).__(e5).__(f5).__(g5).__(h5)
                .__(a4).__(b4).__(c4).__(d4).__(e4).__(f4).__(g4).__(h4)
                .__(a3).__(b3).__(c3).__(d3).__(e3).__(f3).__(g3).__(h3)
                ._R(a2)._P(b2).__(c2).g_(d2).g_(e2).g_(f2)._P(g2).__(h2)
                .__(a1).__(b1).__(c1).g_(d1).yK(e1).g_(f1).g_(g1)._R(h1)
        ), view.getChessBoard());

        movesBuilder.cellLeftClicked(h1);
        movesBuilder.cellLeftClicked(h2);
        movesBuilder.cellLeftClicked(b6);
        movesBuilder.cellLeftClicked(b5);
        view = movesBuilder.cellLeftClicked(e1);
        assertEquals(chessBoardView(b -> b
                ._r(a8).__(b8).__(c8).__(d8)._k(e8).__(f8).__(g8)._r(h8)
                .__(a7).__(b7).__(c7).__(d7).__(e7).__(f7)._p(g7).__(h7)
                .__(a6).__(b6).__(c6).__(d6).__(e6).__(f6).__(g6).__(h6)
                .__(a5)._p(b5).__(c5).__(d5).__(e5).__(f5).__(g5).__(h5)
                .__(a4).__(b4).__(c4).__(d4).__(e4).__(f4).__(g4).__(h4)
                .__(a3).__(b3).__(c3).__(d3).__(e3).__(f3).__(g3).__(h3)
                ._R(a2)._P(b2).__(c2).g_(d2).g_(e2).g_(f2)._P(g2)._R(h2)
                .__(a1).__(b1).__(c1).g_(d1).yK(e1).g_(f1).__(g1).__(h1)
        ), view.getChessBoard());

        movesBuilder.cellLeftClicked(b2);
        movesBuilder.cellLeftClicked(b3);
        view = movesBuilder.cellLeftClicked(e8);
        assertEquals(chessBoardView(b -> b
                ._r(a8).__(b8).g_(c8).g_(d8).yk(e8).g_(f8).g_(g8)._r(h8)
                .__(a7).__(b7).__(c7).g_(d7).g_(e7).g_(f7)._p(g7).__(h7)
                .__(a6).__(b6).__(c6).__(d6).__(e6).__(f6).__(g6).__(h6)
                .__(a5)._p(b5).__(c5).__(d5).__(e5).__(f5).__(g5).__(h5)
                .__(a4).__(b4).__(c4).__(d4).__(e4).__(f4).__(g4).__(h4)
                .__(a3)._P(b3).__(c3).__(d3).__(e3).__(f3).__(g3).__(h3)
                ._R(a2).__(b2).__(c2).__(d2).__(e2).__(f2)._P(g2)._R(h2)
                .__(a1).__(b1).__(c1).__(d1)._K(e1).__(f1).__(g1).__(h1)
        ), view.getChessBoard());

        movesBuilder.cellLeftClicked(a8);
        movesBuilder.cellLeftClicked(a7);
        movesBuilder.cellLeftClicked(b3);
        movesBuilder.cellLeftClicked(b4);
        view = movesBuilder.cellLeftClicked(e8);
        assertEquals(chessBoardView(b -> b
                .__(a8).__(b8).__(c8).g_(d8).yk(e8).g_(f8).g_(g8)._r(h8)
                ._r(a7).__(b7).__(c7).g_(d7).g_(e7).g_(f7)._p(g7).__(h7)
                .__(a6).__(b6).__(c6).__(d6).__(e6).__(f6).__(g6).__(h6)
                .__(a5)._p(b5).__(c5).__(d5).__(e5).__(f5).__(g5).__(h5)
                .__(a4)._P(b4).__(c4).__(d4).__(e4).__(f4).__(g4).__(h4)
                .__(a3).__(b3).__(c3).__(d3).__(e3).__(f3).__(g3).__(h3)
                ._R(a2).__(b2).__(c2).__(d2).__(e2).__(f2)._P(g2)._R(h2)
                .__(a1).__(b1).__(c1).__(d1)._K(e1).__(f1).__(g1).__(h1)
        ), view.getChessBoard());

        movesBuilder.cellLeftClicked(h8);
        movesBuilder.cellLeftClicked(h7);
        movesBuilder.cellLeftClicked(g2);
        movesBuilder.cellLeftClicked(g4);
        view = movesBuilder.cellLeftClicked(e8);
        assertEquals(chessBoardView(b -> b
                .__(a8).__(b8).__(c8).g_(d8).yk(e8).g_(f8).__(g8).__(h8)
                ._r(a7).__(b7).__(c7).g_(d7).g_(e7).g_(f7)._p(g7)._r(h7)
                .__(a6).__(b6).__(c6).__(d6).__(e6).__(f6).__(g6).__(h6)
                .__(a5)._p(b5).__(c5).__(d5).__(e5).__(f5).__(g5).__(h5)
                .__(a4)._P(b4).__(c4).__(d4).__(e4).__(f4)._P(g4).__(h4)
                .__(a3).__(b3).__(c3).__(d3).__(e3).__(f3).__(g3).__(h3)
                ._R(a2).__(b2).__(c2).__(d2).__(e2).__(f2).__(g2)._R(h2)
                .__(a1).__(b1).__(c1).__(d1)._K(e1).__(f1).__(g1).__(h1)
        ), view.getChessBoard());
    }
    @Test public void test_enPasssantIsDeterminedCorrectlyForWhiteLeft() {
        //given
        MovesBuilder movesBuilder = new MovesBuilder(initialPosition(BLACK, b->b
                .p(d7).p(f6).P(e5)
        ));

        ChessComponentView view = movesBuilder.cellLeftClicked(d7);
        assertEquals(chessBoardView(b -> b
                .__(a8).__(b8).__(c8).__(d8).__(e8).__(f8).__(g8).__(h8)
                .__(a7).__(b7).__(c7).yp(d7).__(e7).__(f7).__(g7).__(h7)
                .__(a6).__(b6).__(c6).g_(d6).__(e6)._p(f6).__(g6).__(h6)
                .__(a5).__(b5).__(c5).g_(d5)._P(e5).__(f5).__(g5).__(h5)
                .__(a4).__(b4).__(c4).__(d4).__(e4).__(f4).__(g4).__(h4)
                .__(a3).__(b3).__(c3).__(d3).__(e3).__(f3).__(g3).__(h3)
                .__(a2).__(b2).__(c2).__(d2).__(e2).__(f2).__(g2).__(h2)
                .__(a1).__(b1).__(c1).__(d1).__(e1).__(f1).__(g1).__(h1)
        ), view.getChessBoard());

        view = movesBuilder.cellLeftClicked(d5);
        assertEquals(chessBoardView(b -> b
                .__(a8).__(b8).__(c8).__(d8).__(e8).__(f8).__(g8).__(h8)
                .__(a7).__(b7).__(c7).y_(d7).__(e7).__(f7).__(g7).__(h7)
                .__(a6).__(b6).__(c6).__(d6).__(e6)._p(f6).__(g6).__(h6)
                .__(a5).__(b5).__(c5).gp(d5)._P(e5).__(f5).__(g5).__(h5)
                .__(a4).__(b4).__(c4).__(d4).__(e4).__(f4).__(g4).__(h4)
                .__(a3).__(b3).__(c3).__(d3).__(e3).__(f3).__(g3).__(h3)
                .__(a2).__(b2).__(c2).__(d2).__(e2).__(f2).__(g2).__(h2)
                .__(a1).__(b1).__(c1).__(d1).__(e1).__(f1).__(g1).__(h1)
        ), view.getChessBoard());

        view = movesBuilder.cellLeftClicked(e5);
        assertEquals(chessBoardView(b -> b
                .__(a8).__(b8).__(c8).__(d8).__(e8).__(f8).__(g8).__(h8)
                .__(a7).__(b7).__(c7).__(d7).__(e7).__(f7).__(g7).__(h7)
                .__(a6).__(b6).__(c6).g_(d6).g_(e6).gp(f6).__(g6).__(h6)
                .__(a5).__(b5).__(c5)._p(d5).yP(e5).__(f5).__(g5).__(h5)
                .__(a4).__(b4).__(c4).__(d4).__(e4).__(f4).__(g4).__(h4)
                .__(a3).__(b3).__(c3).__(d3).__(e3).__(f3).__(g3).__(h3)
                .__(a2).__(b2).__(c2).__(d2).__(e2).__(f2).__(g2).__(h2)
                .__(a1).__(b1).__(c1).__(d1).__(e1).__(f1).__(g1).__(h1)
        ), view.getChessBoard());

        view = movesBuilder.cellLeftClicked(d6);
        assertEquals(chessBoardView(b -> b
                .__(a8).__(b8).__(c8).__(d8).__(e8).__(f8).__(g8).__(h8)
                .__(a7).__(b7).__(c7).__(d7).__(e7).__(f7).__(g7).__(h7)
                .__(a6).__(b6).__(c6).gP(d6).__(e6)._p(f6).__(g6).__(h6)
                .__(a5).__(b5).__(c5).__(d5).y_(e5).__(f5).__(g5).__(h5)
                .__(a4).__(b4).__(c4).__(d4).__(e4).__(f4).__(g4).__(h4)
                .__(a3).__(b3).__(c3).__(d3).__(e3).__(f3).__(g3).__(h3)
                .__(a2).__(b2).__(c2).__(d2).__(e2).__(f2).__(g2).__(h2)
                .__(a1).__(b1).__(c1).__(d1).__(e1).__(f1).__(g1).__(h1)
        ), view.getChessBoard());

        view = movesBuilder.cellLeftClicked(f6);
        assertEquals(chessBoardView(b -> b
                .__(a8).__(b8).__(c8).__(d8).__(e8).__(f8).__(g8).__(h8)
                .__(a7).__(b7).__(c7).__(d7).__(e7).__(f7).__(g7).__(h7)
                .__(a6).__(b6).__(c6)._P(d6).__(e6).yp(f6).__(g6).__(h6)
                .__(a5).__(b5).__(c5).__(d5).__(e5).g_(f5).__(g5).__(h5)
                .__(a4).__(b4).__(c4).__(d4).__(e4).__(f4).__(g4).__(h4)
                .__(a3).__(b3).__(c3).__(d3).__(e3).__(f3).__(g3).__(h3)
                .__(a2).__(b2).__(c2).__(d2).__(e2).__(f2).__(g2).__(h2)
                .__(a1).__(b1).__(c1).__(d1).__(e1).__(f1).__(g1).__(h1)
        ), view.getChessBoard());
    }
    @Test public void test_enPasssantIsDeterminedCorrectlyForWhiteRight() {
        //given
        MovesBuilder movesBuilder = new MovesBuilder(initialPosition(BLACK, b->b
                .p(d6).p(f7).P(e5)
        ));

        ChessComponentView view = movesBuilder.cellLeftClicked(f7);
        assertEquals(chessBoardView(b -> b
                .__(a8).__(b8).__(c8).__(d8).__(e8).__(f8).__(g8).__(h8)
                .__(a7).__(b7).__(c7).__(d7).__(e7).yp(f7).__(g7).__(h7)
                .__(a6).__(b6).__(c6)._p(d6).__(e6).g_(f6).__(g6).__(h6)
                .__(a5).__(b5).__(c5).__(d5)._P(e5).g_(f5).__(g5).__(h5)
                .__(a4).__(b4).__(c4).__(d4).__(e4).__(f4).__(g4).__(h4)
                .__(a3).__(b3).__(c3).__(d3).__(e3).__(f3).__(g3).__(h3)
                .__(a2).__(b2).__(c2).__(d2).__(e2).__(f2).__(g2).__(h2)
                .__(a1).__(b1).__(c1).__(d1).__(e1).__(f1).__(g1).__(h1)
        ), view.getChessBoard());

        view = movesBuilder.cellLeftClicked(f5);
        assertEquals(chessBoardView(b -> b
                .__(a8).__(b8).__(c8).__(d8).__(e8).__(f8).__(g8).__(h8)
                .__(a7).__(b7).__(c7).__(d7).__(e7).y_(f7).__(g7).__(h7)
                .__(a6).__(b6).__(c6)._p(d6).__(e6).__(f6).__(g6).__(h6)
                .__(a5).__(b5).__(c5).__(d5)._P(e5).gp(f5).__(g5).__(h5)
                .__(a4).__(b4).__(c4).__(d4).__(e4).__(f4).__(g4).__(h4)
                .__(a3).__(b3).__(c3).__(d3).__(e3).__(f3).__(g3).__(h3)
                .__(a2).__(b2).__(c2).__(d2).__(e2).__(f2).__(g2).__(h2)
                .__(a1).__(b1).__(c1).__(d1).__(e1).__(f1).__(g1).__(h1)
        ), view.getChessBoard());

        view = movesBuilder.cellLeftClicked(e5);
        assertEquals(chessBoardView(b -> b
                .__(a8).__(b8).__(c8).__(d8).__(e8).__(f8).__(g8).__(h8)
                .__(a7).__(b7).__(c7).__(d7).__(e7).__(f7).__(g7).__(h7)
                .__(a6).__(b6).__(c6).gp(d6).g_(e6).g_(f6).__(g6).__(h6)
                .__(a5).__(b5).__(c5).__(d5).yP(e5)._p(f5).__(g5).__(h5)
                .__(a4).__(b4).__(c4).__(d4).__(e4).__(f4).__(g4).__(h4)
                .__(a3).__(b3).__(c3).__(d3).__(e3).__(f3).__(g3).__(h3)
                .__(a2).__(b2).__(c2).__(d2).__(e2).__(f2).__(g2).__(h2)
                .__(a1).__(b1).__(c1).__(d1).__(e1).__(f1).__(g1).__(h1)
        ), view.getChessBoard());

        view = movesBuilder.cellLeftClicked(f6);
        assertEquals(chessBoardView(b -> b
                .__(a8).__(b8).__(c8).__(d8).__(e8).__(f8).__(g8).__(h8)
                .__(a7).__(b7).__(c7).__(d7).__(e7).__(f7).__(g7).__(h7)
                .__(a6).__(b6).__(c6)._p(d6).__(e6).gP(f6).__(g6).__(h6)
                .__(a5).__(b5).__(c5).__(d5).y_(e5).__(f5).__(g5).__(h5)
                .__(a4).__(b4).__(c4).__(d4).__(e4).__(f4).__(g4).__(h4)
                .__(a3).__(b3).__(c3).__(d3).__(e3).__(f3).__(g3).__(h3)
                .__(a2).__(b2).__(c2).__(d2).__(e2).__(f2).__(g2).__(h2)
                .__(a1).__(b1).__(c1).__(d1).__(e1).__(f1).__(g1).__(h1)
        ), view.getChessBoard());

        view = movesBuilder.cellLeftClicked(d6);
        assertEquals(chessBoardView(b -> b
                .__(a8).__(b8).__(c8).__(d8).__(e8).__(f8).__(g8).__(h8)
                .__(a7).__(b7).__(c7).__(d7).__(e7).__(f7).__(g7).__(h7)
                .__(a6).__(b6).__(c6).yp(d6).__(e6)._P(f6).__(g6).__(h6)
                .__(a5).__(b5).__(c5).g_(d5).__(e5).__(f5).__(g5).__(h5)
                .__(a4).__(b4).__(c4).__(d4).__(e4).__(f4).__(g4).__(h4)
                .__(a3).__(b3).__(c3).__(d3).__(e3).__(f3).__(g3).__(h3)
                .__(a2).__(b2).__(c2).__(d2).__(e2).__(f2).__(g2).__(h2)
                .__(a1).__(b1).__(c1).__(d1).__(e1).__(f1).__(g1).__(h1)
        ), view.getChessBoard());
    }
    @Test public void test_enPasssantIsDeterminedCorrectlyForBlackLeft() {
        //given
        MovesBuilder movesBuilder = new MovesBuilder(initialPosition(WHITE, b->b
                .P(a2).p(b4).P(c3)
        ));

        ChessComponentView view = movesBuilder.cellLeftClicked(a2);
        assertEquals(chessBoardView(b -> b
                .__(a8).__(b8).__(c8).__(d8).__(e8).__(f8).__(g8).__(h8)
                .__(a7).__(b7).__(c7).__(d7).__(e7).__(f7).__(g7).__(h7)
                .__(a6).__(b6).__(c6).__(d6).__(e6).__(f6).__(g6).__(h6)
                .__(a5).__(b5).__(c5).__(d5).__(e5).__(f5).__(g5).__(h5)
                .g_(a4)._p(b4).__(c4).__(d4).__(e4).__(f4).__(g4).__(h4)
                .g_(a3).__(b3)._P(c3).__(d3).__(e3).__(f3).__(g3).__(h3)
                .yP(a2).__(b2).__(c2).__(d2).__(e2).__(f2).__(g2).__(h2)
                .__(a1).__(b1).__(c1).__(d1).__(e1).__(f1).__(g1).__(h1)
        ), view.getChessBoard());

        view = movesBuilder.cellLeftClicked(a4);
        assertEquals(chessBoardView(b -> b
                .__(a8).__(b8).__(c8).__(d8).__(e8).__(f8).__(g8).__(h8)
                .__(a7).__(b7).__(c7).__(d7).__(e7).__(f7).__(g7).__(h7)
                .__(a6).__(b6).__(c6).__(d6).__(e6).__(f6).__(g6).__(h6)
                .__(a5).__(b5).__(c5).__(d5).__(e5).__(f5).__(g5).__(h5)
                .gP(a4)._p(b4).__(c4).__(d4).__(e4).__(f4).__(g4).__(h4)
                .__(a3).__(b3)._P(c3).__(d3).__(e3).__(f3).__(g3).__(h3)
                .y_(a2).__(b2).__(c2).__(d2).__(e2).__(f2).__(g2).__(h2)
                .__(a1).__(b1).__(c1).__(d1).__(e1).__(f1).__(g1).__(h1)
        ), view.getChessBoard());

        view = movesBuilder.cellLeftClicked(b4);
        assertEquals(chessBoardView(b -> b
                .__(a8).__(b8).__(c8).__(d8).__(e8).__(f8).__(g8).__(h8)
                .__(a7).__(b7).__(c7).__(d7).__(e7).__(f7).__(g7).__(h7)
                .__(a6).__(b6).__(c6).__(d6).__(e6).__(f6).__(g6).__(h6)
                .__(a5).__(b5).__(c5).__(d5).__(e5).__(f5).__(g5).__(h5)
                ._P(a4).yp(b4).__(c4).__(d4).__(e4).__(f4).__(g4).__(h4)
                .g_(a3).g_(b3).gP(c3).__(d3).__(e3).__(f3).__(g3).__(h3)
                .__(a2).__(b2).__(c2).__(d2).__(e2).__(f2).__(g2).__(h2)
                .__(a1).__(b1).__(c1).__(d1).__(e1).__(f1).__(g1).__(h1)
        ), view.getChessBoard());

        view = movesBuilder.cellLeftClicked(a3);
        assertEquals(chessBoardView(b -> b
                .__(a8).__(b8).__(c8).__(d8).__(e8).__(f8).__(g8).__(h8)
                .__(a7).__(b7).__(c7).__(d7).__(e7).__(f7).__(g7).__(h7)
                .__(a6).__(b6).__(c6).__(d6).__(e6).__(f6).__(g6).__(h6)
                .__(a5).__(b5).__(c5).__(d5).__(e5).__(f5).__(g5).__(h5)
                .__(a4).y_(b4).__(c4).__(d4).__(e4).__(f4).__(g4).__(h4)
                .gp(a3).__(b3)._P(c3).__(d3).__(e3).__(f3).__(g3).__(h3)
                .__(a2).__(b2).__(c2).__(d2).__(e2).__(f2).__(g2).__(h2)
                .__(a1).__(b1).__(c1).__(d1).__(e1).__(f1).__(g1).__(h1)
        ), view.getChessBoard());

        view = movesBuilder.cellLeftClicked(c3);
        assertEquals(chessBoardView(b -> b
                .__(a8).__(b8).__(c8).__(d8).__(e8).__(f8).__(g8).__(h8)
                .__(a7).__(b7).__(c7).__(d7).__(e7).__(f7).__(g7).__(h7)
                .__(a6).__(b6).__(c6).__(d6).__(e6).__(f6).__(g6).__(h6)
                .__(a5).__(b5).__(c5).__(d5).__(e5).__(f5).__(g5).__(h5)
                .__(a4).__(b4).g_(c4).__(d4).__(e4).__(f4).__(g4).__(h4)
                ._p(a3).__(b3).yP(c3).__(d3).__(e3).__(f3).__(g3).__(h3)
                .__(a2).__(b2).__(c2).__(d2).__(e2).__(f2).__(g2).__(h2)
                .__(a1).__(b1).__(c1).__(d1).__(e1).__(f1).__(g1).__(h1)
        ), view.getChessBoard());
    }
    @Test public void test_enPasssantIsDeterminedCorrectlyForBlackRight() {
        //given
        MovesBuilder movesBuilder = new MovesBuilder(initialPosition(WHITE, b->b
                .P(a3).p(b4).P(c2)
        ));

        ChessComponentView view = movesBuilder.cellLeftClicked(c2);
        assertEquals(chessBoardView(b -> b
                .__(a8).__(b8).__(c8).__(d8).__(e8).__(f8).__(g8).__(h8)
                .__(a7).__(b7).__(c7).__(d7).__(e7).__(f7).__(g7).__(h7)
                .__(a6).__(b6).__(c6).__(d6).__(e6).__(f6).__(g6).__(h6)
                .__(a5).__(b5).__(c5).__(d5).__(e5).__(f5).__(g5).__(h5)
                .__(a4)._p(b4).g_(c4).__(d4).__(e4).__(f4).__(g4).__(h4)
                ._P(a3).__(b3).g_(c3).__(d3).__(e3).__(f3).__(g3).__(h3)
                .__(a2).__(b2).yP(c2).__(d2).__(e2).__(f2).__(g2).__(h2)
                .__(a1).__(b1).__(c1).__(d1).__(e1).__(f1).__(g1).__(h1)
        ), view.getChessBoard());

        view = movesBuilder.cellLeftClicked(c4);
        assertEquals(chessBoardView(b -> b
                .__(a8).__(b8).__(c8).__(d8).__(e8).__(f8).__(g8).__(h8)
                .__(a7).__(b7).__(c7).__(d7).__(e7).__(f7).__(g7).__(h7)
                .__(a6).__(b6).__(c6).__(d6).__(e6).__(f6).__(g6).__(h6)
                .__(a5).__(b5).__(c5).__(d5).__(e5).__(f5).__(g5).__(h5)
                .__(a4)._p(b4).gP(c4).__(d4).__(e4).__(f4).__(g4).__(h4)
                ._P(a3).__(b3).__(c3).__(d3).__(e3).__(f3).__(g3).__(h3)
                .__(a2).__(b2).y_(c2).__(d2).__(e2).__(f2).__(g2).__(h2)
                .__(a1).__(b1).__(c1).__(d1).__(e1).__(f1).__(g1).__(h1)
        ), view.getChessBoard());

        view = movesBuilder.cellLeftClicked(b4);
        assertEquals(chessBoardView(b -> b
                .__(a8).__(b8).__(c8).__(d8).__(e8).__(f8).__(g8).__(h8)
                .__(a7).__(b7).__(c7).__(d7).__(e7).__(f7).__(g7).__(h7)
                .__(a6).__(b6).__(c6).__(d6).__(e6).__(f6).__(g6).__(h6)
                .__(a5).__(b5).__(c5).__(d5).__(e5).__(f5).__(g5).__(h5)
                .__(a4).yp(b4)._P(c4).__(d4).__(e4).__(f4).__(g4).__(h4)
                .gP(a3).g_(b3).g_(c3).__(d3).__(e3).__(f3).__(g3).__(h3)
                .__(a2).__(b2).__(c2).__(d2).__(e2).__(f2).__(g2).__(h2)
                .__(a1).__(b1).__(c1).__(d1).__(e1).__(f1).__(g1).__(h1)
        ), view.getChessBoard());

        view = movesBuilder.cellLeftClicked(c3);
        assertEquals(chessBoardView(b -> b
                .__(a8).__(b8).__(c8).__(d8).__(e8).__(f8).__(g8).__(h8)
                .__(a7).__(b7).__(c7).__(d7).__(e7).__(f7).__(g7).__(h7)
                .__(a6).__(b6).__(c6).__(d6).__(e6).__(f6).__(g6).__(h6)
                .__(a5).__(b5).__(c5).__(d5).__(e5).__(f5).__(g5).__(h5)
                .__(a4).y_(b4).__(c4).__(d4).__(e4).__(f4).__(g4).__(h4)
                ._P(a3).__(b3).gp(c3).__(d3).__(e3).__(f3).__(g3).__(h3)
                .__(a2).__(b2).__(c2).__(d2).__(e2).__(f2).__(g2).__(h2)
                .__(a1).__(b1).__(c1).__(d1).__(e1).__(f1).__(g1).__(h1)
        ), view.getChessBoard());

        view = movesBuilder.cellLeftClicked(a3);
        assertEquals(chessBoardView(b -> b
                .__(a8).__(b8).__(c8).__(d8).__(e8).__(f8).__(g8).__(h8)
                .__(a7).__(b7).__(c7).__(d7).__(e7).__(f7).__(g7).__(h7)
                .__(a6).__(b6).__(c6).__(d6).__(e6).__(f6).__(g6).__(h6)
                .__(a5).__(b5).__(c5).__(d5).__(e5).__(f5).__(g5).__(h5)
                .g_(a4).__(b4).__(c4).__(d4).__(e4).__(f4).__(g4).__(h4)
                .yP(a3).__(b3)._p(c3).__(d3).__(e3).__(f3).__(g3).__(h3)
                .__(a2).__(b2).__(c2).__(d2).__(e2).__(f2).__(g2).__(h2)
                .__(a1).__(b1).__(c1).__(d1).__(e1).__(f1).__(g1).__(h1)
        ), view.getChessBoard());
    }
    @Test public void test_whitePawnTurnsIntoAnotherPiece() {
        MovesBuilder movesBuilder = new MovesBuilder(initialPosition(WHITE, b->b
                .P(f6).p(e7)
        ));

        ChessComponentView view = movesBuilder.cellLeftClicked(f6);
        assertEquals(chessBoardView(b -> b
                .__(a8).__(b8).__(c8).__(d8).__(e8).__(f8).__(g8).__(h8)
                .__(a7).__(b7).__(c7).__(d7).gp(e7).g_(f7).__(g7).__(h7)
                .__(a6).__(b6).__(c6).__(d6).__(e6).yP(f6).__(g6).__(h6)
                .__(a5).__(b5).__(c5).__(d5).__(e5).__(f5).__(g5).__(h5)
                .__(a4).__(b4).__(c4).__(d4).__(e4).__(f4).__(g4).__(h4)
                .__(a3).__(b3).__(c3).__(d3).__(e3).__(f3).__(g3).__(h3)
                .__(a2).__(b2).__(c2).__(d2).__(e2).__(f2).__(g2).__(h2)
                .__(a1).__(b1).__(c1).__(d1).__(e1).__(f1).__(g1).__(h1)
        ), view.getChessBoard());

        view = movesBuilder.cellLeftClicked(f7);
        assertEquals(chessBoardView(b -> b
                .__(a8).__(b8).__(c8).__(d8).__(e8).__(f8).__(g8).__(h8)
                .__(a7).__(b7).__(c7).__(d7)._p(e7).gP(f7).__(g7).__(h7)
                .__(a6).__(b6).__(c6).__(d6).__(e6).y_(f6).__(g6).__(h6)
                .__(a5).__(b5).__(c5).__(d5).__(e5).__(f5).__(g5).__(h5)
                .__(a4).__(b4).__(c4).__(d4).__(e4).__(f4).__(g4).__(h4)
                .__(a3).__(b3).__(c3).__(d3).__(e3).__(f3).__(g3).__(h3)
                .__(a2).__(b2).__(c2).__(d2).__(e2).__(f2).__(g2).__(h2)
                .__(a1).__(b1).__(c1).__(d1).__(e1).__(f1).__(g1).__(h1)
        ), view.getChessBoard());

        view = movesBuilder.cellLeftClicked(e7);
        assertEquals(chessBoardView(b -> b
                .__(a8).__(b8).__(c8).__(d8).__(e8).__(f8).__(g8).__(h8)
                .__(a7).__(b7).__(c7).__(d7).yp(e7)._P(f7).__(g7).__(h7)
                .__(a6).__(b6).__(c6).__(d6).g_(e6).__(f6).__(g6).__(h6)
                .__(a5).__(b5).__(c5).__(d5).g_(e5).__(f5).__(g5).__(h5)
                .__(a4).__(b4).__(c4).__(d4).__(e4).__(f4).__(g4).__(h4)
                .__(a3).__(b3).__(c3).__(d3).__(e3).__(f3).__(g3).__(h3)
                .__(a2).__(b2).__(c2).__(d2).__(e2).__(f2).__(g2).__(h2)
                .__(a1).__(b1).__(c1).__(d1).__(e1).__(f1).__(g1).__(h1)
        ), view.getChessBoard());

        view = movesBuilder.cellLeftClicked(e6);
        assertEquals(chessBoardView(b -> b
                .__(a8).__(b8).__(c8).__(d8).__(e8).__(f8).__(g8).__(h8)
                .__(a7).__(b7).__(c7).__(d7).y_(e7)._P(f7).__(g7).__(h7)
                .__(a6).__(b6).__(c6).__(d6).gp(e6).__(f6).__(g6).__(h6)
                .__(a5).__(b5).__(c5).__(d5).__(e5).__(f5).__(g5).__(h5)
                .__(a4).__(b4).__(c4).__(d4).__(e4).__(f4).__(g4).__(h4)
                .__(a3).__(b3).__(c3).__(d3).__(e3).__(f3).__(g3).__(h3)
                .__(a2).__(b2).__(c2).__(d2).__(e2).__(f2).__(g2).__(h2)
                .__(a1).__(b1).__(c1).__(d1).__(e1).__(f1).__(g1).__(h1)
        ), view.getChessBoard());

        view = movesBuilder.cellLeftClicked(f7);
        assertEquals(chessBoardView(b -> b
                .__(a8).__(b8).__(c8).__(d8).__(e8).g_(f8).__(g8).__(h8)
                .__(a7).__(b7).__(c7).__(d7).__(e7).yP(f7).__(g7).__(h7)
                .__(a6).__(b6).__(c6).__(d6)._p(e6).__(f6).__(g6).__(h6)
                .__(a5).__(b5).__(c5).__(d5).__(e5).__(f5).__(g5).__(h5)
                .__(a4).__(b4).__(c4).__(d4).__(e4).__(f4).__(g4).__(h4)
                .__(a3).__(b3).__(c3).__(d3).__(e3).__(f3).__(g3).__(h3)
                .__(a2).__(b2).__(c2).__(d2).__(e2).__(f2).__(g2).__(h2)
                .__(a1).__(b1).__(c1).__(d1).__(e1).__(f1).__(g1).__(h1)
        ), view.getChessBoard());

        view = movesBuilder.cellLeftClicked(f8);
        assertEquals(chessBoardView(b -> b
                .__(a8).__(b8).__(c8).__(d8).__(e8).g_(f8).__(g8).__(h8)
                .__(a7).__(b7).__(c7).__(d7).__(e7).yP(f7).__(g7).__(h7)
                .__(a6).__(b6).__(c6).__(d6)._p(e6).__(f6).__(g6).__(h6)
                .__(a5).__(b5).__(c5).__(d5).__(e5).__(f5).__(g5).__(h5)
                .__(a4).__(b4).__(c4).__(d4).__(e4).__(f4).__(g4).__(h4)
                .__(a3).__(b3).__(c3).__(d3).__(e3).__(f3).__(g3).__(h3)
                .__(a2).__(b2).__(c2).__(d2).__(e2).__(f2).__(g2).__(h2)
                .__(a1).__(b1).__(c1).__(d1).__(e1).__(f1).__(g1).__(h1)
        ), view.getChessBoard());
        assertChoseChessmanTypeDialogForColor(WHITE, view);

        view = movesBuilder.cellLeftClicked(new CellCoords(20,0));
        assertEquals(chessBoardView(b -> b
                .__(a8).__(b8).__(c8).__(d8).__(e8).gN(f8).__(g8).__(h8)
                .__(a7).__(b7).__(c7).__(d7).__(e7).y_(f7).__(g7).__(h7)
                .__(a6).__(b6).__(c6).__(d6)._p(e6).__(f6).__(g6).__(h6)
                .__(a5).__(b5).__(c5).__(d5).__(e5).__(f5).__(g5).__(h5)
                .__(a4).__(b4).__(c4).__(d4).__(e4).__(f4).__(g4).__(h4)
                .__(a3).__(b3).__(c3).__(d3).__(e3).__(f3).__(g3).__(h3)
                .__(a2).__(b2).__(c2).__(d2).__(e2).__(f2).__(g2).__(h2)
                .__(a1).__(b1).__(c1).__(d1).__(e1).__(f1).__(g1).__(h1)
        ), view.getChessBoard());
        Assert.assertNull(view.getChoseChessmanTypeDialogView());

        view = movesBuilder.cellLeftClicked(e6);
        assertEquals(chessBoardView(b -> b
                .__(a8).__(b8).__(c8).__(d8).__(e8)._N(f8).__(g8).__(h8)
                .__(a7).__(b7).__(c7).__(d7).__(e7).__(f7).__(g7).__(h7)
                .__(a6).__(b6).__(c6).__(d6).yp(e6).__(f6).__(g6).__(h6)
                .__(a5).__(b5).__(c5).__(d5).g_(e5).__(f5).__(g5).__(h5)
                .__(a4).__(b4).__(c4).__(d4).__(e4).__(f4).__(g4).__(h4)
                .__(a3).__(b3).__(c3).__(d3).__(e3).__(f3).__(g3).__(h3)
                .__(a2).__(b2).__(c2).__(d2).__(e2).__(f2).__(g2).__(h2)
                .__(a1).__(b1).__(c1).__(d1).__(e1).__(f1).__(g1).__(h1)
        ), view.getChessBoard());
    }
    @Test public void test_blackPawnTurnsIntoAnotherPiece() {
        MovesBuilder movesBuilder = new MovesBuilder(initialPosition(BLACK, b->b
                .p(f3).P(e2)
        ));

        ChessComponentView view = movesBuilder.cellLeftClicked(f3);
        assertEquals(chessBoardView(b -> b
                .__(a8).__(b8).__(c8).__(d8).__(e8).__(f8).__(g8).__(h8)
                .__(a7).__(b7).__(c7).__(d7).__(e7).__(f7).__(g7).__(h7)
                .__(a6).__(b6).__(c6).__(d6).__(e6).__(f6).__(g6).__(h6)
                .__(a5).__(b5).__(c5).__(d5).__(e5).__(f5).__(g5).__(h5)
                .__(a4).__(b4).__(c4).__(d4).__(e4).__(f4).__(g4).__(h4)
                .__(a3).__(b3).__(c3).__(d3).__(e3).yp(f3).__(g3).__(h3)
                .__(a2).__(b2).__(c2).__(d2).gP(e2).g_(f2).__(g2).__(h2)
                .__(a1).__(b1).__(c1).__(d1).__(e1).__(f1).__(g1).__(h1)
        ), view.getChessBoard());

        view = movesBuilder.cellLeftClicked(f2);
        assertEquals(chessBoardView(b -> b
                .__(a8).__(b8).__(c8).__(d8).__(e8).__(f8).__(g8).__(h8)
                .__(a7).__(b7).__(c7).__(d7).__(e7).__(f7).__(g7).__(h7)
                .__(a6).__(b6).__(c6).__(d6).__(e6).__(f6).__(g6).__(h6)
                .__(a5).__(b5).__(c5).__(d5).__(e5).__(f5).__(g5).__(h5)
                .__(a4).__(b4).__(c4).__(d4).__(e4).__(f4).__(g4).__(h4)
                .__(a3).__(b3).__(c3).__(d3).__(e3).y_(f3).__(g3).__(h3)
                .__(a2).__(b2).__(c2).__(d2)._P(e2).gp(f2).__(g2).__(h2)
                .__(a1).__(b1).__(c1).__(d1).__(e1).__(f1).__(g1).__(h1)
        ), view.getChessBoard());

        view = movesBuilder.cellLeftClicked(e2);
        assertEquals(chessBoardView(b -> b
                .__(a8).__(b8).__(c8).__(d8).__(e8).__(f8).__(g8).__(h8)
                .__(a7).__(b7).__(c7).__(d7).__(e7).__(f7).__(g7).__(h7)
                .__(a6).__(b6).__(c6).__(d6).__(e6).__(f6).__(g6).__(h6)
                .__(a5).__(b5).__(c5).__(d5).__(e5).__(f5).__(g5).__(h5)
                .__(a4).__(b4).__(c4).__(d4).g_(e4).__(f4).__(g4).__(h4)
                .__(a3).__(b3).__(c3).__(d3).g_(e3).__(f3).__(g3).__(h3)
                .__(a2).__(b2).__(c2).__(d2).yP(e2)._p(f2).__(g2).__(h2)
                .__(a1).__(b1).__(c1).__(d1).__(e1).__(f1).__(g1).__(h1)
        ), view.getChessBoard());

        view = movesBuilder.cellLeftClicked(e4);
        assertEquals(chessBoardView(b -> b
                .__(a8).__(b8).__(c8).__(d8).__(e8).__(f8).__(g8).__(h8)
                .__(a7).__(b7).__(c7).__(d7).__(e7).__(f7).__(g7).__(h7)
                .__(a6).__(b6).__(c6).__(d6).__(e6).__(f6).__(g6).__(h6)
                .__(a5).__(b5).__(c5).__(d5).__(e5).__(f5).__(g5).__(h5)
                .__(a4).__(b4).__(c4).__(d4).gP(e4).__(f4).__(g4).__(h4)
                .__(a3).__(b3).__(c3).__(d3).__(e3).__(f3).__(g3).__(h3)
                .__(a2).__(b2).__(c2).__(d2).y_(e2)._p(f2).__(g2).__(h2)
                .__(a1).__(b1).__(c1).__(d1).__(e1).__(f1).__(g1).__(h1)
        ), view.getChessBoard());

        view = movesBuilder.cellLeftClicked(f2);
        assertEquals(chessBoardView(b -> b
                .__(a8).__(b8).__(c8).__(d8).__(e8).__(f8).__(g8).__(h8)
                .__(a7).__(b7).__(c7).__(d7).__(e7).__(f7).__(g7).__(h7)
                .__(a6).__(b6).__(c6).__(d6).__(e6).__(f6).__(g6).__(h6)
                .__(a5).__(b5).__(c5).__(d5).__(e5).__(f5).__(g5).__(h5)
                .__(a4).__(b4).__(c4).__(d4)._P(e4).__(f4).__(g4).__(h4)
                .__(a3).__(b3).__(c3).__(d3).__(e3).__(f3).__(g3).__(h3)
                .__(a2).__(b2).__(c2).__(d2).__(e2).yp(f2).__(g2).__(h2)
                .__(a1).__(b1).__(c1).__(d1).__(e1).g_(f1).__(g1).__(h1)
        ), view.getChessBoard());

        view = movesBuilder.cellLeftClicked(f1);
        assertEquals(chessBoardView(b -> b
                .__(a8).__(b8).__(c8).__(d8).__(e8).__(f8).__(g8).__(h8)
                .__(a7).__(b7).__(c7).__(d7).__(e7).__(f7).__(g7).__(h7)
                .__(a6).__(b6).__(c6).__(d6).__(e6).__(f6).__(g6).__(h6)
                .__(a5).__(b5).__(c5).__(d5).__(e5).__(f5).__(g5).__(h5)
                .__(a4).__(b4).__(c4).__(d4)._P(e4).__(f4).__(g4).__(h4)
                .__(a3).__(b3).__(c3).__(d3).__(e3).__(f3).__(g3).__(h3)
                .__(a2).__(b2).__(c2).__(d2).__(e2).yp(f2).__(g2).__(h2)
                .__(a1).__(b1).__(c1).__(d1).__(e1).g_(f1).__(g1).__(h1)
        ), view.getChessBoard());
        assertChoseChessmanTypeDialogForColor(BLACK, view);

        view = movesBuilder.cellLeftClicked(new CellCoords(23,0));
        assertEquals(chessBoardView(b -> b
                .__(a8).__(b8).__(c8).__(d8).__(e8).__(f8).__(g8).__(h8)
                .__(a7).__(b7).__(c7).__(d7).__(e7).__(f7).__(g7).__(h7)
                .__(a6).__(b6).__(c6).__(d6).__(e6).__(f6).__(g6).__(h6)
                .__(a5).__(b5).__(c5).__(d5).__(e5).__(f5).__(g5).__(h5)
                .__(a4).__(b4).__(c4).__(d4)._P(e4).__(f4).__(g4).__(h4)
                .__(a3).__(b3).__(c3).__(d3).__(e3).__(f3).__(g3).__(h3)
                .__(a2).__(b2).__(c2).__(d2).__(e2).y_(f2).__(g2).__(h2)
                .__(a1).__(b1).__(c1).__(d1).__(e1).gq(f1).__(g1).__(h1)
        ), view.getChessBoard());
        Assert.assertNull(view.getChoseChessmanTypeDialogView());

        view = movesBuilder.cellLeftClicked(e4);
        assertEquals(chessBoardView(b -> b
                .__(a8).__(b8).__(c8).__(d8).__(e8).__(f8).__(g8).__(h8)
                .__(a7).__(b7).__(c7).__(d7).__(e7).__(f7).__(g7).__(h7)
                .__(a6).__(b6).__(c6).__(d6).__(e6).__(f6).__(g6).__(h6)
                .__(a5).__(b5).__(c5).__(d5).g_(e5).__(f5).__(g5).__(h5)
                .__(a4).__(b4).__(c4).__(d4).yP(e4).__(f4).__(g4).__(h4)
                .__(a3).__(b3).__(c3).__(d3).__(e3).__(f3).__(g3).__(h3)
                .__(a2).__(b2).__(c2).__(d2).__(e2).__(f2).__(g2).__(h2)
                .__(a1).__(b1).__(c1).__(d1).__(e1)._q(f1).__(g1).__(h1)
        ), view.getChessBoard());
        Assert.assertNull(view.getChoseChessmanTypeDialogView());
    }
    @Test public void test_ChoseChessmanTypeDialogIsOpened_noMovesAreAccepted() {
        MovesBuilder movesBuilder = new MovesBuilder(initialPosition(BLACK, b->b
                .p(f2).P(e2)
        ));

        ChessComponentView view = movesBuilder.cellLeftClicked(f2);
        assertEquals(chessBoardView(b -> b
                .__(a8).__(b8).__(c8).__(d8).__(e8).__(f8).__(g8).__(h8)
                .__(a7).__(b7).__(c7).__(d7).__(e7).__(f7).__(g7).__(h7)
                .__(a6).__(b6).__(c6).__(d6).__(e6).__(f6).__(g6).__(h6)
                .__(a5).__(b5).__(c5).__(d5).__(e5).__(f5).__(g5).__(h5)
                .__(a4).__(b4).__(c4).__(d4).__(e4).__(f4).__(g4).__(h4)
                .__(a3).__(b3).__(c3).__(d3).__(e3).__(f3).__(g3).__(h3)
                .__(a2).__(b2).__(c2).__(d2)._P(e2).yp(f2).__(g2).__(h2)
                .__(a1).__(b1).__(c1).__(d1).__(e1).g_(f1).__(g1).__(h1)
        ), view.getChessBoard());

        view = movesBuilder.cellLeftClicked(f1);
        assertEquals(chessBoardView(b -> b
                .__(a8).__(b8).__(c8).__(d8).__(e8).__(f8).__(g8).__(h8)
                .__(a7).__(b7).__(c7).__(d7).__(e7).__(f7).__(g7).__(h7)
                .__(a6).__(b6).__(c6).__(d6).__(e6).__(f6).__(g6).__(h6)
                .__(a5).__(b5).__(c5).__(d5).__(e5).__(f5).__(g5).__(h5)
                .__(a4).__(b4).__(c4).__(d4).__(e4).__(f4).__(g4).__(h4)
                .__(a3).__(b3).__(c3).__(d3).__(e3).__(f3).__(g3).__(h3)
                .__(a2).__(b2).__(c2).__(d2)._P(e2).yp(f2).__(g2).__(h2)
                .__(a1).__(b1).__(c1).__(d1).__(e1).g_(f1).__(g1).__(h1)
        ), view.getChessBoard());
        assertChoseChessmanTypeDialogForColor(BLACK, view);

        view = movesBuilder.cellLeftClicked(e2);
        assertEquals(chessBoardView(b -> b
                .__(a8).__(b8).__(c8).__(d8).__(e8).__(f8).__(g8).__(h8)
                .__(a7).__(b7).__(c7).__(d7).__(e7).__(f7).__(g7).__(h7)
                .__(a6).__(b6).__(c6).__(d6).__(e6).__(f6).__(g6).__(h6)
                .__(a5).__(b5).__(c5).__(d5).__(e5).__(f5).__(g5).__(h5)
                .__(a4).__(b4).__(c4).__(d4).__(e4).__(f4).__(g4).__(h4)
                .__(a3).__(b3).__(c3).__(d3).__(e3).__(f3).__(g3).__(h3)
                .__(a2).__(b2).__(c2).__(d2)._P(e2).yp(f2).__(g2).__(h2)
                .__(a1).__(b1).__(c1).__(d1).__(e1).g_(f1).__(g1).__(h1)
        ), view.getChessBoard());
        assertChoseChessmanTypeDialogForColor(BLACK, view);

        view = movesBuilder.cellLeftClicked(f1);
        assertEquals(chessBoardView(b -> b
                .__(a8).__(b8).__(c8).__(d8).__(e8).__(f8).__(g8).__(h8)
                .__(a7).__(b7).__(c7).__(d7).__(e7).__(f7).__(g7).__(h7)
                .__(a6).__(b6).__(c6).__(d6).__(e6).__(f6).__(g6).__(h6)
                .__(a5).__(b5).__(c5).__(d5).__(e5).__(f5).__(g5).__(h5)
                .__(a4).__(b4).__(c4).__(d4).__(e4).__(f4).__(g4).__(h4)
                .__(a3).__(b3).__(c3).__(d3).__(e3).__(f3).__(g3).__(h3)
                .__(a2).__(b2).__(c2).__(d2)._P(e2).yp(f2).__(g2).__(h2)
                .__(a1).__(b1).__(c1).__(d1).__(e1).g_(f1).__(g1).__(h1)
        ), view.getChessBoard());
        assertChoseChessmanTypeDialogForColor(BLACK, view);
    }
    @Test public void test_itIsNotPossibleToMoveKingToACellWhereItWillBeChecked() {
        MovesBuilder movesBuilder = new MovesBuilder(initialPosition(WHITE, b->b
                .p(b7).p(e4).K(c2)
        ));

        ChessComponentView view = movesBuilder.cellLeftClicked(c2);
        assertEquals(chessBoardView(b -> b
                .__(a8).__(b8).__(c8).__(d8).__(e8).__(f8).__(g8).__(h8)
                .__(a7)._p(b7).__(c7).__(d7).__(e7).__(f7).__(g7).__(h7)
                .__(a6).__(b6).__(c6).__(d6).__(e6).__(f6).__(g6).__(h6)
                .__(a5).__(b5).__(c5).__(d5).__(e5).__(f5).__(g5).__(h5)
                .__(a4).__(b4).__(c4).__(d4)._p(e4).__(f4).__(g4).__(h4)
                .__(a3).g_(b3).g_(c3).__(d3).__(e3).__(f3).__(g3).__(h3)
                .__(a2).g_(b2).yK(c2).g_(d2).__(e2).__(f2).__(g2).__(h2)
                .__(a1).g_(b1).g_(c1).g_(d1).__(e1).__(f1).__(g1).__(h1)
        ), view.getChessBoard());
    }
    @Test public void test_itIsNotPossibleToCastleIfKingIsChecked() {
        MovesBuilder movesBuilder = new MovesBuilder(initialPosition(WHITE, b->b
                .r(e8)
                .p(b7)
                .R(a1).K(e1).R(h1)
        ));

        ChessComponentView view = movesBuilder.cellLeftClicked(e1);
        assertEquals(chessBoardView(b -> b
                .__(a8).__(b8).__(c8).__(d8)._r(e8).__(f8).__(g8).__(h8)
                .__(a7)._p(b7).__(c7).__(d7).__(e7).__(f7).__(g7).__(h7)
                .__(a6).__(b6).__(c6).__(d6).__(e6).__(f6).__(g6).__(h6)
                .__(a5).__(b5).__(c5).__(d5).__(e5).__(f5).__(g5).__(h5)
                .__(a4).__(b4).__(c4).__(d4).__(e4).__(f4).__(g4).__(h4)
                .__(a3).__(b3).__(c3).__(d3).__(e3).__(f3).__(g3).__(h3)
                .__(a2).__(b2).__(c2).g_(d2).__(e2).g_(f2).__(g2).__(h2)
                ._R(a1).__(b1).__(c1).g_(d1).yK(e1).g_(f1).__(g1)._R(h1)
        ), view.getChessBoard());
    }
    @Test public void test_itIsNotPossibleToCastleIfKingWouldJumpOverAnAttackedCell() {
        MovesBuilder movesBuilder = new MovesBuilder(initialPosition(BLACK, b->b
                .r(a8).k(e8).r(h8)
                .P(g4).B(h4)
        ));

        ChessComponentView view = movesBuilder.cellLeftClicked(e8);
        assertEquals(chessBoardView(b -> b
                ._r(a8).__(b8).__(c8).__(d8).yk(e8).g_(f8).g_(g8)._r(h8)
                .__(a7).__(b7).__(c7).g_(d7).__(e7).g_(f7).__(g7).__(h7)
                .__(a6).__(b6).__(c6).__(d6).__(e6).__(f6).__(g6).__(h6)
                .__(a5).__(b5).__(c5).__(d5).__(e5).__(f5).__(g5).__(h5)
                .__(a4).__(b4).__(c4).__(d4).__(e4).__(f4)._P(g4)._B(h4)
                .__(a3).__(b3).__(c3).__(d3).__(e3).__(f3).__(g3).__(h3)
                .__(a2).__(b2).__(c2).__(d2).__(e2).__(f2).__(g2).__(h2)
                .__(a1).__(b1).__(c1).__(d1).__(e1).__(f1).__(g1).__(h1)
        ), view.getChessBoard());
    }

    private void assertChoseChessmanTypeDialogForColor(ChessmanColor color, ChessComponentView view) {
        final List<ChessBoardCellView> cellsToChoseFrom = view.getChoseChessmanTypeDialogView().getCellsToChoseFrom();
        Assert.assertEquals(4, cellsToChoseFrom.size());

        Assert.assertEquals(new CellCoords(20,0), cellsToChoseFrom.get(0).getCoords());
        Assert.assertEquals(
                color.equals(WHITE) ? ChessmanType.WHITE_KNIGHT.getCode() : ChessmanType.BLACK_KNIGHT.getCode(),
                cellsToChoseFrom.get(0).getCode()
        );

        Assert.assertEquals(new CellCoords(21,0), cellsToChoseFrom.get(1).getCoords());
        Assert.assertEquals(
                color.equals(WHITE) ? ChessmanType.WHITE_BISHOP.getCode() : ChessmanType.BLACK_BISHOP.getCode(),
                cellsToChoseFrom.get(1).getCode()
        );

        Assert.assertEquals(new CellCoords(22,0), cellsToChoseFrom.get(2).getCoords());
        Assert.assertEquals(
                color.equals(WHITE) ? ChessmanType.WHITE_ROOK.getCode() : ChessmanType.BLACK_ROOK.getCode(),
                cellsToChoseFrom.get(2).getCode()
        );

        Assert.assertEquals(new CellCoords(23,0), cellsToChoseFrom.get(3).getCoords());
        Assert.assertEquals(
                color.equals(WHITE) ? ChessmanType.WHITE_QUEEN.getCode() : ChessmanType.BLACK_QUEEN.getCode(),
                cellsToChoseFrom.get(3).getCode()
        );
    }

}