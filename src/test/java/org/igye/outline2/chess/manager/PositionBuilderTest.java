package org.igye.outline2.chess.manager;

import org.igye.outline2.chess.dto.ChessComponentView;
import org.igye.outline2.chess.model.CellCoords;
import org.junit.Test;

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
import static org.igye.outline2.controllers.chess.ChessTestUtils.chessBoardView;

public class PositionBuilderTest {
    @Test
    public void secondClickOnTheSameCellWithTheSamePieceRemovesThisPieceFromTheCell() {
        //given
        PositionBuilder positionBuilder = new PositionBuilder("8/8/8/8/8/8/8/8 w - -");

        positionBuilder.cellLeftClicked(new CellCoords(11,1));
        ChessComponentView view = positionBuilder.cellLeftClicked(f6).getChessComponentView();
        assertBoardsEqual(chessBoardView(b -> b
                .__(a8).__(b8).__(c8).__(d8).__(e8).__(f8).__(g8).__(h8)
                .__(a7).__(b7).__(c7).__(d7).__(e7).__(f7).__(g7).__(h7)
                .__(a6).__(b6).__(c6).__(d6).__(e6)._n(f6).__(g6).__(h6)
                .__(a5).__(b5).__(c5).__(d5).__(e5).__(f5).__(g5).__(h5)
                .__(a4).__(b4).__(c4).__(d4).__(e4).__(f4).__(g4).__(h4)
                .__(a3).__(b3).__(c3).__(d3).__(e3).__(f3).__(g3).__(h3)
                .__(a2).__(b2).__(c2).__(d2).__(e2).__(f2).__(g2).__(h2)
                .__(a1).__(b1).__(c1).__(d1).__(e1).__(f1).__(g1).__(h1)
        ), view.getChessBoard());

        positionBuilder.cellLeftClicked(new CellCoords(12,1));
        view = positionBuilder.cellLeftClicked(f6).getChessComponentView();
        assertBoardsEqual(chessBoardView(b -> b
                .__(a8).__(b8).__(c8).__(d8).__(e8).__(f8).__(g8).__(h8)
                .__(a7).__(b7).__(c7).__(d7).__(e7).__(f7).__(g7).__(h7)
                .__(a6).__(b6).__(c6).__(d6).__(e6)._b(f6).__(g6).__(h6)
                .__(a5).__(b5).__(c5).__(d5).__(e5).__(f5).__(g5).__(h5)
                .__(a4).__(b4).__(c4).__(d4).__(e4).__(f4).__(g4).__(h4)
                .__(a3).__(b3).__(c3).__(d3).__(e3).__(f3).__(g3).__(h3)
                .__(a2).__(b2).__(c2).__(d2).__(e2).__(f2).__(g2).__(h2)
                .__(a1).__(b1).__(c1).__(d1).__(e1).__(f1).__(g1).__(h1)
        ), view.getChessBoard());

        view = positionBuilder.cellLeftClicked(f6).getChessComponentView();
        assertBoardsEqual(chessBoardView(b -> b
                .__(a8).__(b8).__(c8).__(d8).__(e8).__(f8).__(g8).__(h8)
                .__(a7).__(b7).__(c7).__(d7).__(e7).__(f7).__(g7).__(h7)
                .__(a6).__(b6).__(c6).__(d6).__(e6).__(f6).__(g6).__(h6)
                .__(a5).__(b5).__(c5).__(d5).__(e5).__(f5).__(g5).__(h5)
                .__(a4).__(b4).__(c4).__(d4).__(e4).__(f4).__(g4).__(h4)
                .__(a3).__(b3).__(c3).__(d3).__(e3).__(f3).__(g3).__(h3)
                .__(a2).__(b2).__(c2).__(d2).__(e2).__(f2).__(g2).__(h2)
                .__(a1).__(b1).__(c1).__(d1).__(e1).__(f1).__(g1).__(h1)
        ), view.getChessBoard());
    }

}