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
import static org.igye.outline2.controllers.chess.ChessTestUtils.assertNoCellsAvailableToMoveTo;
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
                .ur(a8).u_(b8).u_(c8).u_(d8).uk(e8).u_(f8).u_(g8).ur(h8)
                .u_(a7).up(b7).u_(c7).u_(d7).u_(e7).u_(f7).up(g7).u_(h7)
                .u_(a6).u_(b6).u_(c6).u_(d6).u_(e6).u_(f6).u_(g6).u_(h6)
                .u_(a5).u_(b5).u_(c5).u_(d5).u_(e5).u_(f5).u_(g5).u_(h5)
                .u_(a4).u_(b4).u_(c4).u_(d4).u_(e4).u_(f4).u_(g4).u_(h4)
                .u_(a3).u_(b3).u_(c3).u_(d3).u_(e3).u_(f3).u_(g3).u_(h3)
                .u_(a2).uP(b2).u_(c2).g_(d2).g_(e2).g_(f2).uP(g2).u_(h2)
                .uR(a1).u_(b1).g_(c1).g_(d1).yK(e1).g_(f1).g_(g1).uR(h1)
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
                .ur(a8).u_(b8).u_(c8).u_(d8).uk(e8).u_(f8).u_(g8).ur(h8)
                .u_(a7).u_(b7).u_(c7).u_(d7).u_(e7).u_(f7).up(g7).u_(h7)
                .u_(a6).u_(b6).u_(c6).u_(d6).u_(e6).u_(f6).u_(g6).u_(h6)
                .u_(a5).up(b5).u_(c5).u_(d5).u_(e5).u_(f5).u_(g5).u_(h5)
                .u_(a4).u_(b4).u_(c4).u_(d4).u_(e4).u_(f4).u_(g4).u_(h4)
                .u_(a3).u_(b3).u_(c3).u_(d3).u_(e3).u_(f3).u_(g3).u_(h3)
                .u_(a2).uP(b2).u_(c2).g_(d2).g_(e2).g_(f2).uP(g2).u_(h2)
                .uR(a1).u_(b1).u_(c1).g_(d1).yK(e1).g_(f1).u_(g1).uR(h1)
        ), view.getChessBoard());

        movesBuilder.cellLeftClicked(e2);
        view = movesBuilder.cellLeftClicked(e8);
        assertEquals(chessBoardView(b -> b
                .ur(a8).u_(b8).g_(c8).g_(d8).yk(e8).g_(f8).g_(g8).ur(h8)
                .u_(a7).u_(b7).u_(c7).g_(d7).g_(e7).g_(f7).up(g7).u_(h7)
                .u_(a6).u_(b6).u_(c6).u_(d6).u_(e6).u_(f6).u_(g6).u_(h6)
                .u_(a5).up(b5).u_(c5).u_(d5).u_(e5).u_(f5).u_(g5).u_(h5)
                .u_(a4).u_(b4).u_(c4).u_(d4).u_(e4).u_(f4).u_(g4).u_(h4)
                .u_(a3).u_(b3).u_(c3).u_(d3).u_(e3).u_(f3).u_(g3).u_(h3)
                .u_(a2).uP(b2).u_(c2).u_(d2).uK(e2).u_(f2).uP(g2).u_(h2)
                .uR(a1).u_(b1).u_(c1).u_(d1).u_(e1).u_(f1).u_(g1).uR(h1)
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
                .ur(a8).u_(b8).u_(c8).g_(d8).yk(e8).g_(f8).u_(g8).ur(h8)
                .u_(a7).u_(b7).u_(c7).g_(d7).g_(e7).g_(f7).up(g7).u_(h7)
                .u_(a6).u_(b6).u_(c6).u_(d6).u_(e6).u_(f6).u_(g6).u_(h6)
                .u_(a5).up(b5).u_(c5).u_(d5).u_(e5).u_(f5).u_(g5).u_(h5)
                .u_(a4).u_(b4).u_(c4).u_(d4).u_(e4).u_(f4).uP(g4).u_(h4)
                .u_(a3).u_(b3).u_(c3).u_(d3).u_(e3).u_(f3).u_(g3).u_(h3)
                .u_(a2).uP(b2).u_(c2).u_(d2).uK(e2).u_(f2).u_(g2).u_(h2)
                .uR(a1).u_(b1).u_(c1).u_(d1).u_(e1).u_(f1).u_(g1).uR(h1)
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
                .ur(a8).u_(b8).u_(c8).u_(d8).uk(e8).u_(f8).u_(g8).ur(h8)
                .u_(a7).up(b7).u_(c7).u_(d7).u_(e7).u_(f7).up(g7).u_(h7)
                .u_(a6).u_(b6).u_(c6).u_(d6).u_(e6).u_(f6).u_(g6).u_(h6)
                .u_(a5).u_(b5).u_(c5).u_(d5).u_(e5).u_(f5).u_(g5).u_(h5)
                .u_(a4).u_(b4).u_(c4).u_(d4).u_(e4).u_(f4).u_(g4).u_(h4)
                .u_(a3).u_(b3).u_(c3).u_(d3).u_(e3).u_(f3).u_(g3).u_(h3)
                .u_(a2).uP(b2).u_(c2).g_(d2).g_(e2).g_(f2).uP(g2).u_(h2)
                .uR(a1).u_(b1).g_(c1).g_(d1).yK(e1).g_(f1).g_(g1).uR(h1)
        ), view.getChessBoard());

        movesBuilder.cellLeftClicked(a1);
        movesBuilder.cellLeftClicked(a2);
        movesBuilder.cellLeftClicked(b7);
        movesBuilder.cellLeftClicked(b6);
        view = movesBuilder.cellLeftClicked(e1);
        assertEquals(chessBoardView(b -> b
                .ur(a8).u_(b8).u_(c8).u_(d8).uk(e8).u_(f8).u_(g8).ur(h8)
                .u_(a7).u_(b7).u_(c7).u_(d7).u_(e7).u_(f7).up(g7).u_(h7)
                .u_(a6).up(b6).u_(c6).u_(d6).u_(e6).u_(f6).u_(g6).u_(h6)
                .u_(a5).u_(b5).u_(c5).u_(d5).u_(e5).u_(f5).u_(g5).u_(h5)
                .u_(a4).u_(b4).u_(c4).u_(d4).u_(e4).u_(f4).u_(g4).u_(h4)
                .u_(a3).u_(b3).u_(c3).u_(d3).u_(e3).u_(f3).u_(g3).u_(h3)
                .uR(a2).uP(b2).u_(c2).g_(d2).g_(e2).g_(f2).uP(g2).u_(h2)
                .u_(a1).u_(b1).u_(c1).g_(d1).yK(e1).g_(f1).g_(g1).uR(h1)
        ), view.getChessBoard());

        movesBuilder.cellLeftClicked(h1);
        movesBuilder.cellLeftClicked(h2);
        movesBuilder.cellLeftClicked(b6);
        movesBuilder.cellLeftClicked(b5);
        view = movesBuilder.cellLeftClicked(e1);
        assertEquals(chessBoardView(b -> b
                .ur(a8).u_(b8).u_(c8).u_(d8).uk(e8).u_(f8).u_(g8).ur(h8)
                .u_(a7).u_(b7).u_(c7).u_(d7).u_(e7).u_(f7).up(g7).u_(h7)
                .u_(a6).u_(b6).u_(c6).u_(d6).u_(e6).u_(f6).u_(g6).u_(h6)
                .u_(a5).up(b5).u_(c5).u_(d5).u_(e5).u_(f5).u_(g5).u_(h5)
                .u_(a4).u_(b4).u_(c4).u_(d4).u_(e4).u_(f4).u_(g4).u_(h4)
                .u_(a3).u_(b3).u_(c3).u_(d3).u_(e3).u_(f3).u_(g3).u_(h3)
                .uR(a2).uP(b2).u_(c2).g_(d2).g_(e2).g_(f2).uP(g2).uR(h2)
                .u_(a1).u_(b1).u_(c1).g_(d1).yK(e1).g_(f1).u_(g1).u_(h1)
        ), view.getChessBoard());

        movesBuilder.cellLeftClicked(b2);
        movesBuilder.cellLeftClicked(b3);
        view = movesBuilder.cellLeftClicked(e8);
        assertEquals(chessBoardView(b -> b
                .ur(a8).u_(b8).g_(c8).g_(d8).yk(e8).g_(f8).g_(g8).ur(h8)
                .u_(a7).u_(b7).u_(c7).g_(d7).g_(e7).g_(f7).up(g7).u_(h7)
                .u_(a6).u_(b6).u_(c6).u_(d6).u_(e6).u_(f6).u_(g6).u_(h6)
                .u_(a5).up(b5).u_(c5).u_(d5).u_(e5).u_(f5).u_(g5).u_(h5)
                .u_(a4).u_(b4).u_(c4).u_(d4).u_(e4).u_(f4).u_(g4).u_(h4)
                .u_(a3).uP(b3).u_(c3).u_(d3).u_(e3).u_(f3).u_(g3).u_(h3)
                .uR(a2).u_(b2).u_(c2).u_(d2).u_(e2).u_(f2).uP(g2).uR(h2)
                .u_(a1).u_(b1).u_(c1).u_(d1).uK(e1).u_(f1).u_(g1).u_(h1)
        ), view.getChessBoard());

        movesBuilder.cellLeftClicked(a8);
        movesBuilder.cellLeftClicked(a7);
        movesBuilder.cellLeftClicked(b3);
        movesBuilder.cellLeftClicked(b4);
        view = movesBuilder.cellLeftClicked(e8);
        assertEquals(chessBoardView(b -> b
                .u_(a8).u_(b8).u_(c8).g_(d8).yk(e8).g_(f8).g_(g8).ur(h8)
                .ur(a7).u_(b7).u_(c7).g_(d7).g_(e7).g_(f7).up(g7).u_(h7)
                .u_(a6).u_(b6).u_(c6).u_(d6).u_(e6).u_(f6).u_(g6).u_(h6)
                .u_(a5).up(b5).u_(c5).u_(d5).u_(e5).u_(f5).u_(g5).u_(h5)
                .u_(a4).uP(b4).u_(c4).u_(d4).u_(e4).u_(f4).u_(g4).u_(h4)
                .u_(a3).u_(b3).u_(c3).u_(d3).u_(e3).u_(f3).u_(g3).u_(h3)
                .uR(a2).u_(b2).u_(c2).u_(d2).u_(e2).u_(f2).uP(g2).uR(h2)
                .u_(a1).u_(b1).u_(c1).u_(d1).uK(e1).u_(f1).u_(g1).u_(h1)
        ), view.getChessBoard());

        movesBuilder.cellLeftClicked(h8);
        movesBuilder.cellLeftClicked(h7);
        movesBuilder.cellLeftClicked(g2);
        movesBuilder.cellLeftClicked(g4);
        view = movesBuilder.cellLeftClicked(e8);
        assertEquals(chessBoardView(b -> b
                .u_(a8).u_(b8).u_(c8).g_(d8).yk(e8).g_(f8).u_(g8).u_(h8)
                .ur(a7).u_(b7).u_(c7).g_(d7).g_(e7).g_(f7).up(g7).ur(h7)
                .u_(a6).u_(b6).u_(c6).u_(d6).u_(e6).u_(f6).u_(g6).u_(h6)
                .u_(a5).up(b5).u_(c5).u_(d5).u_(e5).u_(f5).u_(g5).u_(h5)
                .u_(a4).uP(b4).u_(c4).u_(d4).u_(e4).u_(f4).uP(g4).u_(h4)
                .u_(a3).u_(b3).u_(c3).u_(d3).u_(e3).u_(f3).u_(g3).u_(h3)
                .uR(a2).u_(b2).u_(c2).u_(d2).u_(e2).u_(f2).u_(g2).uR(h2)
                .u_(a1).u_(b1).u_(c1).u_(d1).uK(e1).u_(f1).u_(g1).u_(h1)
        ), view.getChessBoard());
    }
    @Test public void test_enPasssantIsDeterminedCorrectlyForWhiteLeft() {
        //given
        MovesBuilder movesBuilder = new MovesBuilder(initialPosition(BLACK, b->b
                .p(d7).p(f6).P(e5)
        ));

        ChessComponentView view = movesBuilder.cellLeftClicked(d7);
        assertEquals(chessBoardView(b -> b
                .u_(a8).u_(b8).u_(c8).u_(d8).u_(e8).u_(f8).u_(g8).u_(h8)
                .u_(a7).u_(b7).u_(c7).yp(d7).u_(e7).u_(f7).u_(g7).u_(h7)
                .u_(a6).u_(b6).u_(c6).g_(d6).u_(e6).up(f6).u_(g6).u_(h6)
                .u_(a5).u_(b5).u_(c5).g_(d5).uP(e5).u_(f5).u_(g5).u_(h5)
                .u_(a4).u_(b4).u_(c4).u_(d4).u_(e4).u_(f4).u_(g4).u_(h4)
                .u_(a3).u_(b3).u_(c3).u_(d3).u_(e3).u_(f3).u_(g3).u_(h3)
                .u_(a2).u_(b2).u_(c2).u_(d2).u_(e2).u_(f2).u_(g2).u_(h2)
                .u_(a1).u_(b1).u_(c1).u_(d1).u_(e1).u_(f1).u_(g1).u_(h1)
        ), view.getChessBoard());

        view = movesBuilder.cellLeftClicked(d5);
        assertEquals(chessBoardView(b -> b
                .u_(a8).u_(b8).u_(c8).u_(d8).u_(e8).u_(f8).u_(g8).u_(h8)
                .u_(a7).u_(b7).u_(c7).y_(d7).u_(e7).u_(f7).u_(g7).u_(h7)
                .u_(a6).u_(b6).u_(c6).u_(d6).u_(e6).up(f6).u_(g6).u_(h6)
                .u_(a5).u_(b5).u_(c5).gp(d5).uP(e5).u_(f5).u_(g5).u_(h5)
                .u_(a4).u_(b4).u_(c4).u_(d4).u_(e4).u_(f4).u_(g4).u_(h4)
                .u_(a3).u_(b3).u_(c3).u_(d3).u_(e3).u_(f3).u_(g3).u_(h3)
                .u_(a2).u_(b2).u_(c2).u_(d2).u_(e2).u_(f2).u_(g2).u_(h2)
                .u_(a1).u_(b1).u_(c1).u_(d1).u_(e1).u_(f1).u_(g1).u_(h1)
        ), view.getChessBoard());

        view = movesBuilder.cellLeftClicked(e5);
        assertEquals(chessBoardView(b -> b
                .u_(a8).u_(b8).u_(c8).u_(d8).u_(e8).u_(f8).u_(g8).u_(h8)
                .u_(a7).u_(b7).u_(c7).u_(d7).u_(e7).u_(f7).u_(g7).u_(h7)
                .u_(a6).u_(b6).u_(c6).g_(d6).g_(e6).gp(f6).u_(g6).u_(h6)
                .u_(a5).u_(b5).u_(c5).up(d5).yP(e5).u_(f5).u_(g5).u_(h5)
                .u_(a4).u_(b4).u_(c4).u_(d4).u_(e4).u_(f4).u_(g4).u_(h4)
                .u_(a3).u_(b3).u_(c3).u_(d3).u_(e3).u_(f3).u_(g3).u_(h3)
                .u_(a2).u_(b2).u_(c2).u_(d2).u_(e2).u_(f2).u_(g2).u_(h2)
                .u_(a1).u_(b1).u_(c1).u_(d1).u_(e1).u_(f1).u_(g1).u_(h1)
        ), view.getChessBoard());

        view = movesBuilder.cellLeftClicked(d6);
        assertEquals(chessBoardView(b -> b
                .u_(a8).u_(b8).u_(c8).u_(d8).u_(e8).u_(f8).u_(g8).u_(h8)
                .u_(a7).u_(b7).u_(c7).u_(d7).u_(e7).u_(f7).u_(g7).u_(h7)
                .u_(a6).u_(b6).u_(c6).gP(d6).u_(e6).up(f6).u_(g6).u_(h6)
                .u_(a5).u_(b5).u_(c5).u_(d5).y_(e5).u_(f5).u_(g5).u_(h5)
                .u_(a4).u_(b4).u_(c4).u_(d4).u_(e4).u_(f4).u_(g4).u_(h4)
                .u_(a3).u_(b3).u_(c3).u_(d3).u_(e3).u_(f3).u_(g3).u_(h3)
                .u_(a2).u_(b2).u_(c2).u_(d2).u_(e2).u_(f2).u_(g2).u_(h2)
                .u_(a1).u_(b1).u_(c1).u_(d1).u_(e1).u_(f1).u_(g1).u_(h1)
        ), view.getChessBoard());

        view = movesBuilder.cellLeftClicked(f6);
        assertEquals(chessBoardView(b -> b
                .u_(a8).u_(b8).u_(c8).u_(d8).u_(e8).u_(f8).u_(g8).u_(h8)
                .u_(a7).u_(b7).u_(c7).u_(d7).u_(e7).u_(f7).u_(g7).u_(h7)
                .u_(a6).u_(b6).u_(c6).uP(d6).u_(e6).yp(f6).u_(g6).u_(h6)
                .u_(a5).u_(b5).u_(c5).u_(d5).u_(e5).g_(f5).u_(g5).u_(h5)
                .u_(a4).u_(b4).u_(c4).u_(d4).u_(e4).u_(f4).u_(g4).u_(h4)
                .u_(a3).u_(b3).u_(c3).u_(d3).u_(e3).u_(f3).u_(g3).u_(h3)
                .u_(a2).u_(b2).u_(c2).u_(d2).u_(e2).u_(f2).u_(g2).u_(h2)
                .u_(a1).u_(b1).u_(c1).u_(d1).u_(e1).u_(f1).u_(g1).u_(h1)
        ), view.getChessBoard());
    }
    @Test public void test_enPasssantIsDeterminedCorrectlyForWhiteRight() {
        //given
        MovesBuilder movesBuilder = new MovesBuilder(initialPosition(BLACK, b->b
                .p(d6).p(f7).P(e5)
        ));

        ChessComponentView view = movesBuilder.cellLeftClicked(f7);
        assertEquals(chessBoardView(b -> b
                .u_(a8).u_(b8).u_(c8).u_(d8).u_(e8).u_(f8).u_(g8).u_(h8)
                .u_(a7).u_(b7).u_(c7).u_(d7).u_(e7).yp(f7).u_(g7).u_(h7)
                .u_(a6).u_(b6).u_(c6).up(d6).u_(e6).g_(f6).u_(g6).u_(h6)
                .u_(a5).u_(b5).u_(c5).u_(d5).uP(e5).g_(f5).u_(g5).u_(h5)
                .u_(a4).u_(b4).u_(c4).u_(d4).u_(e4).u_(f4).u_(g4).u_(h4)
                .u_(a3).u_(b3).u_(c3).u_(d3).u_(e3).u_(f3).u_(g3).u_(h3)
                .u_(a2).u_(b2).u_(c2).u_(d2).u_(e2).u_(f2).u_(g2).u_(h2)
                .u_(a1).u_(b1).u_(c1).u_(d1).u_(e1).u_(f1).u_(g1).u_(h1)
        ), view.getChessBoard());

        view = movesBuilder.cellLeftClicked(f5);
        assertEquals(chessBoardView(b -> b
                .u_(a8).u_(b8).u_(c8).u_(d8).u_(e8).u_(f8).u_(g8).u_(h8)
                .u_(a7).u_(b7).u_(c7).u_(d7).u_(e7).y_(f7).u_(g7).u_(h7)
                .u_(a6).u_(b6).u_(c6).up(d6).u_(e6).u_(f6).u_(g6).u_(h6)
                .u_(a5).u_(b5).u_(c5).u_(d5).uP(e5).gp(f5).u_(g5).u_(h5)
                .u_(a4).u_(b4).u_(c4).u_(d4).u_(e4).u_(f4).u_(g4).u_(h4)
                .u_(a3).u_(b3).u_(c3).u_(d3).u_(e3).u_(f3).u_(g3).u_(h3)
                .u_(a2).u_(b2).u_(c2).u_(d2).u_(e2).u_(f2).u_(g2).u_(h2)
                .u_(a1).u_(b1).u_(c1).u_(d1).u_(e1).u_(f1).u_(g1).u_(h1)
        ), view.getChessBoard());

        view = movesBuilder.cellLeftClicked(e5);
        assertEquals(chessBoardView(b -> b
                .u_(a8).u_(b8).u_(c8).u_(d8).u_(e8).u_(f8).u_(g8).u_(h8)
                .u_(a7).u_(b7).u_(c7).u_(d7).u_(e7).u_(f7).u_(g7).u_(h7)
                .u_(a6).u_(b6).u_(c6).gp(d6).g_(e6).g_(f6).u_(g6).u_(h6)
                .u_(a5).u_(b5).u_(c5).u_(d5).yP(e5).up(f5).u_(g5).u_(h5)
                .u_(a4).u_(b4).u_(c4).u_(d4).u_(e4).u_(f4).u_(g4).u_(h4)
                .u_(a3).u_(b3).u_(c3).u_(d3).u_(e3).u_(f3).u_(g3).u_(h3)
                .u_(a2).u_(b2).u_(c2).u_(d2).u_(e2).u_(f2).u_(g2).u_(h2)
                .u_(a1).u_(b1).u_(c1).u_(d1).u_(e1).u_(f1).u_(g1).u_(h1)
        ), view.getChessBoard());

        view = movesBuilder.cellLeftClicked(f6);
        assertEquals(chessBoardView(b -> b
                .u_(a8).u_(b8).u_(c8).u_(d8).u_(e8).u_(f8).u_(g8).u_(h8)
                .u_(a7).u_(b7).u_(c7).u_(d7).u_(e7).u_(f7).u_(g7).u_(h7)
                .u_(a6).u_(b6).u_(c6).up(d6).u_(e6).gP(f6).u_(g6).u_(h6)
                .u_(a5).u_(b5).u_(c5).u_(d5).y_(e5).u_(f5).u_(g5).u_(h5)
                .u_(a4).u_(b4).u_(c4).u_(d4).u_(e4).u_(f4).u_(g4).u_(h4)
                .u_(a3).u_(b3).u_(c3).u_(d3).u_(e3).u_(f3).u_(g3).u_(h3)
                .u_(a2).u_(b2).u_(c2).u_(d2).u_(e2).u_(f2).u_(g2).u_(h2)
                .u_(a1).u_(b1).u_(c1).u_(d1).u_(e1).u_(f1).u_(g1).u_(h1)
        ), view.getChessBoard());

        view = movesBuilder.cellLeftClicked(d6);
        assertEquals(chessBoardView(b -> b
                .u_(a8).u_(b8).u_(c8).u_(d8).u_(e8).u_(f8).u_(g8).u_(h8)
                .u_(a7).u_(b7).u_(c7).u_(d7).u_(e7).u_(f7).u_(g7).u_(h7)
                .u_(a6).u_(b6).u_(c6).yp(d6).u_(e6).uP(f6).u_(g6).u_(h6)
                .u_(a5).u_(b5).u_(c5).g_(d5).u_(e5).u_(f5).u_(g5).u_(h5)
                .u_(a4).u_(b4).u_(c4).u_(d4).u_(e4).u_(f4).u_(g4).u_(h4)
                .u_(a3).u_(b3).u_(c3).u_(d3).u_(e3).u_(f3).u_(g3).u_(h3)
                .u_(a2).u_(b2).u_(c2).u_(d2).u_(e2).u_(f2).u_(g2).u_(h2)
                .u_(a1).u_(b1).u_(c1).u_(d1).u_(e1).u_(f1).u_(g1).u_(h1)
        ), view.getChessBoard());
    }
    @Test public void test_enPasssantIsDeterminedCorrectlyForBlackLeft() {
        //given
        MovesBuilder movesBuilder = new MovesBuilder(initialPosition(WHITE, b->b
                .P(a2).p(b4).P(c3)
        ));

        ChessComponentView view = movesBuilder.cellLeftClicked(a2);
        assertEquals(chessBoardView(b -> b
                .u_(a8).u_(b8).u_(c8).u_(d8).u_(e8).u_(f8).u_(g8).u_(h8)
                .u_(a7).u_(b7).u_(c7).u_(d7).u_(e7).u_(f7).u_(g7).u_(h7)
                .u_(a6).u_(b6).u_(c6).u_(d6).u_(e6).u_(f6).u_(g6).u_(h6)
                .u_(a5).u_(b5).u_(c5).u_(d5).u_(e5).u_(f5).u_(g5).u_(h5)
                .g_(a4).up(b4).u_(c4).u_(d4).u_(e4).u_(f4).u_(g4).u_(h4)
                .g_(a3).u_(b3).uP(c3).u_(d3).u_(e3).u_(f3).u_(g3).u_(h3)
                .yP(a2).u_(b2).u_(c2).u_(d2).u_(e2).u_(f2).u_(g2).u_(h2)
                .u_(a1).u_(b1).u_(c1).u_(d1).u_(e1).u_(f1).u_(g1).u_(h1)
        ), view.getChessBoard());

        view = movesBuilder.cellLeftClicked(a4);
        assertEquals(chessBoardView(b -> b
                .u_(a8).u_(b8).u_(c8).u_(d8).u_(e8).u_(f8).u_(g8).u_(h8)
                .u_(a7).u_(b7).u_(c7).u_(d7).u_(e7).u_(f7).u_(g7).u_(h7)
                .u_(a6).u_(b6).u_(c6).u_(d6).u_(e6).u_(f6).u_(g6).u_(h6)
                .u_(a5).u_(b5).u_(c5).u_(d5).u_(e5).u_(f5).u_(g5).u_(h5)
                .gP(a4).up(b4).u_(c4).u_(d4).u_(e4).u_(f4).u_(g4).u_(h4)
                .u_(a3).u_(b3).uP(c3).u_(d3).u_(e3).u_(f3).u_(g3).u_(h3)
                .y_(a2).u_(b2).u_(c2).u_(d2).u_(e2).u_(f2).u_(g2).u_(h2)
                .u_(a1).u_(b1).u_(c1).u_(d1).u_(e1).u_(f1).u_(g1).u_(h1)
        ), view.getChessBoard());

        view = movesBuilder.cellLeftClicked(b4);
        assertEquals(chessBoardView(b -> b
                .u_(a8).u_(b8).u_(c8).u_(d8).u_(e8).u_(f8).u_(g8).u_(h8)
                .u_(a7).u_(b7).u_(c7).u_(d7).u_(e7).u_(f7).u_(g7).u_(h7)
                .u_(a6).u_(b6).u_(c6).u_(d6).u_(e6).u_(f6).u_(g6).u_(h6)
                .u_(a5).u_(b5).u_(c5).u_(d5).u_(e5).u_(f5).u_(g5).u_(h5)
                .uP(a4).yp(b4).u_(c4).u_(d4).u_(e4).u_(f4).u_(g4).u_(h4)
                .g_(a3).g_(b3).gP(c3).u_(d3).u_(e3).u_(f3).u_(g3).u_(h3)
                .u_(a2).u_(b2).u_(c2).u_(d2).u_(e2).u_(f2).u_(g2).u_(h2)
                .u_(a1).u_(b1).u_(c1).u_(d1).u_(e1).u_(f1).u_(g1).u_(h1)
        ), view.getChessBoard());

        view = movesBuilder.cellLeftClicked(a3);
        assertEquals(chessBoardView(b -> b
                .u_(a8).u_(b8).u_(c8).u_(d8).u_(e8).u_(f8).u_(g8).u_(h8)
                .u_(a7).u_(b7).u_(c7).u_(d7).u_(e7).u_(f7).u_(g7).u_(h7)
                .u_(a6).u_(b6).u_(c6).u_(d6).u_(e6).u_(f6).u_(g6).u_(h6)
                .u_(a5).u_(b5).u_(c5).u_(d5).u_(e5).u_(f5).u_(g5).u_(h5)
                .u_(a4).y_(b4).u_(c4).u_(d4).u_(e4).u_(f4).u_(g4).u_(h4)
                .gp(a3).u_(b3).uP(c3).u_(d3).u_(e3).u_(f3).u_(g3).u_(h3)
                .u_(a2).u_(b2).u_(c2).u_(d2).u_(e2).u_(f2).u_(g2).u_(h2)
                .u_(a1).u_(b1).u_(c1).u_(d1).u_(e1).u_(f1).u_(g1).u_(h1)
        ), view.getChessBoard());

        view = movesBuilder.cellLeftClicked(c3);
        assertEquals(chessBoardView(b -> b
                .u_(a8).u_(b8).u_(c8).u_(d8).u_(e8).u_(f8).u_(g8).u_(h8)
                .u_(a7).u_(b7).u_(c7).u_(d7).u_(e7).u_(f7).u_(g7).u_(h7)
                .u_(a6).u_(b6).u_(c6).u_(d6).u_(e6).u_(f6).u_(g6).u_(h6)
                .u_(a5).u_(b5).u_(c5).u_(d5).u_(e5).u_(f5).u_(g5).u_(h5)
                .u_(a4).u_(b4).g_(c4).u_(d4).u_(e4).u_(f4).u_(g4).u_(h4)
                .up(a3).u_(b3).yP(c3).u_(d3).u_(e3).u_(f3).u_(g3).u_(h3)
                .u_(a2).u_(b2).u_(c2).u_(d2).u_(e2).u_(f2).u_(g2).u_(h2)
                .u_(a1).u_(b1).u_(c1).u_(d1).u_(e1).u_(f1).u_(g1).u_(h1)
        ), view.getChessBoard());
    }
    @Test public void test_enPasssantIsDeterminedCorrectlyForBlackRight() {
        //given
        MovesBuilder movesBuilder = new MovesBuilder(initialPosition(WHITE, b->b
                .P(a3).p(b4).P(c2)
        ));

        ChessComponentView view = movesBuilder.cellLeftClicked(c2);
        assertEquals(chessBoardView(b -> b
                .u_(a8).u_(b8).u_(c8).u_(d8).u_(e8).u_(f8).u_(g8).u_(h8)
                .u_(a7).u_(b7).u_(c7).u_(d7).u_(e7).u_(f7).u_(g7).u_(h7)
                .u_(a6).u_(b6).u_(c6).u_(d6).u_(e6).u_(f6).u_(g6).u_(h6)
                .u_(a5).u_(b5).u_(c5).u_(d5).u_(e5).u_(f5).u_(g5).u_(h5)
                .u_(a4).up(b4).g_(c4).u_(d4).u_(e4).u_(f4).u_(g4).u_(h4)
                .uP(a3).u_(b3).g_(c3).u_(d3).u_(e3).u_(f3).u_(g3).u_(h3)
                .u_(a2).u_(b2).yP(c2).u_(d2).u_(e2).u_(f2).u_(g2).u_(h2)
                .u_(a1).u_(b1).u_(c1).u_(d1).u_(e1).u_(f1).u_(g1).u_(h1)
        ), view.getChessBoard());

        view = movesBuilder.cellLeftClicked(c4);
        assertEquals(chessBoardView(b -> b
                .u_(a8).u_(b8).u_(c8).u_(d8).u_(e8).u_(f8).u_(g8).u_(h8)
                .u_(a7).u_(b7).u_(c7).u_(d7).u_(e7).u_(f7).u_(g7).u_(h7)
                .u_(a6).u_(b6).u_(c6).u_(d6).u_(e6).u_(f6).u_(g6).u_(h6)
                .u_(a5).u_(b5).u_(c5).u_(d5).u_(e5).u_(f5).u_(g5).u_(h5)
                .u_(a4).up(b4).gP(c4).u_(d4).u_(e4).u_(f4).u_(g4).u_(h4)
                .uP(a3).u_(b3).u_(c3).u_(d3).u_(e3).u_(f3).u_(g3).u_(h3)
                .u_(a2).u_(b2).y_(c2).u_(d2).u_(e2).u_(f2).u_(g2).u_(h2)
                .u_(a1).u_(b1).u_(c1).u_(d1).u_(e1).u_(f1).u_(g1).u_(h1)
        ), view.getChessBoard());

        view = movesBuilder.cellLeftClicked(b4);
        assertEquals(chessBoardView(b -> b
                .u_(a8).u_(b8).u_(c8).u_(d8).u_(e8).u_(f8).u_(g8).u_(h8)
                .u_(a7).u_(b7).u_(c7).u_(d7).u_(e7).u_(f7).u_(g7).u_(h7)
                .u_(a6).u_(b6).u_(c6).u_(d6).u_(e6).u_(f6).u_(g6).u_(h6)
                .u_(a5).u_(b5).u_(c5).u_(d5).u_(e5).u_(f5).u_(g5).u_(h5)
                .u_(a4).yp(b4).uP(c4).u_(d4).u_(e4).u_(f4).u_(g4).u_(h4)
                .gP(a3).g_(b3).g_(c3).u_(d3).u_(e3).u_(f3).u_(g3).u_(h3)
                .u_(a2).u_(b2).u_(c2).u_(d2).u_(e2).u_(f2).u_(g2).u_(h2)
                .u_(a1).u_(b1).u_(c1).u_(d1).u_(e1).u_(f1).u_(g1).u_(h1)
        ), view.getChessBoard());

        view = movesBuilder.cellLeftClicked(c3);
        assertEquals(chessBoardView(b -> b
                .u_(a8).u_(b8).u_(c8).u_(d8).u_(e8).u_(f8).u_(g8).u_(h8)
                .u_(a7).u_(b7).u_(c7).u_(d7).u_(e7).u_(f7).u_(g7).u_(h7)
                .u_(a6).u_(b6).u_(c6).u_(d6).u_(e6).u_(f6).u_(g6).u_(h6)
                .u_(a5).u_(b5).u_(c5).u_(d5).u_(e5).u_(f5).u_(g5).u_(h5)
                .u_(a4).y_(b4).u_(c4).u_(d4).u_(e4).u_(f4).u_(g4).u_(h4)
                .uP(a3).u_(b3).gp(c3).u_(d3).u_(e3).u_(f3).u_(g3).u_(h3)
                .u_(a2).u_(b2).u_(c2).u_(d2).u_(e2).u_(f2).u_(g2).u_(h2)
                .u_(a1).u_(b1).u_(c1).u_(d1).u_(e1).u_(f1).u_(g1).u_(h1)
        ), view.getChessBoard());

        view = movesBuilder.cellLeftClicked(a3);
        assertEquals(chessBoardView(b -> b
                .u_(a8).u_(b8).u_(c8).u_(d8).u_(e8).u_(f8).u_(g8).u_(h8)
                .u_(a7).u_(b7).u_(c7).u_(d7).u_(e7).u_(f7).u_(g7).u_(h7)
                .u_(a6).u_(b6).u_(c6).u_(d6).u_(e6).u_(f6).u_(g6).u_(h6)
                .u_(a5).u_(b5).u_(c5).u_(d5).u_(e5).u_(f5).u_(g5).u_(h5)
                .g_(a4).u_(b4).u_(c4).u_(d4).u_(e4).u_(f4).u_(g4).u_(h4)
                .yP(a3).u_(b3).up(c3).u_(d3).u_(e3).u_(f3).u_(g3).u_(h3)
                .u_(a2).u_(b2).u_(c2).u_(d2).u_(e2).u_(f2).u_(g2).u_(h2)
                .u_(a1).u_(b1).u_(c1).u_(d1).u_(e1).u_(f1).u_(g1).u_(h1)
        ), view.getChessBoard());
    }
    @Test public void test_whitePawnTurnsIntoAnotherPiece() {
        MovesBuilder movesBuilder = new MovesBuilder(initialPosition(WHITE, b->b
                .P(f6).p(e7)
        ));

        ChessComponentView view = movesBuilder.cellLeftClicked(f6);
        assertEquals(chessBoardView(b -> b
                .u_(a8).u_(b8).u_(c8).u_(d8).u_(e8).u_(f8).u_(g8).u_(h8)
                .u_(a7).u_(b7).u_(c7).u_(d7).gp(e7).g_(f7).u_(g7).u_(h7)
                .u_(a6).u_(b6).u_(c6).u_(d6).u_(e6).yP(f6).u_(g6).u_(h6)
                .u_(a5).u_(b5).u_(c5).u_(d5).u_(e5).u_(f5).u_(g5).u_(h5)
                .u_(a4).u_(b4).u_(c4).u_(d4).u_(e4).u_(f4).u_(g4).u_(h4)
                .u_(a3).u_(b3).u_(c3).u_(d3).u_(e3).u_(f3).u_(g3).u_(h3)
                .u_(a2).u_(b2).u_(c2).u_(d2).u_(e2).u_(f2).u_(g2).u_(h2)
                .u_(a1).u_(b1).u_(c1).u_(d1).u_(e1).u_(f1).u_(g1).u_(h1)
        ), view.getChessBoard());

        view = movesBuilder.cellLeftClicked(f7);
        assertEquals(chessBoardView(b -> b
                .u_(a8).u_(b8).u_(c8).u_(d8).u_(e8).u_(f8).u_(g8).u_(h8)
                .u_(a7).u_(b7).u_(c7).u_(d7).up(e7).gP(f7).u_(g7).u_(h7)
                .u_(a6).u_(b6).u_(c6).u_(d6).u_(e6).y_(f6).u_(g6).u_(h6)
                .u_(a5).u_(b5).u_(c5).u_(d5).u_(e5).u_(f5).u_(g5).u_(h5)
                .u_(a4).u_(b4).u_(c4).u_(d4).u_(e4).u_(f4).u_(g4).u_(h4)
                .u_(a3).u_(b3).u_(c3).u_(d3).u_(e3).u_(f3).u_(g3).u_(h3)
                .u_(a2).u_(b2).u_(c2).u_(d2).u_(e2).u_(f2).u_(g2).u_(h2)
                .u_(a1).u_(b1).u_(c1).u_(d1).u_(e1).u_(f1).u_(g1).u_(h1)
        ), view.getChessBoard());

        view = movesBuilder.cellLeftClicked(e7);
        assertEquals(chessBoardView(b -> b
                .u_(a8).u_(b8).u_(c8).u_(d8).u_(e8).u_(f8).u_(g8).u_(h8)
                .u_(a7).u_(b7).u_(c7).u_(d7).yp(e7).uP(f7).u_(g7).u_(h7)
                .u_(a6).u_(b6).u_(c6).u_(d6).g_(e6).u_(f6).u_(g6).u_(h6)
                .u_(a5).u_(b5).u_(c5).u_(d5).g_(e5).u_(f5).u_(g5).u_(h5)
                .u_(a4).u_(b4).u_(c4).u_(d4).u_(e4).u_(f4).u_(g4).u_(h4)
                .u_(a3).u_(b3).u_(c3).u_(d3).u_(e3).u_(f3).u_(g3).u_(h3)
                .u_(a2).u_(b2).u_(c2).u_(d2).u_(e2).u_(f2).u_(g2).u_(h2)
                .u_(a1).u_(b1).u_(c1).u_(d1).u_(e1).u_(f1).u_(g1).u_(h1)
        ), view.getChessBoard());

        view = movesBuilder.cellLeftClicked(e6);
        assertEquals(chessBoardView(b -> b
                .u_(a8).u_(b8).u_(c8).u_(d8).u_(e8).u_(f8).u_(g8).u_(h8)
                .u_(a7).u_(b7).u_(c7).u_(d7).y_(e7).uP(f7).u_(g7).u_(h7)
                .u_(a6).u_(b6).u_(c6).u_(d6).gp(e6).u_(f6).u_(g6).u_(h6)
                .u_(a5).u_(b5).u_(c5).u_(d5).u_(e5).u_(f5).u_(g5).u_(h5)
                .u_(a4).u_(b4).u_(c4).u_(d4).u_(e4).u_(f4).u_(g4).u_(h4)
                .u_(a3).u_(b3).u_(c3).u_(d3).u_(e3).u_(f3).u_(g3).u_(h3)
                .u_(a2).u_(b2).u_(c2).u_(d2).u_(e2).u_(f2).u_(g2).u_(h2)
                .u_(a1).u_(b1).u_(c1).u_(d1).u_(e1).u_(f1).u_(g1).u_(h1)
        ), view.getChessBoard());

        view = movesBuilder.cellLeftClicked(f7);
        assertEquals(chessBoardView(b -> b
                .u_(a8).u_(b8).u_(c8).u_(d8).u_(e8).g_(f8).u_(g8).u_(h8)
                .u_(a7).u_(b7).u_(c7).u_(d7).u_(e7).yP(f7).u_(g7).u_(h7)
                .u_(a6).u_(b6).u_(c6).u_(d6).up(e6).u_(f6).u_(g6).u_(h6)
                .u_(a5).u_(b5).u_(c5).u_(d5).u_(e5).u_(f5).u_(g5).u_(h5)
                .u_(a4).u_(b4).u_(c4).u_(d4).u_(e4).u_(f4).u_(g4).u_(h4)
                .u_(a3).u_(b3).u_(c3).u_(d3).u_(e3).u_(f3).u_(g3).u_(h3)
                .u_(a2).u_(b2).u_(c2).u_(d2).u_(e2).u_(f2).u_(g2).u_(h2)
                .u_(a1).u_(b1).u_(c1).u_(d1).u_(e1).u_(f1).u_(g1).u_(h1)
        ), view.getChessBoard());

        view = movesBuilder.cellLeftClicked(f8);
        assertEquals(chessBoardView(b -> b
                .u_(a8).u_(b8).u_(c8).u_(d8).u_(e8).g_(f8).u_(g8).u_(h8)
                .u_(a7).u_(b7).u_(c7).u_(d7).u_(e7).yP(f7).u_(g7).u_(h7)
                .u_(a6).u_(b6).u_(c6).u_(d6).up(e6).u_(f6).u_(g6).u_(h6)
                .u_(a5).u_(b5).u_(c5).u_(d5).u_(e5).u_(f5).u_(g5).u_(h5)
                .u_(a4).u_(b4).u_(c4).u_(d4).u_(e4).u_(f4).u_(g4).u_(h4)
                .u_(a3).u_(b3).u_(c3).u_(d3).u_(e3).u_(f3).u_(g3).u_(h3)
                .u_(a2).u_(b2).u_(c2).u_(d2).u_(e2).u_(f2).u_(g2).u_(h2)
                .u_(a1).u_(b1).u_(c1).u_(d1).u_(e1).u_(f1).u_(g1).u_(h1)
        ), view.getChessBoard());
        assertChoseChessmanTypeDialogForColor(WHITE, view);

        view = movesBuilder.cellLeftClicked(new CellCoords(20,0));
        assertEquals(chessBoardView(b -> b
                .u_(a8).u_(b8).u_(c8).u_(d8).u_(e8).gN(f8).u_(g8).u_(h8)
                .u_(a7).u_(b7).u_(c7).u_(d7).u_(e7).y_(f7).u_(g7).u_(h7)
                .u_(a6).u_(b6).u_(c6).u_(d6).up(e6).u_(f6).u_(g6).u_(h6)
                .u_(a5).u_(b5).u_(c5).u_(d5).u_(e5).u_(f5).u_(g5).u_(h5)
                .u_(a4).u_(b4).u_(c4).u_(d4).u_(e4).u_(f4).u_(g4).u_(h4)
                .u_(a3).u_(b3).u_(c3).u_(d3).u_(e3).u_(f3).u_(g3).u_(h3)
                .u_(a2).u_(b2).u_(c2).u_(d2).u_(e2).u_(f2).u_(g2).u_(h2)
                .u_(a1).u_(b1).u_(c1).u_(d1).u_(e1).u_(f1).u_(g1).u_(h1)
        ), view.getChessBoard());
        Assert.assertNull(view.getChoseChessmanTypeDialogView());

        view = movesBuilder.cellLeftClicked(e6);
        assertEquals(chessBoardView(b -> b
                .u_(a8).u_(b8).u_(c8).u_(d8).u_(e8).uN(f8).u_(g8).u_(h8)
                .u_(a7).u_(b7).u_(c7).u_(d7).u_(e7).u_(f7).u_(g7).u_(h7)
                .u_(a6).u_(b6).u_(c6).u_(d6).yp(e6).u_(f6).u_(g6).u_(h6)
                .u_(a5).u_(b5).u_(c5).u_(d5).g_(e5).u_(f5).u_(g5).u_(h5)
                .u_(a4).u_(b4).u_(c4).u_(d4).u_(e4).u_(f4).u_(g4).u_(h4)
                .u_(a3).u_(b3).u_(c3).u_(d3).u_(e3).u_(f3).u_(g3).u_(h3)
                .u_(a2).u_(b2).u_(c2).u_(d2).u_(e2).u_(f2).u_(g2).u_(h2)
                .u_(a1).u_(b1).u_(c1).u_(d1).u_(e1).u_(f1).u_(g1).u_(h1)
        ), view.getChessBoard());
    }
    @Test public void test_blackPawnTurnsIntoAnotherPiece() {
        MovesBuilder movesBuilder = new MovesBuilder(initialPosition(BLACK, b->b
                .p(f3).P(e2)
        ));

        ChessComponentView view = movesBuilder.cellLeftClicked(f3);
        assertEquals(chessBoardView(b -> b
                .u_(a8).u_(b8).u_(c8).u_(d8).u_(e8).u_(f8).u_(g8).u_(h8)
                .u_(a7).u_(b7).u_(c7).u_(d7).u_(e7).u_(f7).u_(g7).u_(h7)
                .u_(a6).u_(b6).u_(c6).u_(d6).u_(e6).u_(f6).u_(g6).u_(h6)
                .u_(a5).u_(b5).u_(c5).u_(d5).u_(e5).u_(f5).u_(g5).u_(h5)
                .u_(a4).u_(b4).u_(c4).u_(d4).u_(e4).u_(f4).u_(g4).u_(h4)
                .u_(a3).u_(b3).u_(c3).u_(d3).u_(e3).yp(f3).u_(g3).u_(h3)
                .u_(a2).u_(b2).u_(c2).u_(d2).gP(e2).g_(f2).u_(g2).u_(h2)
                .u_(a1).u_(b1).u_(c1).u_(d1).u_(e1).u_(f1).u_(g1).u_(h1)
        ), view.getChessBoard());

        view = movesBuilder.cellLeftClicked(f2);
        assertEquals(chessBoardView(b -> b
                .u_(a8).u_(b8).u_(c8).u_(d8).u_(e8).u_(f8).u_(g8).u_(h8)
                .u_(a7).u_(b7).u_(c7).u_(d7).u_(e7).u_(f7).u_(g7).u_(h7)
                .u_(a6).u_(b6).u_(c6).u_(d6).u_(e6).u_(f6).u_(g6).u_(h6)
                .u_(a5).u_(b5).u_(c5).u_(d5).u_(e5).u_(f5).u_(g5).u_(h5)
                .u_(a4).u_(b4).u_(c4).u_(d4).u_(e4).u_(f4).u_(g4).u_(h4)
                .u_(a3).u_(b3).u_(c3).u_(d3).u_(e3).y_(f3).u_(g3).u_(h3)
                .u_(a2).u_(b2).u_(c2).u_(d2).uP(e2).gp(f2).u_(g2).u_(h2)
                .u_(a1).u_(b1).u_(c1).u_(d1).u_(e1).u_(f1).u_(g1).u_(h1)
        ), view.getChessBoard());

        view = movesBuilder.cellLeftClicked(e2);
        assertEquals(chessBoardView(b -> b
                .u_(a8).u_(b8).u_(c8).u_(d8).u_(e8).u_(f8).u_(g8).u_(h8)
                .u_(a7).u_(b7).u_(c7).u_(d7).u_(e7).u_(f7).u_(g7).u_(h7)
                .u_(a6).u_(b6).u_(c6).u_(d6).u_(e6).u_(f6).u_(g6).u_(h6)
                .u_(a5).u_(b5).u_(c5).u_(d5).u_(e5).u_(f5).u_(g5).u_(h5)
                .u_(a4).u_(b4).u_(c4).u_(d4).g_(e4).u_(f4).u_(g4).u_(h4)
                .u_(a3).u_(b3).u_(c3).u_(d3).g_(e3).u_(f3).u_(g3).u_(h3)
                .u_(a2).u_(b2).u_(c2).u_(d2).yP(e2).up(f2).u_(g2).u_(h2)
                .u_(a1).u_(b1).u_(c1).u_(d1).u_(e1).u_(f1).u_(g1).u_(h1)
        ), view.getChessBoard());

        view = movesBuilder.cellLeftClicked(e4);
        assertEquals(chessBoardView(b -> b
                .u_(a8).u_(b8).u_(c8).u_(d8).u_(e8).u_(f8).u_(g8).u_(h8)
                .u_(a7).u_(b7).u_(c7).u_(d7).u_(e7).u_(f7).u_(g7).u_(h7)
                .u_(a6).u_(b6).u_(c6).u_(d6).u_(e6).u_(f6).u_(g6).u_(h6)
                .u_(a5).u_(b5).u_(c5).u_(d5).u_(e5).u_(f5).u_(g5).u_(h5)
                .u_(a4).u_(b4).u_(c4).u_(d4).gP(e4).u_(f4).u_(g4).u_(h4)
                .u_(a3).u_(b3).u_(c3).u_(d3).u_(e3).u_(f3).u_(g3).u_(h3)
                .u_(a2).u_(b2).u_(c2).u_(d2).y_(e2).up(f2).u_(g2).u_(h2)
                .u_(a1).u_(b1).u_(c1).u_(d1).u_(e1).u_(f1).u_(g1).u_(h1)
        ), view.getChessBoard());

        view = movesBuilder.cellLeftClicked(f2);
        assertEquals(chessBoardView(b -> b
                .u_(a8).u_(b8).u_(c8).u_(d8).u_(e8).u_(f8).u_(g8).u_(h8)
                .u_(a7).u_(b7).u_(c7).u_(d7).u_(e7).u_(f7).u_(g7).u_(h7)
                .u_(a6).u_(b6).u_(c6).u_(d6).u_(e6).u_(f6).u_(g6).u_(h6)
                .u_(a5).u_(b5).u_(c5).u_(d5).u_(e5).u_(f5).u_(g5).u_(h5)
                .u_(a4).u_(b4).u_(c4).u_(d4).uP(e4).u_(f4).u_(g4).u_(h4)
                .u_(a3).u_(b3).u_(c3).u_(d3).u_(e3).u_(f3).u_(g3).u_(h3)
                .u_(a2).u_(b2).u_(c2).u_(d2).u_(e2).yp(f2).u_(g2).u_(h2)
                .u_(a1).u_(b1).u_(c1).u_(d1).u_(e1).g_(f1).u_(g1).u_(h1)
        ), view.getChessBoard());

        view = movesBuilder.cellLeftClicked(f1);
        assertEquals(chessBoardView(b -> b
                .u_(a8).u_(b8).u_(c8).u_(d8).u_(e8).u_(f8).u_(g8).u_(h8)
                .u_(a7).u_(b7).u_(c7).u_(d7).u_(e7).u_(f7).u_(g7).u_(h7)
                .u_(a6).u_(b6).u_(c6).u_(d6).u_(e6).u_(f6).u_(g6).u_(h6)
                .u_(a5).u_(b5).u_(c5).u_(d5).u_(e5).u_(f5).u_(g5).u_(h5)
                .u_(a4).u_(b4).u_(c4).u_(d4).uP(e4).u_(f4).u_(g4).u_(h4)
                .u_(a3).u_(b3).u_(c3).u_(d3).u_(e3).u_(f3).u_(g3).u_(h3)
                .u_(a2).u_(b2).u_(c2).u_(d2).u_(e2).yp(f2).u_(g2).u_(h2)
                .u_(a1).u_(b1).u_(c1).u_(d1).u_(e1).g_(f1).u_(g1).u_(h1)
        ), view.getChessBoard());
        assertChoseChessmanTypeDialogForColor(BLACK, view);

        view = movesBuilder.cellLeftClicked(new CellCoords(23,0));
        assertEquals(chessBoardView(b -> b
                .u_(a8).u_(b8).u_(c8).u_(d8).u_(e8).u_(f8).u_(g8).u_(h8)
                .u_(a7).u_(b7).u_(c7).u_(d7).u_(e7).u_(f7).u_(g7).u_(h7)
                .u_(a6).u_(b6).u_(c6).u_(d6).u_(e6).u_(f6).u_(g6).u_(h6)
                .u_(a5).u_(b5).u_(c5).u_(d5).u_(e5).u_(f5).u_(g5).u_(h5)
                .u_(a4).u_(b4).u_(c4).u_(d4).uP(e4).u_(f4).u_(g4).u_(h4)
                .u_(a3).u_(b3).u_(c3).u_(d3).u_(e3).u_(f3).u_(g3).u_(h3)
                .u_(a2).u_(b2).u_(c2).u_(d2).u_(e2).y_(f2).u_(g2).u_(h2)
                .u_(a1).u_(b1).u_(c1).u_(d1).u_(e1).gq(f1).u_(g1).u_(h1)
        ), view.getChessBoard());
        Assert.assertNull(view.getChoseChessmanTypeDialogView());

        view = movesBuilder.cellLeftClicked(e4);
        assertEquals(chessBoardView(b -> b
                .u_(a8).u_(b8).u_(c8).u_(d8).u_(e8).u_(f8).u_(g8).u_(h8)
                .u_(a7).u_(b7).u_(c7).u_(d7).u_(e7).u_(f7).u_(g7).u_(h7)
                .u_(a6).u_(b6).u_(c6).u_(d6).u_(e6).u_(f6).u_(g6).u_(h6)
                .u_(a5).u_(b5).u_(c5).u_(d5).g_(e5).u_(f5).u_(g5).u_(h5)
                .u_(a4).u_(b4).u_(c4).u_(d4).yP(e4).u_(f4).u_(g4).u_(h4)
                .u_(a3).u_(b3).u_(c3).u_(d3).u_(e3).u_(f3).u_(g3).u_(h3)
                .u_(a2).u_(b2).u_(c2).u_(d2).u_(e2).u_(f2).u_(g2).u_(h2)
                .u_(a1).u_(b1).u_(c1).u_(d1).u_(e1).uq(f1).u_(g1).u_(h1)
        ), view.getChessBoard());
        Assert.assertNull(view.getChoseChessmanTypeDialogView());
    }
    @Test public void test_ChoseChessmanTypeDialogIsOpened_noMovesAreAccepted() {
        MovesBuilder movesBuilder = new MovesBuilder(initialPosition(BLACK, b->b
                .p(f2).P(e2)
        ));

        ChessComponentView view = movesBuilder.cellLeftClicked(f2);
        assertEquals(chessBoardView(b -> b
                .u_(a8).u_(b8).u_(c8).u_(d8).u_(e8).u_(f8).u_(g8).u_(h8)
                .u_(a7).u_(b7).u_(c7).u_(d7).u_(e7).u_(f7).u_(g7).u_(h7)
                .u_(a6).u_(b6).u_(c6).u_(d6).u_(e6).u_(f6).u_(g6).u_(h6)
                .u_(a5).u_(b5).u_(c5).u_(d5).u_(e5).u_(f5).u_(g5).u_(h5)
                .u_(a4).u_(b4).u_(c4).u_(d4).u_(e4).u_(f4).u_(g4).u_(h4)
                .u_(a3).u_(b3).u_(c3).u_(d3).u_(e3).u_(f3).u_(g3).u_(h3)
                .u_(a2).u_(b2).u_(c2).u_(d2).uP(e2).yp(f2).u_(g2).u_(h2)
                .u_(a1).u_(b1).u_(c1).u_(d1).u_(e1).g_(f1).u_(g1).u_(h1)
        ), view.getChessBoard());

        view = movesBuilder.cellLeftClicked(f1);
        assertEquals(chessBoardView(b -> b
                .u_(a8).u_(b8).u_(c8).u_(d8).u_(e8).u_(f8).u_(g8).u_(h8)
                .u_(a7).u_(b7).u_(c7).u_(d7).u_(e7).u_(f7).u_(g7).u_(h7)
                .u_(a6).u_(b6).u_(c6).u_(d6).u_(e6).u_(f6).u_(g6).u_(h6)
                .u_(a5).u_(b5).u_(c5).u_(d5).u_(e5).u_(f5).u_(g5).u_(h5)
                .u_(a4).u_(b4).u_(c4).u_(d4).u_(e4).u_(f4).u_(g4).u_(h4)
                .u_(a3).u_(b3).u_(c3).u_(d3).u_(e3).u_(f3).u_(g3).u_(h3)
                .u_(a2).u_(b2).u_(c2).u_(d2).uP(e2).yp(f2).u_(g2).u_(h2)
                .u_(a1).u_(b1).u_(c1).u_(d1).u_(e1).g_(f1).u_(g1).u_(h1)
        ), view.getChessBoard());
        assertChoseChessmanTypeDialogForColor(BLACK, view);

        view = movesBuilder.cellLeftClicked(e2);
        assertEquals(chessBoardView(b -> b
                .u_(a8).u_(b8).u_(c8).u_(d8).u_(e8).u_(f8).u_(g8).u_(h8)
                .u_(a7).u_(b7).u_(c7).u_(d7).u_(e7).u_(f7).u_(g7).u_(h7)
                .u_(a6).u_(b6).u_(c6).u_(d6).u_(e6).u_(f6).u_(g6).u_(h6)
                .u_(a5).u_(b5).u_(c5).u_(d5).u_(e5).u_(f5).u_(g5).u_(h5)
                .u_(a4).u_(b4).u_(c4).u_(d4).u_(e4).u_(f4).u_(g4).u_(h4)
                .u_(a3).u_(b3).u_(c3).u_(d3).u_(e3).u_(f3).u_(g3).u_(h3)
                .u_(a2).u_(b2).u_(c2).u_(d2).uP(e2).yp(f2).u_(g2).u_(h2)
                .u_(a1).u_(b1).u_(c1).u_(d1).u_(e1).g_(f1).u_(g1).u_(h1)
        ), view.getChessBoard());
        assertChoseChessmanTypeDialogForColor(BLACK, view);

        view = movesBuilder.cellLeftClicked(f1);
        assertEquals(chessBoardView(b -> b
                .u_(a8).u_(b8).u_(c8).u_(d8).u_(e8).u_(f8).u_(g8).u_(h8)
                .u_(a7).u_(b7).u_(c7).u_(d7).u_(e7).u_(f7).u_(g7).u_(h7)
                .u_(a6).u_(b6).u_(c6).u_(d6).u_(e6).u_(f6).u_(g6).u_(h6)
                .u_(a5).u_(b5).u_(c5).u_(d5).u_(e5).u_(f5).u_(g5).u_(h5)
                .u_(a4).u_(b4).u_(c4).u_(d4).u_(e4).u_(f4).u_(g4).u_(h4)
                .u_(a3).u_(b3).u_(c3).u_(d3).u_(e3).u_(f3).u_(g3).u_(h3)
                .u_(a2).u_(b2).u_(c2).u_(d2).uP(e2).yp(f2).u_(g2).u_(h2)
                .u_(a1).u_(b1).u_(c1).u_(d1).u_(e1).g_(f1).u_(g1).u_(h1)
        ), view.getChessBoard());
        assertChoseChessmanTypeDialogForColor(BLACK, view);
    }
    @Test public void test_itIsNotPossibleToMoveKingToACellWhereItWillBeChecked() {
        MovesBuilder movesBuilder = new MovesBuilder(initialPosition(WHITE, b->b
                .p(b7).p(e4).K(c2)
        ));

        ChessComponentView view = movesBuilder.cellLeftClicked(c2);
        assertEquals(chessBoardView(b -> b
                .u_(a8).u_(b8).u_(c8).u_(d8).u_(e8).u_(f8).u_(g8).u_(h8)
                .u_(a7).up(b7).u_(c7).u_(d7).u_(e7).u_(f7).u_(g7).u_(h7)
                .u_(a6).u_(b6).u_(c6).u_(d6).u_(e6).u_(f6).u_(g6).u_(h6)
                .u_(a5).u_(b5).u_(c5).u_(d5).u_(e5).u_(f5).u_(g5).u_(h5)
                .u_(a4).u_(b4).u_(c4).u_(d4).up(e4).u_(f4).u_(g4).u_(h4)
                .u_(a3).g_(b3).g_(c3).u_(d3).u_(e3).u_(f3).u_(g3).u_(h3)
                .u_(a2).g_(b2).yK(c2).g_(d2).u_(e2).u_(f2).u_(g2).u_(h2)
                .u_(a1).g_(b1).g_(c1).g_(d1).u_(e1).u_(f1).u_(g1).u_(h1)
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
                .u_(a8).u_(b8).u_(c8).u_(d8).ur(e8).u_(f8).u_(g8).u_(h8)
                .u_(a7).up(b7).u_(c7).u_(d7).u_(e7).u_(f7).u_(g7).u_(h7)
                .u_(a6).u_(b6).u_(c6).u_(d6).u_(e6).u_(f6).u_(g6).u_(h6)
                .u_(a5).u_(b5).u_(c5).u_(d5).u_(e5).u_(f5).u_(g5).u_(h5)
                .u_(a4).u_(b4).u_(c4).u_(d4).u_(e4).u_(f4).u_(g4).u_(h4)
                .u_(a3).u_(b3).u_(c3).u_(d3).u_(e3).u_(f3).u_(g3).u_(h3)
                .u_(a2).u_(b2).u_(c2).g_(d2).u_(e2).g_(f2).u_(g2).u_(h2)
                .uR(a1).u_(b1).u_(c1).g_(d1).yK(e1).g_(f1).u_(g1).uR(h1)
        ), view.getChessBoard());
    }
    @Test public void test_itIsNotPossibleToCastleIfKingWouldJumpOverAnAttackedCell() {
        MovesBuilder movesBuilder = new MovesBuilder(initialPosition(BLACK, b->b
                .r(a8).k(e8).r(h8)
                .P(g4).B(h4)
        ));

        ChessComponentView view = movesBuilder.cellLeftClicked(e8);
        assertEquals(chessBoardView(b -> b
                .ur(a8).u_(b8).u_(c8).u_(d8).yk(e8).g_(f8).g_(g8).ur(h8)
                .u_(a7).u_(b7).u_(c7).g_(d7).u_(e7).g_(f7).u_(g7).u_(h7)
                .u_(a6).u_(b6).u_(c6).u_(d6).u_(e6).u_(f6).u_(g6).u_(h6)
                .u_(a5).u_(b5).u_(c5).u_(d5).u_(e5).u_(f5).u_(g5).u_(h5)
                .u_(a4).u_(b4).u_(c4).u_(d4).u_(e4).u_(f4).uP(g4).uB(h4)
                .u_(a3).u_(b3).u_(c3).u_(d3).u_(e3).u_(f3).u_(g3).u_(h3)
                .u_(a2).u_(b2).u_(c2).u_(d2).u_(e2).u_(f2).u_(g2).u_(h2)
                .u_(a1).u_(b1).u_(c1).u_(d1).u_(e1).u_(f1).u_(g1).u_(h1)
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