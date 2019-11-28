package org.igye.outline2.chess.manager;

import org.igye.outline2.chess.dto.ChessComponentView;
import org.junit.Test;

import java.util.Collections;

import static org.igye.outline2.chess.model.ChessmanColor.BLACK;
import static org.igye.outline2.chess.model.ChessmanColor.WHITE;
import static org.igye.outline2.common.OutlineUtils.setOf;
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
import static org.igye.outline2.controllers.chess.ChessTestUtils.assertBoardsEqual;
import static org.igye.outline2.controllers.chess.ChessTestUtils.assertCellPreparedToMove;
import static org.igye.outline2.controllers.chess.ChessTestUtils.assertCellsAvailableToMoveTo;
import static org.igye.outline2.controllers.chess.ChessTestUtils.assertEqualsByChessmenTypes;
import static org.igye.outline2.controllers.chess.ChessTestUtils.assertNoCellPreparedToMove;
import static org.igye.outline2.controllers.chess.ChessTestUtils.chessBoard;
import static org.igye.outline2.controllers.chess.ChessTestUtils.chessBoardBuilder;
import static org.igye.outline2.controllers.chess.ChessTestUtils.chessBoardView;
import static org.igye.outline2.controllers.chess.ChessTestUtils.execCommand;
import static org.igye.outline2.controllers.chess.ChessTestUtils.getLastMove;
import static org.igye.outline2.controllers.chess.ChessTestUtils.getSelectedMove;
import static org.igye.outline2.controllers.chess.ChessTestUtils.initialPosition;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class MovesBuilderTest {

    @Test public void test_highlightingDisappearsIfNextClickOnTheSameCell() {
        //given
        MovesBuilder movesBuilder = new MovesBuilder(null, initialPosition(WHITE, b->b.N(e4).n(a1)));

        //when
        ChessComponentView view = movesBuilder.cellLeftClicked(e4);
        //then
        assertCellPreparedToMove(view, e4);
        assertCellsAvailableToMoveTo(view, setOf(c3,c5,d6,d2,f6,f2,g3,g5));

        //when
        view = movesBuilder.cellLeftClicked(e4);
        //then
        assertNoCellPreparedToMove(view);
        assertCellsAvailableToMoveTo(view, Collections.EMPTY_SET);
    }
    @Test public void test_highlightingDisappearsIfNextClickOnTheCellNotAvailableToMoveTo() {
        //given
        MovesBuilder movesBuilder = new MovesBuilder(null, initialPosition(BLACK, b->b.N(e4).n(b3).P(a1)));
        movesBuilder.cellLeftClicked(b3);
        ChessComponentView view = movesBuilder.cellLeftClicked(a1);
        assertCellPreparedToMove(view, b3);
        assertCellsAvailableToMoveTo(view, setOf(a1));

        //when
        view = movesBuilder.cellLeftClicked(e4);
        //then
        assertCellPreparedToMove(view, e4);
        assertCellsAvailableToMoveTo(view, setOf(c3,c5,d6,d2,f6,f2,g3,g5));

        //when
        view = movesBuilder.cellLeftClicked(c2);
        //then
        assertCellPreparedToMove(view, b3);
        assertCellsAvailableToMoveTo(view, setOf(a1));
    }
    @Test public void test_knightMovesFromCenter() {
        //given
        MovesBuilder movesBuilder = new MovesBuilder(null, initialPosition(WHITE, b->b.N(e4).n(a1)));

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
        MovesBuilder movesBuilder = new MovesBuilder(null, initialPosition(WHITE, b->b
                .r(a8).k(e8).r(h8)
                .p(b7).p(g7)
                .P(b2).P(g2)
                .R(a1).K(e1).R(h1)
        ));

        ChessComponentView view = movesBuilder.cellLeftClicked(e1);
        assertBoardsEqual(chessBoardView(b -> b
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
        assertBoardsEqual(chessBoardView(b -> b
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
        assertBoardsEqual(chessBoardView(b -> b
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
        assertBoardsEqual(chessBoardView(b -> b
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
        MovesBuilder movesBuilder = new MovesBuilder(null, initialPosition(WHITE, b->b
                .r(a8).k(e8).r(h8)
                .p(b7).p(g7)
                .P(b2).P(g2)
                .R(a1).K(e1).R(h1)
        ));

        ChessComponentView view = movesBuilder.cellLeftClicked(e1);
        assertBoardsEqual(chessBoardView(b -> b
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
        assertBoardsEqual(chessBoardView(b -> b
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
        assertBoardsEqual(chessBoardView(b -> b
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
        assertBoardsEqual(chessBoardView(b -> b
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
        assertBoardsEqual(chessBoardView(b -> b
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
        assertBoardsEqual(chessBoardView(b -> b
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
        MovesBuilder movesBuilder = new MovesBuilder(null, initialPosition(BLACK, b->b
                .p(d7).p(f6).P(e5)
        ));

        ChessComponentView view = movesBuilder.cellLeftClicked(d7);
        assertBoardsEqual(chessBoardView(b -> b
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
        assertBoardsEqual(chessBoardView(b -> b
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
        assertBoardsEqual(chessBoardView(b -> b
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
        assertBoardsEqual(chessBoardView(b -> b
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
        assertBoardsEqual(chessBoardView(b -> b
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
        MovesBuilder movesBuilder = new MovesBuilder(null, initialPosition(BLACK, b->b
                .p(d6).p(f7).P(e5)
        ));

        ChessComponentView view = movesBuilder.cellLeftClicked(f7);
        assertBoardsEqual(chessBoardView(b -> b
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
        assertBoardsEqual(chessBoardView(b -> b
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
        assertBoardsEqual(chessBoardView(b -> b
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
        assertBoardsEqual(chessBoardView(b -> b
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
        assertBoardsEqual(chessBoardView(b -> b
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
        MovesBuilder movesBuilder = new MovesBuilder(null, initialPosition(WHITE, b->b
                .P(a2).p(b4).P(c3)
        ));

        ChessComponentView view = movesBuilder.cellLeftClicked(a2);
        assertBoardsEqual(chessBoardView(b -> b
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
        assertBoardsEqual(chessBoardView(b -> b
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
        assertBoardsEqual(chessBoardView(b -> b
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
        assertBoardsEqual(chessBoardView(b -> b
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
        assertBoardsEqual(chessBoardView(b -> b
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
        MovesBuilder movesBuilder = new MovesBuilder(null, initialPosition(WHITE, b->b
                .P(a3).p(b4).P(c2)
        ));

        ChessComponentView view = movesBuilder.cellLeftClicked(c2);
        assertBoardsEqual(chessBoardView(b -> b
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
        assertBoardsEqual(chessBoardView(b -> b
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
        assertBoardsEqual(chessBoardView(b -> b
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
        assertBoardsEqual(chessBoardView(b -> b
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
        assertBoardsEqual(chessBoardView(b -> b
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
    @Test public void test_whitePawnCannotJumpOverAPiece() {
        //given
        MovesBuilder movesBuilder = new MovesBuilder(null, initialPosition(WHITE, b->b
                ._(a8)._(b8)._(c8)._(d8).k(e8)._(f8)._(g8)._(h8)
                ._(a7)._(b7)._(c7)._(d7)._(e7)._(f7)._(g7)._(h7)
                ._(a6)._(b6)._(c6)._(d6)._(e6)._(f6)._(g6)._(h6)
                ._(a5)._(b5)._(c5)._(d5)._(e5)._(f5)._(g5)._(h5)
                ._(a4)._(b4)._(c4)._(d4)._(e4)._(f4)._(g4)._(h4)
                ._(a3)._(b3)._(c3)._(d3)._(e3)._(f3).Q(g3).p(h3)
                ._(a2)._(b2)._(c2)._(d2)._(e2)._(f2).P(g2)._(h2)
                ._(a1)._(b1)._(c1)._(d1).K(e1)._(f1)._(g1)._(h1)
        ));

        //when
        ChessComponentView view = movesBuilder.cellLeftClicked(g2);

        //then
        assertBoardsEqual(chessBoardView(b -> b
                .__(a8).__(b8).__(c8).__(d8)._k(e8).__(f8).__(g8).__(h8)
                .__(a7).__(b7).__(c7).__(d7).__(e7).__(f7).__(g7).__(h7)
                .__(a6).__(b6).__(c6).__(d6).__(e6).__(f6).__(g6).__(h6)
                .__(a5).__(b5).__(c5).__(d5).__(e5).__(f5).__(g5).__(h5)
                .__(a4).__(b4).__(c4).__(d4).__(e4).__(f4).__(g4).__(h4)
                .__(a3).__(b3).__(c3).__(d3).__(e3).__(f3)._Q(g3).gp(h3)
                .__(a2).__(b2).__(c2).__(d2).__(e2).__(f2).yP(g2).__(h2)
                .__(a1).__(b1).__(c1).__(d1)._K(e1).__(f1).__(g1).__(h1)
        ), view.getChessBoard());
    }
    @Test public void test_blackPawnCannotJumpOverAPiece() {
        //given
        MovesBuilder movesBuilder = new MovesBuilder(null, initialPosition(BLACK, b->b
                ._(a8)._(b8)._(c8)._(d8).k(e8)._(f8)._(g8)._(h8)
                ._(a7)._(b7)._(c7)._(d7)._(e7)._(f7).p(g7)._(h7)
                ._(a6)._(b6)._(c6)._(d6)._(e6)._(f6).q(g6).P(h6)
                ._(a5)._(b5)._(c5)._(d5)._(e5)._(f5)._(g5)._(h5)
                ._(a4)._(b4)._(c4)._(d4)._(e4)._(f4)._(g4)._(h4)
                ._(a3)._(b3)._(c3)._(d3)._(e3)._(f3)._(g3)._(h3)
                ._(a2)._(b2)._(c2)._(d2)._(e2)._(f2)._(g2)._(h2)
                ._(a1)._(b1)._(c1)._(d1).K(e1)._(f1)._(g1)._(h1)
        ));

        //when
        ChessComponentView view = movesBuilder.cellLeftClicked(g7);

        //then
        assertBoardsEqual(chessBoardView(b -> b
                .__(a8).__(b8).__(c8).__(d8)._k(e8).__(f8).__(g8).__(h8)
                .__(a7).__(b7).__(c7).__(d7).__(e7).__(f7).yp(g7).__(h7)
                .__(a6).__(b6).__(c6).__(d6).__(e6).__(f6)._q(g6).gP(h6)
                .__(a5).__(b5).__(c5).__(d5).__(e5).__(f5).__(g5).__(h5)
                .__(a4).__(b4).__(c4).__(d4).__(e4).__(f4).__(g4).__(h4)
                .__(a3).__(b3).__(c3).__(d3).__(e3).__(f3).__(g3).__(h3)
                .__(a2).__(b2).__(c2).__(d2).__(e2).__(f2).__(g2).__(h2)
                .__(a1).__(b1).__(c1).__(d1)._K(e1).__(f1).__(g1).__(h1)
        ), view.getChessBoard());
    }
    @Test public void test_whitePawnTurnsIntoAnotherPiece() {
        MovesBuilder movesBuilder = new MovesBuilder(null, initialPosition(WHITE, b->b
                .P(f6).p(e7)
        ));

        ChessComponentView view = movesBuilder.cellLeftClicked(f6);
        assertBoardsEqual(chessBoardView(b -> b
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
        assertBoardsEqual(chessBoardView(b -> b
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
        assertBoardsEqual(chessBoardView(b -> b
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
        assertBoardsEqual(chessBoardView(b -> b
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
        assertBoardsEqual(chessBoardView(b -> b
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
        assertBoardsEqual(chessBoardView(b -> b
                .__(a8).__(b8).__(c8).__(d8).__(e8).bQ(f8).__(g8).__(h8)
                .__(a7).__(b7).__(c7).__(d7).__(e7).bR(f7).__(g7).__(h7)
                .__(a6).__(b6).__(c6).__(d6)._p(e6).bB(f6).__(g6).__(h6)
                .__(a5).__(b5).__(c5).__(d5).__(e5).bN(f5).__(g5).__(h5)
                .__(a4).__(b4).__(c4).__(d4).__(e4).__(f4).__(g4).__(h4)
                .__(a3).__(b3).__(c3).__(d3).__(e3).__(f3).__(g3).__(h3)
                .__(a2).__(b2).__(c2).__(d2).__(e2).__(f2).__(g2).__(h2)
                .__(a1).__(b1).__(c1).__(d1).__(e1).__(f1).__(g1).__(h1)
        ), view.getChessBoard());

        view = movesBuilder.cellLeftClicked(f5);
        assertBoardsEqual(chessBoardView(b -> b
                .__(a8).__(b8).__(c8).__(d8).__(e8).gN(f8).__(g8).__(h8)
                .__(a7).__(b7).__(c7).__(d7).__(e7).y_(f7).__(g7).__(h7)
                .__(a6).__(b6).__(c6).__(d6)._p(e6).__(f6).__(g6).__(h6)
                .__(a5).__(b5).__(c5).__(d5).__(e5).__(f5).__(g5).__(h5)
                .__(a4).__(b4).__(c4).__(d4).__(e4).__(f4).__(g4).__(h4)
                .__(a3).__(b3).__(c3).__(d3).__(e3).__(f3).__(g3).__(h3)
                .__(a2).__(b2).__(c2).__(d2).__(e2).__(f2).__(g2).__(h2)
                .__(a1).__(b1).__(c1).__(d1).__(e1).__(f1).__(g1).__(h1)
        ), view.getChessBoard());

        view = movesBuilder.cellLeftClicked(e6);
        assertBoardsEqual(chessBoardView(b -> b
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
        MovesBuilder movesBuilder = new MovesBuilder(null, initialPosition(BLACK, b->b
                .p(f3).P(e2)
        ));

        ChessComponentView view = movesBuilder.cellLeftClicked(f3);
        assertBoardsEqual(chessBoardView(b -> b
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
        assertBoardsEqual(chessBoardView(b -> b
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
        assertBoardsEqual(chessBoardView(b -> b
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
        assertBoardsEqual(chessBoardView(b -> b
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
        assertBoardsEqual(chessBoardView(b -> b
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
        assertBoardsEqual(chessBoardView(b -> b
                .__(a8).__(b8).__(c8).__(d8).__(e8).__(f8).__(g8).__(h8)
                .__(a7).__(b7).__(c7).__(d7).__(e7).__(f7).__(g7).__(h7)
                .__(a6).__(b6).__(c6).__(d6).__(e6).__(f6).__(g6).__(h6)
                .__(a5).__(b5).__(c5).__(d5).__(e5).__(f5).__(g5).__(h5)
                .__(a4).__(b4).__(c4).__(d4)._P(e4).bq(f4).__(g4).__(h4)
                .__(a3).__(b3).__(c3).__(d3).__(e3).br(f3).__(g3).__(h3)
                .__(a2).__(b2).__(c2).__(d2).__(e2).bb(f2).__(g2).__(h2)
                .__(a1).__(b1).__(c1).__(d1).__(e1).bn(f1).__(g1).__(h1)
        ), view.getChessBoard());

        view = movesBuilder.cellLeftClicked(f4);
        assertBoardsEqual(chessBoardView(b -> b
                .__(a8).__(b8).__(c8).__(d8).__(e8).__(f8).__(g8).__(h8)
                .__(a7).__(b7).__(c7).__(d7).__(e7).__(f7).__(g7).__(h7)
                .__(a6).__(b6).__(c6).__(d6).__(e6).__(f6).__(g6).__(h6)
                .__(a5).__(b5).__(c5).__(d5).__(e5).__(f5).__(g5).__(h5)
                .__(a4).__(b4).__(c4).__(d4)._P(e4).__(f4).__(g4).__(h4)
                .__(a3).__(b3).__(c3).__(d3).__(e3).__(f3).__(g3).__(h3)
                .__(a2).__(b2).__(c2).__(d2).__(e2).y_(f2).__(g2).__(h2)
                .__(a1).__(b1).__(c1).__(d1).__(e1).gq(f1).__(g1).__(h1)
        ), view.getChessBoard());

        view = movesBuilder.cellLeftClicked(e4);
        assertBoardsEqual(chessBoardView(b -> b
                .__(a8).__(b8).__(c8).__(d8).__(e8).__(f8).__(g8).__(h8)
                .__(a7).__(b7).__(c7).__(d7).__(e7).__(f7).__(g7).__(h7)
                .__(a6).__(b6).__(c6).__(d6).__(e6).__(f6).__(g6).__(h6)
                .__(a5).__(b5).__(c5).__(d5).g_(e5).__(f5).__(g5).__(h5)
                .__(a4).__(b4).__(c4).__(d4).yP(e4).__(f4).__(g4).__(h4)
                .__(a3).__(b3).__(c3).__(d3).__(e3).__(f3).__(g3).__(h3)
                .__(a2).__(b2).__(c2).__(d2).__(e2).__(f2).__(g2).__(h2)
                .__(a1).__(b1).__(c1).__(d1).__(e1)._q(f1).__(g1).__(h1)
        ), view.getChessBoard());
    }
    @Test public void test_choseChessmanTypeDialogIsOpened_noMovesAreAccepted() {
        MovesBuilder movesBuilder = new MovesBuilder(null, initialPosition(BLACK, b->b
                .p(f2).P(e2)
        ));

        ChessComponentView view = movesBuilder.cellLeftClicked(f2);
        assertBoardsEqual(chessBoardView(b -> b
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
        assertBoardsEqual(chessBoardView(b -> b
                .__(a8).__(b8).__(c8).__(d8).__(e8).__(f8).__(g8).__(h8)
                .__(a7).__(b7).__(c7).__(d7).__(e7).__(f7).__(g7).__(h7)
                .__(a6).__(b6).__(c6).__(d6).__(e6).__(f6).__(g6).__(h6)
                .__(a5).__(b5).__(c5).__(d5).__(e5).__(f5).__(g5).__(h5)
                .__(a4).__(b4).__(c4).__(d4).__(e4).bq(f4).__(g4).__(h4)
                .__(a3).__(b3).__(c3).__(d3).__(e3).br(f3).__(g3).__(h3)
                .__(a2).__(b2).__(c2).__(d2)._P(e2).bb(f2).__(g2).__(h2)
                .__(a1).__(b1).__(c1).__(d1).__(e1).bn(f1).__(g1).__(h1)
        ), view.getChessBoard());

        view = movesBuilder.cellLeftClicked(e2);
        assertBoardsEqual(chessBoardView(b -> b
                .__(a8).__(b8).__(c8).__(d8).__(e8).__(f8).__(g8).__(h8)
                .__(a7).__(b7).__(c7).__(d7).__(e7).__(f7).__(g7).__(h7)
                .__(a6).__(b6).__(c6).__(d6).__(e6).__(f6).__(g6).__(h6)
                .__(a5).__(b5).__(c5).__(d5).__(e5).__(f5).__(g5).__(h5)
                .__(a4).__(b4).__(c4).__(d4).__(e4).bq(f4).__(g4).__(h4)
                .__(a3).__(b3).__(c3).__(d3).__(e3).br(f3).__(g3).__(h3)
                .__(a2).__(b2).__(c2).__(d2)._P(e2).bb(f2).__(g2).__(h2)
                .__(a1).__(b1).__(c1).__(d1).__(e1).bn(f1).__(g1).__(h1)
        ), view.getChessBoard());

        view = movesBuilder.cellLeftClicked(e3);
        assertBoardsEqual(chessBoardView(b -> b
                .__(a8).__(b8).__(c8).__(d8).__(e8).__(f8).__(g8).__(h8)
                .__(a7).__(b7).__(c7).__(d7).__(e7).__(f7).__(g7).__(h7)
                .__(a6).__(b6).__(c6).__(d6).__(e6).__(f6).__(g6).__(h6)
                .__(a5).__(b5).__(c5).__(d5).__(e5).__(f5).__(g5).__(h5)
                .__(a4).__(b4).__(c4).__(d4).__(e4).bq(f4).__(g4).__(h4)
                .__(a3).__(b3).__(c3).__(d3).__(e3).br(f3).__(g3).__(h3)
                .__(a2).__(b2).__(c2).__(d2)._P(e2).bb(f2).__(g2).__(h2)
                .__(a1).__(b1).__(c1).__(d1).__(e1).bn(f1).__(g1).__(h1)
        ), view.getChessBoard());
    }
    @Test public void test_itIsNotPossibleToMoveKingToACellWhereItWillBeChecked() {
        MovesBuilder movesBuilder = new MovesBuilder(null, initialPosition(WHITE, b->b
                .p(b7).p(e4).K(c2)
        ));

        ChessComponentView view = movesBuilder.cellLeftClicked(c2);
        assertBoardsEqual(chessBoardView(b -> b
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
        MovesBuilder movesBuilder = new MovesBuilder(null, initialPosition(WHITE, b->b
                .r(e8)
                .p(b7)
                .R(a1).K(e1).R(h1)
        ));

        ChessComponentView view = movesBuilder.cellLeftClicked(e1);
        assertBoardsEqual(chessBoardView(b -> b
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
        MovesBuilder movesBuilder = new MovesBuilder(null, initialPosition(BLACK, b->b
                .r(a8).k(e8).r(h8)
                .P(g4).B(h4)
        ));

        ChessComponentView view = movesBuilder.cellLeftClicked(e8);
        assertBoardsEqual(chessBoardView(b -> b
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
    @Test public void moveNotationForSimplePawnMove() {
        MovesBuilder movesBuilder = new MovesBuilder(null, initialPosition(WHITE, b->b
                ._(a8)._(b8)._(c8)._(d8)._(e8)._(f8)._(g8)._(h8)
                ._(a7)._(b7)._(c7)._(d7)._(e7)._(f7)._(g7)._(h7)
                ._(a6)._(b6)._(c6)._(d6)._(e6)._(f6)._(g6)._(h6)
                ._(a5)._(b5)._(c5).p(d5)._(e5)._(f5)._(g5)._(h5)
                ._(a4)._(b4)._(c4)._(d4)._(e4)._(f4)._(g4)._(h4)
                ._(a3)._(b3).P(c3)._(d3)._(e3)._(f3)._(g3)._(h3)
                ._(a2)._(b2)._(c2)._(d2)._(e2)._(f2)._(g2)._(h2)
                ._(a1)._(b1)._(c1)._(d1)._(e1)._(f1)._(g1)._(h1)
        ));

        ChessComponentView view = execCommand(movesBuilder, "c4");
        assertEquals("c4", getLastMove(view));
    }
    @Test public void moveNotationForAmbiguousPawnMove() {
        MovesBuilder movesBuilder = new MovesBuilder(null, initialPosition(BLACK, b->b
                .k(a8)._(b8)._(c8)._(d8)._(e8)._(f8)._(g8)._(h8)
                ._(a7)._(b7).p(c7)._(d7).p(e7)._(f7)._(g7)._(h7)
                ._(a6)._(b6)._(c6).B(d6)._(e6)._(f6)._(g6)._(h6)
                ._(a5)._(b5)._(c5)._(d5)._(e5)._(f5)._(g5)._(h5)
                ._(a4)._(b4)._(c4)._(d4)._(e4)._(f4)._(g4)._(h4)
                ._(a3)._(b3)._(c3)._(d3)._(e3)._(f3)._(g3)._(h3)
                ._(a2)._(b2)._(c2)._(d2)._(e2)._(f2)._(g2)._(h2)
                .K(a1)._(b1)._(c1)._(d1)._(e1)._(f1)._(g1)._(h1)
        ));

        ChessComponentView view = execCommand(movesBuilder, "cd6");
        assertEquals("cxd6", getLastMove(view));
    }
    @Test public void moveNotationForSimplePawnMoveWithCapture() {
        MovesBuilder movesBuilder = new MovesBuilder(null, initialPosition(WHITE, b->b
                ._(a8)._(b8)._(c8)._(d8)._(e8)._(f8)._(g8)._(h8)
                ._(a7)._(b7).p(c7)._(d7).p(e7)._(f7)._(g7)._(h7)
                ._(a6)._(b6)._(c6).P(d6)._(e6)._(f6).P(g6)._(h6)
                ._(a5)._(b5)._(c5)._(d5)._(e5).P(f5)._(g5)._(h5)
                ._(a4)._(b4)._(c4)._(d4)._(e4)._(f4)._(g4)._(h4)
                ._(a3)._(b3)._(c3)._(d3)._(e3)._(f3)._(g3)._(h3)
                ._(a2)._(b2)._(c2)._(d2)._(e2)._(f2)._(g2)._(h2)
                ._(a1)._(b1)._(c1)._(d1)._(e1)._(f1)._(g1)._(h1)
        ));

        ChessComponentView view = execCommand(movesBuilder, "e7");
        assertEquals("xe7", getLastMove(view));
    }
    @Test public void moveNotationForSimplePawnMoveWhenThePawnChanges() {
        MovesBuilder movesBuilder = new MovesBuilder(null, initialPosition(WHITE, b->b
                ._(a8)._(b8)._(c8)._(d8)._(e8)._(f8)._(g8)._(h8)
                ._(a7)._(b7).p(c7)._(d7).p(e7)._(f7).P(g7)._(h7)
                ._(a6)._(b6)._(c6).P(d6)._(e6)._(f6)._(g6)._(h6)
                ._(a5)._(b5)._(c5)._(d5)._(e5).P(f5)._(g5)._(h5)
                ._(a4)._(b4)._(c4)._(d4)._(e4)._(f4)._(g4)._(h4)
                ._(a3)._(b3)._(c3)._(d3)._(e3)._(f3)._(g3)._(h3)
                ._(a2)._(b2)._(c2)._(d2)._(e2)._(f2)._(g2)._(h2)
                ._(a1)._(b1)._(c1)._(d1)._(e1)._(f1)._(g1)._(h1)
        ));

        ChessComponentView view = execCommand(movesBuilder, "g8q");
        assertEquals("g8Q", getLastMove(view));
    }
    @Test public void moveNotationForSimplePawnMoveWhenThePawnChangesWithCheckAndKingCanEscape() {
        MovesBuilder movesBuilder = new MovesBuilder(null, initialPosition(BLACK, b->b
                ._(a8)._(b8)._(c8)._(d8)._(e8)._(f8)._(g8)._(h8)
                ._(a7)._(b7)._(c7)._(d7)._(e7)._(f7)._(g7)._(h7)
                ._(a6)._(b6)._(c6)._(d6)._(e6)._(f6)._(g6)._(h6)
                ._(a5)._(b5)._(c5)._(d5)._(e5)._(f5)._(g5)._(h5)
                ._(a4)._(b4)._(c4)._(d4)._(e4)._(f4)._(g4)._(h4)
                ._(a3)._(b3)._(c3)._(d3)._(e3)._(f3)._(g3)._(h3)
                ._(a2)._(b2).p(c2)._(d2)._(e2)._(f2)._(g2)._(h2)
                ._(a1)._(b1)._(c1)._(d1).K(e1)._(f1)._(g1)._(h1)
        ));

        ChessComponentView view = execCommand(movesBuilder, "c1r");
        assertEquals("c1R+", getLastMove(view));
    }
    @Test public void moveNotationForSimplePawnMoveWhenThePawnChangesWithCheckAndKingCanBeProtected() {
        MovesBuilder movesBuilder = new MovesBuilder(null, initialPosition(BLACK, b->b
                ._(a8)._(b8)._(c8)._(d8)._(e8)._(f8)._(g8)._(h8)
                ._(a7)._(b7)._(c7)._(d7)._(e7)._(f7)._(g7)._(h7)
                ._(a6)._(b6)._(c6)._(d6)._(e6)._(f6)._(g6)._(h6)
                ._(a5)._(b5)._(c5)._(d5)._(e5)._(f5)._(g5)._(h5)
                ._(a4)._(b4)._(c4)._(d4)._(e4)._(f4)._(g4)._(h4)
                ._(a3)._(b3)._(c3)._(d3)._(e3).B(f3)._(g3)._(h3)
                ._(a2)._(b2).p(c2)._(d2)._(e2)._(f2)._(g2).r(h2)
                ._(a1)._(b1)._(c1)._(d1).K(e1)._(f1)._(g1)._(h1)
        ));

        ChessComponentView view = execCommand(movesBuilder, "c1r");
        assertEquals("c1R+", getLastMove(view));
    }
    @Test public void moveNotationForSimplePawnMoveWhenThePawnChangesWithCheckMate() {
        MovesBuilder movesBuilder = new MovesBuilder(null, initialPosition(BLACK, b->b
                ._(a8)._(b8)._(c8)._(d8)._(e8)._(f8)._(g8)._(h8)
                ._(a7)._(b7)._(c7)._(d7)._(e7)._(f7)._(g7)._(h7)
                ._(a6)._(b6)._(c6)._(d6)._(e6)._(f6)._(g6)._(h6)
                ._(a5)._(b5)._(c5)._(d5)._(e5)._(f5)._(g5)._(h5)
                ._(a4)._(b4)._(c4)._(d4)._(e4)._(f4)._(g4)._(h4)
                ._(a3)._(b3)._(c3)._(d3)._(e3)._(f3)._(g3)._(h3)
                ._(a2)._(b2).p(c2)._(d2)._(e2)._(f2)._(g2).r(h2)
                ._(a1)._(b1)._(c1)._(d1).K(e1)._(f1)._(g1)._(h1)
        ));

        ChessComponentView view = execCommand(movesBuilder, "c1r");
        assertEquals("c1R#", getLastMove(view));
    }
    @Test public void moveNotationForStalemate() {
        MovesBuilder movesBuilder = new MovesBuilder(null, initialPosition(BLACK, b->b
                ._(a8)._(b8)._(c8)._(d8)._(e8)._(f8)._(g8)._(h8)
                ._(a7)._(b7)._(c7)._(d7)._(e7)._(f7)._(g7)._(h7)
                ._(a6)._(b6)._(c6)._(d6)._(e6)._(f6)._(g6)._(h6)
                .K(a5)._(b5)._(c5).b(d5)._(e5)._(f5)._(g5)._(h5)
                ._(a4)._(b4)._(c4)._(d4)._(e4)._(f4)._(g4)._(h4)
                ._(a3).q(b3)._(c3)._(d3)._(e3)._(f3)._(g3)._(h3)
                ._(a2)._(b2)._(c2)._(d2)._(e2)._(f2)._(g2)._(h2)
                .k(a1)._(b1)._(c1)._(d1)._(e1)._(f1)._(g1)._(h1)
        ));

        ChessComponentView view = execCommand(movesBuilder, "bc4");
        assertEquals("Bc4=", getLastMove(view));
    }
    @Test public void moveNotationForSimplePawnMoveWhenThePawnCapturesAndChangesWithCheckMate() {
        MovesBuilder movesBuilder = new MovesBuilder(null, initialPosition(WHITE, b->b
                ._(a8)._(b8).k(c8)._(d8)._(e8)._(f8).n(g8)._(h8)
                .R(a7)._(b7)._(c7)._(d7)._(e7).P(f7)._(g7).P(h7)
                ._(a6)._(b6)._(c6)._(d6)._(e6)._(f6)._(g6)._(h6)
                ._(a5)._(b5)._(c5)._(d5)._(e5)._(f5)._(g5)._(h5)
                ._(a4)._(b4)._(c4)._(d4)._(e4)._(f4)._(g4)._(h4)
                ._(a3)._(b3)._(c3)._(d3)._(e3)._(f3)._(g3)._(h3)
                ._(a2)._(b2)._(c2)._(d2)._(e2)._(f2)._(g2)._(h2)
                ._(a1)._(b1)._(c1)._(d1)._(e1)._(f1)._(g1)._(h1)
        ));

        ChessComponentView view = execCommand(movesBuilder, "hg8q");
        assertEquals("hxg8Q#", getLastMove(view));
    }
    @Test public void moveNotationForEnPassantPawnMove() {
        MovesBuilder movesBuilder = new MovesBuilder(null, initialPosition(WHITE, b->b
                .k(a8)._(b8)._(c8)._(d8)._(e8)._(f8)._(g8)._(h8)
                ._(a7)._(b7)._(c7)._(d7)._(e7)._(f7)._(g7)._(h7)
                ._(a6)._(b6)._(c6)._(d6)._(e6)._(f6)._(g6)._(h6)
                ._(a5)._(b5)._(c5)._(d5)._(e5)._(f5)._(g5)._(h5)
                ._(a4)._(b4)._(c4).p(d4)._(e4)._(f4)._(g4)._(h4)
                ._(a3)._(b3)._(c3)._(d3)._(e3)._(f3)._(g3)._(h3)
                ._(a2)._(b2)._(c2)._(d2).P(e2)._(f2)._(g2)._(h2)
                .K(a1)._(b1)._(c1)._(d1)._(e1)._(f1)._(g1)._(h1)
        ));

        movesBuilder.cellLeftClicked(e2);
        movesBuilder.cellLeftClicked(e4);
        ChessComponentView view = execCommand(movesBuilder, "e3");
        assertEquals("e3e.p.", getLastMove(view));
    }
    @Test public void moveNotationIndicatesUniqueXCoordFromWhenItIsAmbiguous() {
        MovesBuilder movesBuilder = new MovesBuilder(null, initialPosition(BLACK, b->b
                .k(a8)._(b8)._(c8)._(d8)._(e8)._(f8)._(g8)._(h8)
                ._(a7)._(b7)._(c7)._(d7).n(e7)._(f7)._(g7)._(h7)
                ._(a6)._(b6)._(c6)._(d6)._(e6)._(f6)._(g6)._(h6)
                ._(a5)._(b5)._(c5).P(d5)._(e5)._(f5)._(g5)._(h5)
                ._(a4)._(b4)._(c4)._(d4)._(e4)._(f4)._(g4)._(h4)
                ._(a3)._(b3).n(c3)._(d3)._(e3)._(f3)._(g3)._(h3)
                ._(a2)._(b2)._(c2)._(d2)._(e2)._(f2)._(g2)._(h2)
                .K(a1)._(b1)._(c1)._(d1)._(e1)._(f1)._(g1)._(h1)
        ));

        ChessComponentView view = execCommand(movesBuilder, "ncd5");
        assertNull(view.getCommandErrorMsg());
        assertEquals("Ncxd5", getLastMove(view));
    }
    @Test public void moveNotationIndicatesUniqueYCoordFromWhenItIsAmbiguous() {
        MovesBuilder movesBuilder = new MovesBuilder(null, initialPosition(BLACK, b->b
                .k(a8)._(b8)._(c8)._(d8)._(e8)._(f8)._(g8)._(h8)
                ._(a7)._(b7).n(c7)._(d7)._(e7)._(f7)._(g7)._(h7)
                ._(a6)._(b6)._(c6)._(d6)._(e6)._(f6)._(g6)._(h6)
                ._(a5)._(b5)._(c5).P(d5)._(e5)._(f5)._(g5)._(h5)
                ._(a4)._(b4)._(c4)._(d4)._(e4)._(f4)._(g4)._(h4)
                ._(a3)._(b3).n(c3)._(d3)._(e3)._(f3)._(g3)._(h3)
                ._(a2)._(b2)._(c2)._(d2)._(e2)._(f2)._(g2)._(h2)
                .K(a1)._(b1)._(c1)._(d1)._(e1)._(f1)._(g1)._(h1)
        ));

        ChessComponentView view = execCommand(movesBuilder, "n3d5");
        assertEquals("N3xd5", getLastMove(view));
    }
    @Test public void moveNotationDoesntIndicateCoordFromWhenThereAreFewPiecesButTheyAreOfDifferentTypes() {
        MovesBuilder movesBuilder = new MovesBuilder(null, initialPosition(BLACK, b->b
                .k(a8)._(b8)._(c8)._(d8)._(e8)._(f8)._(g8)._(h8)
                ._(a7)._(b7)._(c7)._(d7)._(e7)._(f7)._(g7)._(h7)
                ._(a6)._(b6).b(c6)._(d6)._(e6)._(f6)._(g6)._(h6)
                ._(a5)._(b5)._(c5).P(d5)._(e5)._(f5)._(g5)._(h5)
                ._(a4)._(b4).q(c4)._(d4)._(e4)._(f4)._(g4)._(h4)
                ._(a3)._(b3)._(c3)._(d3)._(e3)._(f3)._(g3)._(h3)
                ._(a2)._(b2)._(c2)._(d2)._(e2)._(f2)._(g2)._(h2)
                .K(a1)._(b1)._(c1)._(d1)._(e1)._(f1)._(g1)._(h1)
        ));

        ChessComponentView view = execCommand(movesBuilder, "qd5");
        assertEquals("Qxd5", getLastMove(view));
    }
    @Test public void moveNotationForShortCastling() {
        MovesBuilder movesBuilder = new MovesBuilder(null, initialPosition(WHITE, b->b
                ._(a8)._(b8)._(c8)._(d8)._(e8).k(f8)._(g8)._(h8)
                ._(a7)._(b7)._(c7)._(d7)._(e7)._(f7)._(g7)._(h7)
                ._(a6)._(b6)._(c6)._(d6)._(e6)._(f6)._(g6)._(h6)
                ._(a5)._(b5)._(c5)._(d5)._(e5)._(f5)._(g5)._(h5)
                ._(a4)._(b4)._(c4)._(d4)._(e4)._(f4)._(g4)._(h4)
                ._(a3)._(b3)._(c3)._(d3)._(e3)._(f3)._(g3)._(h3)
                ._(a2)._(b2)._(c2)._(d2)._(e2)._(f2)._(g2)._(h2)
                ._(a1)._(b1)._(c1)._(d1).K(e1)._(f1)._(g1).R(h1)
        ));

        ChessComponentView view = execCommand(movesBuilder, "kg1");
        assertEquals("0-0", getLastMove(view));
    }
    @Test public void moveNotationForLongCastling() {
        MovesBuilder movesBuilder = new MovesBuilder(null, initialPosition(BLACK, b->b
                .r(a8)._(b8)._(c8)._(d8).k(e8)._(f8)._(g8)._(h8)
                ._(a7)._(b7)._(c7)._(d7)._(e7)._(f7)._(g7)._(h7)
                ._(a6)._(b6)._(c6)._(d6)._(e6)._(f6)._(g6)._(h6)
                ._(a5)._(b5)._(c5)._(d5)._(e5)._(f5)._(g5)._(h5)
                ._(a4)._(b4)._(c4)._(d4)._(e4)._(f4)._(g4)._(h4)
                ._(a3)._(b3)._(c3)._(d3)._(e3)._(f3)._(g3)._(h3)
                ._(a2)._(b2)._(c2)._(d2)._(e2)._(f2)._(g2)._(h2)
                ._(a1).K(b1)._(c1)._(d1)._(e1)._(f1)._(g1)._(h1)
        ));

        ChessComponentView view = execCommand(movesBuilder, "kc8");
        assertEquals("0-0-0", getLastMove(view));
    }
    @Test public void makeMoveFailsForIncorrecltyFormattedCommand() {
        MovesBuilder movesBuilder = new MovesBuilder(null, initialPosition(WHITE, b->b
                ._(a8)._(b8)._(c8)._(d8).k(e8)._(f8)._(g8)._(h8)
                ._(a7)._(b7)._(c7)._(d7)._(e7)._(f7)._(g7)._(h7)
                ._(a6)._(b6)._(c6)._(d6)._(e6)._(f6)._(g6)._(h6)
                ._(a5)._(b5)._(c5)._(d5)._(e5)._(f5)._(g5)._(h5)
                ._(a4)._(b4)._(c4)._(d4)._(e4)._(f4)._(g4)._(h4)
                ._(a3)._(b3)._(c3)._(d3)._(e3)._(f3)._(g3)._(h3)
                ._(a2)._(b2)._(c2)._(d2)._(e2)._(f2)._(g2)._(h2)
                ._(a1)._(b1)._(c1)._(d1).K(e1)._(f1)._(g1)._(h1)
        ));

        ChessComponentView view = execCommand(movesBuilder, "k");
        assertEquals("'k' - could not recognize move notation format.", view.getCommandErrorMsg());
    }
    @Test public void makeMoveFailsIfSpecifiedPieceIsNotPresentOnTheBoard() {
        MovesBuilder movesBuilder = new MovesBuilder(null, initialPosition(WHITE, b->b
                ._(a8)._(b8)._(c8)._(d8).k(e8)._(f8)._(g8)._(h8)
                ._(a7)._(b7)._(c7)._(d7)._(e7)._(f7)._(g7)._(h7)
                ._(a6)._(b6)._(c6)._(d6)._(e6)._(f6)._(g6)._(h6)
                ._(a5)._(b5)._(c5)._(d5)._(e5)._(f5)._(g5)._(h5)
                ._(a4)._(b4)._(c4)._(d4)._(e4)._(f4)._(g4)._(h4)
                ._(a3)._(b3)._(c3)._(d3)._(e3)._(f3)._(g3)._(h3)
                ._(a2)._(b2)._(c2)._(d2)._(e2)._(f2)._(g2)._(h2)
                ._(a1)._(b1)._(c1)._(d1).K(e1)._(f1)._(g1)._(h1)
        ));

        ChessComponentView view = execCommand(movesBuilder, "ng4");
        assertEquals("'ng4' - cannot find specified piece to move.", view.getCommandErrorMsg());
    }
    @Test public void makeMoveFailsIfSpecifiedPieceIsPresentOnTheBoardButItsCoordinateIsSpecifiedIncorrectly() {
        MovesBuilder movesBuilder = new MovesBuilder(null, initialPosition(WHITE, b->b
                ._(a8)._(b8)._(c8)._(d8).k(e8)._(f8)._(g8)._(h8)
                ._(a7)._(b7)._(c7)._(d7)._(e7)._(f7)._(g7)._(h7)
                ._(a6)._(b6)._(c6)._(d6)._(e6)._(f6)._(g6)._(h6)
                ._(a5)._(b5)._(c5)._(d5)._(e5)._(f5)._(g5)._(h5)
                ._(a4)._(b4).N(c4)._(d4)._(e4)._(f4)._(g4)._(h4)
                ._(a3)._(b3)._(c3)._(d3)._(e3)._(f3)._(g3)._(h3)
                ._(a2)._(b2)._(c2)._(d2)._(e2)._(f2)._(g2)._(h2)
                ._(a1)._(b1)._(c1)._(d1).K(e1)._(f1)._(g1)._(h1)
        ));

        ChessComponentView view = execCommand(movesBuilder, "nbd6");
        assertEquals("'nbd6' - cannot find specified piece to move.", view.getCommandErrorMsg());
    }
    @Test public void makeMoveFailsIfThereAreMoreThanOnePieceAbleToDoSpecifiedMove() {
        MovesBuilder movesBuilder = new MovesBuilder(null, initialPosition(WHITE, b->b
                ._(a8)._(b8)._(c8)._(d8).k(e8)._(f8)._(g8)._(h8)
                ._(a7)._(b7)._(c7)._(d7)._(e7)._(f7)._(g7)._(h7)
                ._(a6)._(b6)._(c6)._(d6)._(e6)._(f6)._(g6)._(h6)
                ._(a5)._(b5)._(c5)._(d5)._(e5)._(f5)._(g5)._(h5)
                ._(a4).N(b4)._(c4).N(d4)._(e4)._(f4)._(g4)._(h4)
                ._(a3)._(b3)._(c3)._(d3)._(e3)._(f3)._(g3)._(h3)
                ._(a2)._(b2)._(c2)._(d2)._(e2)._(f2)._(g2)._(h2)
                ._(a1)._(b1)._(c1)._(d1).K(e1)._(f1)._(g1)._(h1)
        ));

        ChessComponentView view = execCommand(movesBuilder, "nc6");
        assertEquals("Move is ambiguously specified.", view.getCommandErrorMsg());
    }
    @Test public void makeMoveFailsIfAPawnShouldBeReplacedButReplacementIsNotSpecified() {
        MovesBuilder movesBuilder = new MovesBuilder(null, initialPosition(WHITE, b->b
                ._(a8)._(b8)._(c8)._(d8).k(e8)._(f8)._(g8)._(h8)
                ._(a7)._(b7)._(c7)._(d7)._(e7)._(f7).P(g7)._(h7)
                ._(a6)._(b6)._(c6)._(d6)._(e6)._(f6)._(g6)._(h6)
                ._(a5)._(b5)._(c5)._(d5)._(e5)._(f5)._(g5)._(h5)
                ._(a4)._(b4)._(c4)._(d4)._(e4)._(f4)._(g4)._(h4)
                ._(a3)._(b3)._(c3)._(d3)._(e3)._(f3)._(g3)._(h3)
                ._(a2)._(b2)._(c2)._(d2)._(e2)._(f2)._(g2)._(h2)
                ._(a1)._(b1)._(c1)._(d1).K(e1)._(f1)._(g1)._(h1)
        ));

        ChessComponentView view = execCommand(movesBuilder, "g8");
        assertEquals("'g8' - replacement is not specified.", view.getCommandErrorMsg());
    }
    @Test public void makeMoveErrorMessageDisappearsAfterTheCommandIsCorrected() {
        MovesBuilder movesBuilder = new MovesBuilder(null, initialPosition(WHITE, b->b
                ._(a8)._(b8)._(c8)._(d8).k(e8)._(f8)._(g8)._(h8)
                ._(a7)._(b7)._(c7)._(d7)._(e7)._(f7).P(g7)._(h7)
                ._(a6)._(b6)._(c6)._(d6)._(e6)._(f6)._(g6)._(h6)
                ._(a5)._(b5)._(c5)._(d5)._(e5)._(f5)._(g5)._(h5)
                ._(a4)._(b4)._(c4)._(d4)._(e4)._(f4)._(g4)._(h4)
                ._(a3)._(b3)._(c3)._(d3)._(e3)._(f3)._(g3)._(h3)
                ._(a2)._(b2)._(c2)._(d2)._(e2)._(f2)._(g2)._(h2)
                ._(a1)._(b1)._(c1)._(d1).K(e1)._(f1)._(g1)._(h1)
        ));

        ChessComponentView view = execCommand(movesBuilder, "g8q");
        assertNull(view.getCommandErrorMsg());
        assertEquals("g8Q+", getLastMove(view));
    }
    @Test public void historyNavigationWorksCorrectly() {
        MovesBuilder movesBuilder = new MovesBuilder(null, initialPosition(WHITE, b->b
                ._(a8)._(b8)._(c8)._(d8).k(e8)._(f8)._(g8)._(h8)
                ._(a7)._(b7)._(c7)._(d7).p(e7)._(f7).P(g7)._(h7)
                ._(a6)._(b6)._(c6)._(d6)._(e6)._(f6)._(g6)._(h6)
                ._(a5)._(b5)._(c5)._(d5)._(e5)._(f5)._(g5)._(h5)
                ._(a4)._(b4)._(c4)._(d4)._(e4)._(f4)._(g4)._(h4)
                ._(a3)._(b3)._(c3)._(d3)._(e3)._(f3)._(g3)._(h3)
                ._(a2)._(b2)._(c2)._(d2)._(e2).P(f2)._(g2)._(h2)
                ._(a1)._(b1)._(c1)._(d1).K(e1)._(f1)._(g1)._(h1)
        ));

        //1w
        ChessComponentView view = execCommand(movesBuilder, "f4");
        assertNull(view.getCommandErrorMsg());
        assertEquals("f4", getSelectedMove(view));
        assertEqualsByChessmenTypes(chessBoardBuilder()
                ._(a8)._(b8)._(c8)._(d8).k(e8)._(f8)._(g8)._(h8)
                ._(a7)._(b7)._(c7)._(d7).p(e7)._(f7).P(g7)._(h7)
                ._(a6)._(b6)._(c6)._(d6)._(e6)._(f6)._(g6)._(h6)
                ._(a5)._(b5)._(c5)._(d5)._(e5)._(f5)._(g5)._(h5)
                ._(a4)._(b4)._(c4)._(d4)._(e4).P(f4)._(g4)._(h4)
                ._(a3)._(b3)._(c3)._(d3)._(e3)._(f3)._(g3)._(h3)
                ._(a2)._(b2)._(c2)._(d2)._(e2)._(f2)._(g2)._(h2)
                ._(a1)._(b1)._(c1)._(d1).K(e1)._(f1)._(g1)._(h1)
                .build(), view
        );

        //1b
        view = execCommand(movesBuilder, "e5");
        assertNull(view.getCommandErrorMsg());
        assertEquals("e5", getSelectedMove(view));
        assertEqualsByChessmenTypes(chessBoardBuilder()
                ._(a8)._(b8)._(c8)._(d8).k(e8)._(f8)._(g8)._(h8)
                ._(a7)._(b7)._(c7)._(d7)._(e7)._(f7).P(g7)._(h7)
                ._(a6)._(b6)._(c6)._(d6)._(e6)._(f6)._(g6)._(h6)
                ._(a5)._(b5)._(c5)._(d5).p(e5)._(f5)._(g5)._(h5)
                ._(a4)._(b4)._(c4)._(d4)._(e4).P(f4)._(g4)._(h4)
                ._(a3)._(b3)._(c3)._(d3)._(e3)._(f3)._(g3)._(h3)
                ._(a2)._(b2)._(c2)._(d2)._(e2)._(f2)._(g2)._(h2)
                ._(a1)._(b1)._(c1)._(d1).K(e1)._(f1)._(g1)._(h1)
                .build(), view
        );

        //2w
        view = execCommand(movesBuilder, "e5");
        assertNull(view.getCommandErrorMsg());
        assertEquals("xe5", getSelectedMove(view));
        assertEqualsByChessmenTypes(chessBoardBuilder()
                ._(a8)._(b8)._(c8)._(d8).k(e8)._(f8)._(g8)._(h8)
                ._(a7)._(b7)._(c7)._(d7)._(e7)._(f7).P(g7)._(h7)
                ._(a6)._(b6)._(c6)._(d6)._(e6)._(f6)._(g6)._(h6)
                ._(a5)._(b5)._(c5)._(d5).P(e5)._(f5)._(g5)._(h5)
                ._(a4)._(b4)._(c4)._(d4)._(e4)._(f4)._(g4)._(h4)
                ._(a3)._(b3)._(c3)._(d3)._(e3)._(f3)._(g3)._(h3)
                ._(a2)._(b2)._(c2)._(d2)._(e2)._(f2)._(g2)._(h2)
                ._(a1)._(b1)._(c1)._(d1).K(e1)._(f1)._(g1)._(h1)
                .build(), view
        );

        //2b
        view = execCommand(movesBuilder, "ke7");
        assertNull(view.getCommandErrorMsg());
        assertEquals("Ke7", getSelectedMove(view));
        assertEqualsByChessmenTypes(chessBoardBuilder()
                ._(a8)._(b8)._(c8)._(d8)._(e8)._(f8)._(g8)._(h8)
                ._(a7)._(b7)._(c7)._(d7).k(e7)._(f7).P(g7)._(h7)
                ._(a6)._(b6)._(c6)._(d6)._(e6)._(f6)._(g6)._(h6)
                ._(a5)._(b5)._(c5)._(d5).P(e5)._(f5)._(g5)._(h5)
                ._(a4)._(b4)._(c4)._(d4)._(e4)._(f4)._(g4)._(h4)
                ._(a3)._(b3)._(c3)._(d3)._(e3)._(f3)._(g3)._(h3)
                ._(a2)._(b2)._(c2)._(d2)._(e2)._(f2)._(g2)._(h2)
                ._(a1)._(b1)._(c1)._(d1).K(e1)._(f1)._(g1)._(h1)
                .build(), view
        );

        //3w
        view = execCommand(movesBuilder, "e6");
        assertNull(view.getCommandErrorMsg());
        assertEquals("e6", getSelectedMove(view));
        assertEqualsByChessmenTypes(chessBoardBuilder()
                ._(a8)._(b8)._(c8)._(d8)._(e8)._(f8)._(g8)._(h8)
                ._(a7)._(b7)._(c7)._(d7).k(e7)._(f7).P(g7)._(h7)
                ._(a6)._(b6)._(c6)._(d6).P(e6)._(f6)._(g6)._(h6)
                ._(a5)._(b5)._(c5)._(d5)._(e5)._(f5)._(g5)._(h5)
                ._(a4)._(b4)._(c4)._(d4)._(e4)._(f4)._(g4)._(h4)
                ._(a3)._(b3)._(c3)._(d3)._(e3)._(f3)._(g3)._(h3)
                ._(a2)._(b2)._(c2)._(d2)._(e2)._(f2)._(g2)._(h2)
                ._(a1)._(b1)._(c1)._(d1).K(e1)._(f1)._(g1)._(h1)
                .build(), view
        );

        //g 1w
        view = execCommand(movesBuilder, "g 1w");
        assertNull(view.getCommandErrorMsg());
        assertEquals("f4", getSelectedMove(view));
        assertEqualsByChessmenTypes(chessBoardBuilder()
                ._(a8)._(b8)._(c8)._(d8).k(e8)._(f8)._(g8)._(h8)
                ._(a7)._(b7)._(c7)._(d7).p(e7)._(f7).P(g7)._(h7)
                ._(a6)._(b6)._(c6)._(d6)._(e6)._(f6)._(g6)._(h6)
                ._(a5)._(b5)._(c5)._(d5)._(e5)._(f5)._(g5)._(h5)
                ._(a4)._(b4)._(c4)._(d4)._(e4).P(f4)._(g4)._(h4)
                ._(a3)._(b3)._(c3)._(d3)._(e3)._(f3)._(g3)._(h3)
                ._(a2)._(b2)._(c2)._(d2)._(e2)._(f2)._(g2)._(h2)
                ._(a1)._(b1)._(c1)._(d1).K(e1)._(f1)._(g1)._(h1)
                .build(), view
        );

        //p
        view = execCommand(movesBuilder, "p");
        assertNull(view.getCommandErrorMsg());
        assertEqualsByChessmenTypes(chessBoardBuilder()
                ._(a8)._(b8)._(c8)._(d8).k(e8)._(f8)._(g8)._(h8)
                ._(a7)._(b7)._(c7)._(d7).p(e7)._(f7).P(g7)._(h7)
                ._(a6)._(b6)._(c6)._(d6)._(e6)._(f6)._(g6)._(h6)
                ._(a5)._(b5)._(c5)._(d5)._(e5)._(f5)._(g5)._(h5)
                ._(a4)._(b4)._(c4)._(d4)._(e4)._(f4)._(g4)._(h4)
                ._(a3)._(b3)._(c3)._(d3)._(e3)._(f3)._(g3)._(h3)
                ._(a2)._(b2)._(c2)._(d2)._(e2).P(f2)._(g2)._(h2)
                ._(a1)._(b1)._(c1)._(d1).K(e1)._(f1)._(g1)._(h1)
                .build(), view
        );

        //n
        view = execCommand(movesBuilder, "n");
        assertNull(view.getCommandErrorMsg());
        assertEquals("f4", getSelectedMove(view));
        assertEqualsByChessmenTypes(chessBoardBuilder()
                ._(a8)._(b8)._(c8)._(d8).k(e8)._(f8)._(g8)._(h8)
                ._(a7)._(b7)._(c7)._(d7).p(e7)._(f7).P(g7)._(h7)
                ._(a6)._(b6)._(c6)._(d6)._(e6)._(f6)._(g6)._(h6)
                ._(a5)._(b5)._(c5)._(d5)._(e5)._(f5)._(g5)._(h5)
                ._(a4)._(b4)._(c4)._(d4)._(e4).P(f4)._(g4)._(h4)
                ._(a3)._(b3)._(c3)._(d3)._(e3)._(f3)._(g3)._(h3)
                ._(a2)._(b2)._(c2)._(d2)._(e2)._(f2)._(g2)._(h2)
                ._(a1)._(b1)._(c1)._(d1).K(e1)._(f1)._(g1)._(h1)
                .build(), view
        );

        //n
        view = execCommand(movesBuilder, "n");
        assertNull(view.getCommandErrorMsg());
        assertEquals("e5", getSelectedMove(view));
        assertEqualsByChessmenTypes(chessBoardBuilder()
                ._(a8)._(b8)._(c8)._(d8).k(e8)._(f8)._(g8)._(h8)
                ._(a7)._(b7)._(c7)._(d7)._(e7)._(f7).P(g7)._(h7)
                ._(a6)._(b6)._(c6)._(d6)._(e6)._(f6)._(g6)._(h6)
                ._(a5)._(b5)._(c5)._(d5).p(e5)._(f5)._(g5)._(h5)
                ._(a4)._(b4)._(c4)._(d4)._(e4).P(f4)._(g4)._(h4)
                ._(a3)._(b3)._(c3)._(d3)._(e3)._(f3)._(g3)._(h3)
                ._(a2)._(b2)._(c2)._(d2)._(e2)._(f2)._(g2)._(h2)
                ._(a1)._(b1)._(c1)._(d1).K(e1)._(f1)._(g1)._(h1)
                .build(), view
        );

        //e
        view = execCommand(movesBuilder, "e");
        assertNull(view.getCommandErrorMsg());
        assertEquals("e6", getSelectedMove(view));
        assertEqualsByChessmenTypes(chessBoardBuilder()
                ._(a8)._(b8)._(c8)._(d8)._(e8)._(f8)._(g8)._(h8)
                ._(a7)._(b7)._(c7)._(d7).k(e7)._(f7).P(g7)._(h7)
                ._(a6)._(b6)._(c6)._(d6).P(e6)._(f6)._(g6)._(h6)
                ._(a5)._(b5)._(c5)._(d5)._(e5)._(f5)._(g5)._(h5)
                ._(a4)._(b4)._(c4)._(d4)._(e4)._(f4)._(g4)._(h4)
                ._(a3)._(b3)._(c3)._(d3)._(e3)._(f3)._(g3)._(h3)
                ._(a2)._(b2)._(c2)._(d2)._(e2)._(f2)._(g2)._(h2)
                ._(a1)._(b1)._(c1)._(d1).K(e1)._(f1)._(g1)._(h1)
                .build(), view
        );

        //g 1b
        view = execCommand(movesBuilder, "g 1b");
        assertNull(view.getCommandErrorMsg());
        assertEquals("e5", getSelectedMove(view));
        assertEqualsByChessmenTypes(chessBoardBuilder()
                ._(a8)._(b8)._(c8)._(d8).k(e8)._(f8)._(g8)._(h8)
                ._(a7)._(b7)._(c7)._(d7)._(e7)._(f7).P(g7)._(h7)
                ._(a6)._(b6)._(c6)._(d6)._(e6)._(f6)._(g6)._(h6)
                ._(a5)._(b5)._(c5)._(d5).p(e5)._(f5)._(g5)._(h5)
                ._(a4)._(b4)._(c4)._(d4)._(e4).P(f4)._(g4)._(h4)
                ._(a3)._(b3)._(c3)._(d3)._(e3)._(f3)._(g3)._(h3)
                ._(a2)._(b2)._(c2)._(d2)._(e2)._(f2)._(g2)._(h2)
                ._(a1)._(b1)._(c1)._(d1).K(e1)._(f1)._(g1)._(h1)
                .build(), view
        );

        //rr
        view = execCommand(movesBuilder, "rr");
        assertNull(view.getCommandErrorMsg());
        assertEquals("e5", getSelectedMove(view));
        assertEqualsByChessmenTypes(chessBoardBuilder()
                ._(a8)._(b8)._(c8)._(d8).k(e8)._(f8)._(g8)._(h8)
                ._(a7)._(b7)._(c7)._(d7)._(e7)._(f7).P(g7)._(h7)
                ._(a6)._(b6)._(c6)._(d6)._(e6)._(f6)._(g6)._(h6)
                ._(a5)._(b5)._(c5)._(d5).p(e5)._(f5)._(g5)._(h5)
                ._(a4)._(b4)._(c4)._(d4)._(e4).P(f4)._(g4)._(h4)
                ._(a3)._(b3)._(c3)._(d3)._(e3)._(f3)._(g3)._(h3)
                ._(a2)._(b2)._(c2)._(d2)._(e2)._(f2)._(g2)._(h2)
                ._(a1)._(b1)._(c1)._(d1).K(e1)._(f1)._(g1)._(h1)
                .build(), view
        );

        //s
        view = execCommand(movesBuilder, "s");
        assertNull(view.getCommandErrorMsg());
        assertEqualsByChessmenTypes(chessBoardBuilder()
                ._(a8)._(b8)._(c8)._(d8).k(e8)._(f8)._(g8)._(h8)
                ._(a7)._(b7)._(c7)._(d7).p(e7)._(f7).P(g7)._(h7)
                ._(a6)._(b6)._(c6)._(d6)._(e6)._(f6)._(g6)._(h6)
                ._(a5)._(b5)._(c5)._(d5)._(e5)._(f5)._(g5)._(h5)
                ._(a4)._(b4)._(c4)._(d4)._(e4)._(f4)._(g4)._(h4)
                ._(a3)._(b3)._(c3)._(d3)._(e3)._(f3)._(g3)._(h3)
                ._(a2)._(b2)._(c2)._(d2)._(e2).P(f2)._(g2)._(h2)
                ._(a1)._(b1)._(c1)._(d1).K(e1)._(f1)._(g1)._(h1)
                .build(), view
        );

        //n
        view = execCommand(movesBuilder, "n");
        assertNull(view.getCommandErrorMsg());
        assertEquals("f4", getSelectedMove(view));
        assertEqualsByChessmenTypes(chessBoardBuilder()
                ._(a8)._(b8)._(c8)._(d8).k(e8)._(f8)._(g8)._(h8)
                ._(a7)._(b7)._(c7)._(d7).p(e7)._(f7).P(g7)._(h7)
                ._(a6)._(b6)._(c6)._(d6)._(e6)._(f6)._(g6)._(h6)
                ._(a5)._(b5)._(c5)._(d5)._(e5)._(f5)._(g5)._(h5)
                ._(a4)._(b4)._(c4)._(d4)._(e4).P(f4)._(g4)._(h4)
                ._(a3)._(b3)._(c3)._(d3)._(e3)._(f3)._(g3)._(h3)
                ._(a2)._(b2)._(c2)._(d2)._(e2)._(f2)._(g2)._(h2)
                ._(a1)._(b1)._(c1)._(d1).K(e1)._(f1)._(g1)._(h1)
                .build(), view
        );

        //n
        view = execCommand(movesBuilder, "n");
        assertNull(view.getCommandErrorMsg());
        assertEquals("e5", getSelectedMove(view));
        assertEqualsByChessmenTypes(chessBoardBuilder()
                ._(a8)._(b8)._(c8)._(d8).k(e8)._(f8)._(g8)._(h8)
                ._(a7)._(b7)._(c7)._(d7)._(e7)._(f7).P(g7)._(h7)
                ._(a6)._(b6)._(c6)._(d6)._(e6)._(f6)._(g6)._(h6)
                ._(a5)._(b5)._(c5)._(d5).p(e5)._(f5)._(g5)._(h5)
                ._(a4)._(b4)._(c4)._(d4)._(e4).P(f4)._(g4)._(h4)
                ._(a3)._(b3)._(c3)._(d3)._(e3)._(f3)._(g3)._(h3)
                ._(a2)._(b2)._(c2)._(d2)._(e2)._(f2)._(g2)._(h2)
                ._(a1)._(b1)._(c1)._(d1).K(e1)._(f1)._(g1)._(h1)
                .build(), view
        );

        //n
        view = execCommand(movesBuilder, "n");
        assertNull(view.getCommandErrorMsg());
        assertEquals("e5", getSelectedMove(view));
        assertEqualsByChessmenTypes(chessBoardBuilder()
                ._(a8)._(b8)._(c8)._(d8).k(e8)._(f8)._(g8)._(h8)
                ._(a7)._(b7)._(c7)._(d7)._(e7)._(f7).P(g7)._(h7)
                ._(a6)._(b6)._(c6)._(d6)._(e6)._(f6)._(g6)._(h6)
                ._(a5)._(b5)._(c5)._(d5).p(e5)._(f5)._(g5)._(h5)
                ._(a4)._(b4)._(c4)._(d4)._(e4).P(f4)._(g4)._(h4)
                ._(a3)._(b3)._(c3)._(d3)._(e3)._(f3)._(g3)._(h3)
                ._(a2)._(b2)._(c2)._(d2)._(e2)._(f2)._(g2)._(h2)
                ._(a1)._(b1)._(c1)._(d1).K(e1)._(f1)._(g1)._(h1)
                .build(), view
        );
    }
}