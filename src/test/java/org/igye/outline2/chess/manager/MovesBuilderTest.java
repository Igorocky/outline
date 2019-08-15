package org.igye.outline2.chess.manager;

import org.igye.outline2.chess.dto.ChessComponentView;
import org.junit.Test;

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
        assertNoCellsAvailableToMoveTo(view);
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
        assertNoCellsAvailableToMoveTo(view);
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
        assertNoCellPreparedToMove(view);
        assertNoCellsAvailableToMoveTo(view);
        assertEqualsByChessmenTypes(chessBoard(b->b.N(f2).n(a1)), view);
    }
    @Test public void test_availableCastlingsAreDeterminedCorrectlyInCaseOfKingsMoveToA() {
        //given
        MovesBuilder movesBuilder = new MovesBuilder(initialPosition(WHITE, b->b
                .r(a8).k(e8).r(h8)
                .R(a1).K(e1).R(h1)
        ));

        //move white King, expect castling
        //when
        ChessComponentView view = movesBuilder.cellLeftClicked(e1);
        //then
        assertCellPreparedToMove(view, e1);
        assertCellsAvailableToMoveTo(view, setOf(d2,e2,f2,d1,f1,c1,g1));

        //when
        view = movesBuilder.cellLeftClicked(c1);
        //then
        assertNoCellPreparedToMove(view);
        assertNoCellsAvailableToMoveTo(view);
        assertEqualsByChessmenTypes(chessBoard(b->b
                .r(a8).k(e8).r(h8)
                .K(c1).R(d1).R(h1)
        ), view);

        //move black King, expect castling
        //when
        view = movesBuilder.cellLeftClicked(e8);
        //then
        assertCellPreparedToMove(view, e8);
        assertCellsAvailableToMoveTo(view, setOf(d7,e7,f7,d8,f8,c8,g8));

        //when
        view = movesBuilder.cellLeftClicked(c8);
        //then
        assertNoCellPreparedToMove(view);
        assertNoCellsAvailableToMoveTo(view);
        assertEqualsByChessmenTypes(chessBoard(b->b
                .k(c8).r(d8).r(h8)
                .K(c1).R(d1).R(h1)
        ), view);

        //move white King, don't expect castling
        //when
        view = movesBuilder.cellLeftClicked(c1);
        //then
        assertCellPreparedToMove(view, c1);
        assertCellsAvailableToMoveTo(view, setOf(b2,c2,d2,b1));

        //when
        view = movesBuilder.cellLeftClicked(b1);
        //then
        assertNoCellPreparedToMove(view);
        assertNoCellsAvailableToMoveTo(view);
        assertEqualsByChessmenTypes(chessBoard(b->b
                .k(c8).r(d8).r(h8)
                .K(b1).R(d1).R(h1)
        ), view);

        //move black King, don't expect castling
        //when
        view = movesBuilder.cellLeftClicked(c8);
        //then
        assertCellPreparedToMove(view, c8);
        assertCellsAvailableToMoveTo(view, setOf(b7,c7,d7,b8));

        //when
        view = movesBuilder.cellLeftClicked(b8);
        //then
        assertNoCellPreparedToMove(view);
        assertNoCellsAvailableToMoveTo(view);
        assertEqualsByChessmenTypes(chessBoard(b->b
                .k(b8).r(d8).r(h8)
                .K(b1).R(d1).R(h1)
        ), view);
    }
    @Test public void test_availableCastlingsAreDeterminedCorrectlyInCaseOfKingsMoveToH() {
        //given
        MovesBuilder movesBuilder = new MovesBuilder(initialPosition(WHITE, b->b
                .r(a8).k(e8).r(h8)
                .R(a1).K(e1).R(h1)
        ));

        //move white King, expect castling
        //when
        ChessComponentView view = movesBuilder.cellLeftClicked(e1);
        //then
        assertCellPreparedToMove(view, e1);
        assertCellsAvailableToMoveTo(view, setOf(d2,e2,f2,d1,f1,c1,g1));

        //when
        view = movesBuilder.cellLeftClicked(g1);
        //then
        assertNoCellPreparedToMove(view);
        assertNoCellsAvailableToMoveTo(view);
        assertEqualsByChessmenTypes(chessBoard(b->b
                .r(a8).k(e8).r(h8)
                .R(a1).R(f1).K(g1)
        ), view);

        //move black King, expect castling
        //when
        view = movesBuilder.cellLeftClicked(e8);
        //then
        assertCellPreparedToMove(view, e8);
        assertCellsAvailableToMoveTo(view, setOf(d7,e7,f7,d8,f8,c8,g8));

        //when
        view = movesBuilder.cellLeftClicked(g8);
        //then
        assertNoCellPreparedToMove(view);
        assertNoCellsAvailableToMoveTo(view);
        assertEqualsByChessmenTypes(chessBoard(b->b
                .r(a8).r(f8).k(g8)
                .R(a1).R(f1).K(g1)
        ), view);

        //move white King, don't expect castling
        //when
        view = movesBuilder.cellLeftClicked(g1);
        //then
        assertCellPreparedToMove(view, g1);
        assertCellsAvailableToMoveTo(view, setOf(f2,g2,h2,h1));

        //when
        view = movesBuilder.cellLeftClicked(h1);
        //then
        assertNoCellPreparedToMove(view);
        assertNoCellsAvailableToMoveTo(view);
        assertEqualsByChessmenTypes(chessBoard(b->b
                .r(a8).r(f8).k(g8)
                .R(a1).R(f1).K(h1)
        ), view);

        //move black King, don't expect castling
        //when
        view = movesBuilder.cellLeftClicked(g8);
        //then
        assertCellPreparedToMove(view, g8);
        assertCellsAvailableToMoveTo(view, setOf(f7,g7,h7,h8));

        //when
        view = movesBuilder.cellLeftClicked(h8);
        //then
        assertNoCellPreparedToMove(view);
        assertNoCellsAvailableToMoveTo(view);
        assertEqualsByChessmenTypes(chessBoard(b->b
                .r(a8).r(f8).k(h8)
                .R(a1).R(f1).K(h1)
        ), view);
    }
    @Test public void test_availableCastlingsAreDeterminedCorrectlyInCaseOfRooksMove_A1A8_H8H1() {
        //given
        MovesBuilder movesBuilder = new MovesBuilder(initialPosition(WHITE, b->b
                .r(a8).k(e8).r(h8)
                .R(a1).K(e1).R(h1)
        ));

        //a1->a8, expect only king castling for black
        //when
        ChessComponentView view = movesBuilder.cellLeftClicked(a1);
        //then
        assertCellPreparedToMove(view, a1);
        assertCellsAvailableToMoveTo(view, setOf(a2,a3,a4,a5,a6,a7,a8,b1,c1,d1));
        assertEqualsByChessmenTypes(chessBoard(b->b
                .r(a8).k(e8).r(h8)
                .R(a1).K(e1).R(h1)
        ), view);

        //when
        view = movesBuilder.cellLeftClicked(a8);
        //then
        assertNoCellPreparedToMove(view);
        assertNoCellsAvailableToMoveTo(view);
        assertEqualsByChessmenTypes(chessBoard(b->b
                .R(a8).k(e8).r(h8)
                .o(a1).K(e1).R(h1)
        ), view);

        //select black King, expect king castling
        //when
        view = movesBuilder.cellLeftClicked(e8);
        //then
        assertCellPreparedToMove(view, e8);
        assertCellsAvailableToMoveTo(view, setOf(d7,e7,f7,d8,f8,g8));
        assertEqualsByChessmenTypes(chessBoard(b->b
                .R(a8).k(e8).r(h8)
                .o(a1).K(e1).R(h1)
        ), view);

        //h8->h1, don't expect king castling for white
        //when
        view = movesBuilder.cellLeftClicked(h8);
        //then
        assertCellPreparedToMove(view, h8);
        assertCellsAvailableToMoveTo(view, setOf(h7,h6,h5,h4,h3,h2,h1,g8,f8));
        assertEqualsByChessmenTypes(chessBoard(b->b
                .R(a8).k(e8).r(h8)
                .o(a1).K(e1).R(h1)
        ), view);

        //when
        view = movesBuilder.cellLeftClicked(h1);
        //then
        assertNoCellPreparedToMove(view);
        assertNoCellsAvailableToMoveTo(view);
        assertEqualsByChessmenTypes(chessBoard(b->b
                .R(a8).k(e8).o(h8)
                .o(a1).K(e1).r(h1)
        ), view);

        //select white King, expect no castling
        //when
        view = movesBuilder.cellLeftClicked(e1);
        //then
        assertCellPreparedToMove(view, e1);
        assertCellsAvailableToMoveTo(view, setOf(d2,e2,f2,d1,f1));
        assertEqualsByChessmenTypes(chessBoard(b->b
                .R(a8).k(e8).o(h8)
                .o(a1).K(e1).r(h1)
        ), view);
    }
    @Test public void test_availableCastlingsAreDeterminedCorrectlyInCaseOfRooksMove_H8H1_A1A8() {
        //given
        MovesBuilder movesBuilder = new MovesBuilder(initialPosition(BLACK, b->b
                .r(a8).k(e8).r(h8)
                .R(a1).K(e1).R(h1)
        ));

        ChessComponentView view = movesBuilder.cellLeftClicked(e8);
        assertEquals(chessBoardView(b -> b
                .ur(a8).uo(b8).go(c8).go(d8).yk(e8).go(f8).go(g8).ur(h8)
                .uo(a7).uo(b7).uo(c7).go(d7).go(e7).go(f7).uo(g7).uo(h7)
                .uo(a6).uo(b6).uo(c6).uo(d6).uo(e6).uo(f6).uo(g6).uo(h6)
                .uo(a5).uo(b5).uo(c5).uo(d5).uo(e5).uo(f5).uo(g5).uo(h5)
                .uo(a4).uo(b4).uo(c4).uo(d4).uo(e4).uo(f4).uo(g4).uo(h4)
                .uo(a3).uo(b3).uo(c3).uo(d3).uo(e3).uo(f3).uo(g3).uo(h3)
                .uo(a2).uo(b2).uo(c2).uo(d2).uo(e2).uo(f2).uo(g2).uo(h2)
                .uR(a1).uo(b1).uo(c1).uo(d1).uK(e1).uo(f1).uo(g1).uR(h1)
        ), view.getChessBoard());

        view = movesBuilder.cellLeftClicked(h8);
        assertEquals(chessBoardView(b -> b
                .ur(a8).uo(b8).uo(c8).uo(d8).uk(e8).go(f8).go(g8).yr(h8)
                .uo(a7).uo(b7).uo(c7).uo(d7).uo(e7).uo(f7).uo(g7).go(h7)
                .uo(a6).uo(b6).uo(c6).uo(d6).uo(e6).uo(f6).uo(g6).go(h6)
                .uo(a5).uo(b5).uo(c5).uo(d5).uo(e5).uo(f5).uo(g5).go(h5)
                .uo(a4).uo(b4).uo(c4).uo(d4).uo(e4).uo(f4).uo(g4).go(h4)
                .uo(a3).uo(b3).uo(c3).uo(d3).uo(e3).uo(f3).uo(g3).go(h3)
                .uo(a2).uo(b2).uo(c2).uo(d2).uo(e2).uo(f2).uo(g2).go(h2)
                .uR(a1).uo(b1).uo(c1).uo(d1).uK(e1).uo(f1).uo(g1).gR(h1)
        ), view.getChessBoard());

        view = movesBuilder.cellLeftClicked(h1);
        assertEquals(chessBoardView(b -> b
                .ur(a8).uo(b8).uo(c8).uo(d8).uk(e8).uo(f8).uo(g8).uo(h8)
                .uo(a7).uo(b7).uo(c7).uo(d7).uo(e7).uo(f7).uo(g7).uo(h7)
                .uo(a6).uo(b6).uo(c6).uo(d6).uo(e6).uo(f6).uo(g6).uo(h6)
                .uo(a5).uo(b5).uo(c5).uo(d5).uo(e5).uo(f5).uo(g5).uo(h5)
                .uo(a4).uo(b4).uo(c4).uo(d4).uo(e4).uo(f4).uo(g4).uo(h4)
                .uo(a3).uo(b3).uo(c3).uo(d3).uo(e3).uo(f3).uo(g3).uo(h3)
                .uo(a2).uo(b2).uo(c2).uo(d2).uo(e2).uo(f2).uo(g2).uo(h2)
                .uR(a1).uo(b1).uo(c1).uo(d1).uK(e1).uo(f1).uo(g1).ur(h1)
        ), view.getChessBoard());

        view = movesBuilder.cellLeftClicked(e1);
        assertEquals(chessBoardView(b -> b
                .ur(a8).uo(b8).uo(c8).uo(d8).uk(e8).uo(f8).uo(g8).uo(h8)
                .uo(a7).uo(b7).uo(c7).uo(d7).uo(e7).uo(f7).uo(g7).uo(h7)
                .uo(a6).uo(b6).uo(c6).uo(d6).uo(e6).uo(f6).uo(g6).uo(h6)
                .uo(a5).uo(b5).uo(c5).uo(d5).uo(e5).uo(f5).uo(g5).uo(h5)
                .uo(a4).uo(b4).uo(c4).uo(d4).uo(e4).uo(f4).uo(g4).uo(h4)
                .uo(a3).uo(b3).uo(c3).uo(d3).uo(e3).uo(f3).uo(g3).uo(h3)
                .uo(a2).uo(b2).uo(c2).go(d2).go(e2).go(f2).uo(g2).uo(h2)
                .uR(a1).uo(b1).go(c1).go(d1).yK(e1).go(f1).uo(g1).ur(h1)
        ), view.getChessBoard());

        view = movesBuilder.cellLeftClicked(a1);
        assertEquals(chessBoardView(b -> b
                .gr(a8).uo(b8).uo(c8).uo(d8).uk(e8).uo(f8).uo(g8).uo(h8)
                .go(a7).uo(b7).uo(c7).uo(d7).uo(e7).uo(f7).uo(g7).uo(h7)
                .go(a6).uo(b6).uo(c6).uo(d6).uo(e6).uo(f6).uo(g6).uo(h6)
                .go(a5).uo(b5).uo(c5).uo(d5).uo(e5).uo(f5).uo(g5).uo(h5)
                .go(a4).uo(b4).uo(c4).uo(d4).uo(e4).uo(f4).uo(g4).uo(h4)
                .go(a3).uo(b3).uo(c3).uo(d3).uo(e3).uo(f3).uo(g3).uo(h3)
                .go(a2).uo(b2).uo(c2).uo(d2).uo(e2).uo(f2).uo(g2).uo(h2)
                .yR(a1).go(b1).go(c1).go(d1).uK(e1).uo(f1).uo(g1).ur(h1)
        ), view.getChessBoard());

        view = movesBuilder.cellLeftClicked(a8);
        assertEquals(chessBoardView(b -> b
                .uR(a8).uo(b8).uo(c8).uo(d8).uk(e8).uo(f8).uo(g8).uo(h8)
                .uo(a7).uo(b7).uo(c7).uo(d7).uo(e7).uo(f7).uo(g7).uo(h7)
                .uo(a6).uo(b6).uo(c6).uo(d6).uo(e6).uo(f6).uo(g6).uo(h6)
                .uo(a5).uo(b5).uo(c5).uo(d5).uo(e5).uo(f5).uo(g5).uo(h5)
                .uo(a4).uo(b4).uo(c4).uo(d4).uo(e4).uo(f4).uo(g4).uo(h4)
                .uo(a3).uo(b3).uo(c3).uo(d3).uo(e3).uo(f3).uo(g3).uo(h3)
                .uo(a2).uo(b2).uo(c2).uo(d2).uo(e2).uo(f2).uo(g2).uo(h2)
                .uo(a1).uo(b1).uo(c1).uo(d1).uK(e1).uo(f1).uo(g1).ur(h1)
        ), view.getChessBoard());

        view = movesBuilder.cellLeftClicked(e8);
        assertEquals(chessBoardView(b -> b
                .uR(a8).uo(b8).uo(c8).go(d8).yk(e8).go(f8).uo(g8).uo(h8)
                .uo(a7).uo(b7).uo(c7).go(d7).go(e7).go(f7).uo(g7).uo(h7)
                .uo(a6).uo(b6).uo(c6).uo(d6).uo(e6).uo(f6).uo(g6).uo(h6)
                .uo(a5).uo(b5).uo(c5).uo(d5).uo(e5).uo(f5).uo(g5).uo(h5)
                .uo(a4).uo(b4).uo(c4).uo(d4).uo(e4).uo(f4).uo(g4).uo(h4)
                .uo(a3).uo(b3).uo(c3).uo(d3).uo(e3).uo(f3).uo(g3).uo(h3)
                .uo(a2).uo(b2).uo(c2).uo(d2).uo(e2).uo(f2).uo(g2).uo(h2)
                .uo(a1).uo(b1).uo(c1).uo(d1).uK(e1).uo(f1).uo(g1).ur(h1)
        ), view.getChessBoard());

    }

}