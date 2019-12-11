package org.igye.outline2.chess.manager;

import org.apache.commons.lang3.StringUtils;
import org.igye.outline2.chess.dto.ChessComponentView;
import org.igye.outline2.chess.model.CellCoords;
import org.igye.outline2.chess.model.ChessmanColor;
import org.igye.outline2.chess.model.Move;
import org.igye.outline2.rpc.RpcMethod;
import org.igye.outline2.websocket.State;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component("chessboard")
@Scope("prototype")
public class ChessManager extends State implements ChessComponentStateManager {
    public static final String EMPTY_BOARD_FEN = "8/8/8/8/8/8/8/8 w - - 0 1";
    @Value("${chess.stockfish.cmd:null}")
    private String stockfishCmd;
    private ChessComponentStateManager stateManager;

    @PostConstruct
    public void postConstruct() {
        stateManager = new PositionBuilder("8/8/8/8/8/8/8/8 w - - 0 1");
    }

    @RpcMethod
    public ChessComponentView getCurrentState() {
        return stateManager.toView();
    }

    @RpcMethod
    public ChessComponentView loadFromPgn(String pgn, ChessComponentStage tabToOpen) {
        final MovesBuilder movesBuilder = new MovesBuilder(stockfishCmd, new Move(EMPTY_BOARD_FEN));
        this.stateManager = movesBuilder;
        movesBuilder.loadFromPgn(pgn);
        return chessTabSelected(tabToOpen);
    }

    @Override
    @RpcMethod
    public ChessComponentView cellLeftClicked(CellCoords coords) {
        return stateManager.cellLeftClicked(coords);
    }

    @RpcMethod
    @Override
    public ChessComponentView execChessCommand(String command) {
        if (!StringUtils.isBlank(command)) {
            return stateManager.execChessCommand(command);
        } else {
            return stateManager.toView();
        }
    }

    @RpcMethod
    @Override
    public ChessComponentView setColorToMove(ChessmanColor colorToMove) {
        return stateManager.setColorToMove(colorToMove);
    }

    @RpcMethod
    @Override
    public ChessComponentView changeCastlingAvailability(ChessmanColor color, boolean isLong) {
        return stateManager.changeCastlingAvailability(color, isLong);
    }

    @RpcMethod
    @Override
    public ChessComponentView setPositionFromFen(String fen) {
        return stateManager.setPositionFromFen(fen);
    }

    @RpcMethod
    public ChessComponentView chessTabSelected(ChessComponentStage tab) {
        if (stateManager instanceof PositionBuilder) {
            if (tab.equals(ChessComponentStage.MOVES)) {
                final PositionBuilder positionBuilder = (PositionBuilder) this.stateManager;
                stateManager = new MovesBuilder(stockfishCmd, positionBuilder.getInitialPosition());
            }
        } else if (stateManager instanceof MovesBuilder) {
            final MovesBuilder movesBuilder = (MovesBuilder) this.stateManager;
            if (tab.equals(ChessComponentStage.INITIAL_POSITION)) {
                stateManager = new PositionBuilder(movesBuilder.getInitialPosition());
            } else if (tab.equals(ChessComponentStage.PRACTISE_SEQUENCE)) {
                movesBuilder.setPracticeMode(true);
            } else if (tab.equals(ChessComponentStage.MOVES)) {
                movesBuilder.setPracticeMode(false);
            }
        }
        return stateManager.toView();
    }

    @Override
    public ChessComponentView toView() {
        return stateManager.toView();
    }
}
