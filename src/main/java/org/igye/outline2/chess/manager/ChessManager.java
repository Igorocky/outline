package org.igye.outline2.chess.manager;

import org.apache.commons.lang3.StringUtils;
import org.igye.outline2.chess.dto.ChessComponentResponse;
import org.igye.outline2.chess.model.CellCoords;
import org.igye.outline2.chess.model.ChessmanColor;
import org.igye.outline2.chess.model.Move;
import org.igye.outline2.rpc.Default;
import org.igye.outline2.rpc.RpcMethod;
import org.igye.outline2.websocket.State;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component(ChessManager.CHESSBOARD)
@Scope("prototype")
public class ChessManager extends State implements ChessComponentStateManager {
    public static final String EMPTY_BOARD_FEN = "8/8/8/8/8/8/8/8 w - - 0 1";
    public static final String CHESSBOARD = "chessboard";
    @Value("${chess.stockfish.cmd:null}")
    private String stockfishCmd;
    private ChessComponentStateManager stateManager;

    @PostConstruct
    public void postConstruct() {
        stateManager = new PositionBuilder("8/8/8/8/8/8/8/8 w - - 0 1");
    }

    @RpcMethod
    public ChessComponentResponse getCurrentState() {
        return toView();
    }

    @RpcMethod
    public ChessComponentResponse loadFromPgn(@Default("\"\"") String pgn, ChessComponentStage tabToOpen,
                                              @Default("false") boolean autoResponse,
                                              @Default("null") List<String> commands) {
        final MovesBuilder movesBuilder = new MovesBuilder(stockfishCmd, new Move(EMPTY_BOARD_FEN));
        this.stateManager = movesBuilder;
        if (!StringUtils.isBlank(pgn)) {
            movesBuilder.loadFromPgn(pgn);
        }
        chessTabSelected(tabToOpen);
        if (autoResponse) {
            movesBuilder.setAutoResponseForOpponent();
        }
        if (commands != null) {
            commands.forEach(cmd -> execChessCommand(cmd));
        }
        return toView();
    }

    @Override
    @RpcMethod
    public ChessComponentResponse cellLeftClicked(CellCoords coords) {
        stateManager.cellLeftClicked(coords);
        return toView();
    }

    @RpcMethod
    @Override
    public ChessComponentResponse execChessCommand(String command) {
        if (!StringUtils.isBlank(command)) {
            stateManager.execChessCommand(command);
        }
        return toView();
    }

    @RpcMethod
    @Override
    public ChessComponentResponse setColorToMove(ChessmanColor colorToMove) {
        stateManager.setColorToMove(colorToMove);
        return toView();
    }

    @RpcMethod
    @Override
    public ChessComponentResponse changeCastlingAvailability(ChessmanColor color, boolean isLong) {
        stateManager.changeCastlingAvailability(color, isLong);
        return toView();
    }

    @RpcMethod
    @Override
    public ChessComponentResponse setPositionFromFen(String fen) {
        stateManager.setPositionFromFen(fen);
        return toView();
    }

    @RpcMethod
    @Override
    public ChessComponentResponse showCorrectMove() {
        stateManager.showCorrectMove();
        return toView();
    }

    @Override
    public ChessComponentResponse setAutoResponseForOpponent() {
        stateManager.setAutoResponseForOpponent();
        return toView();
    }

    @RpcMethod
    public ChessComponentResponse chessTabSelected(ChessComponentStage tab) {
        if (stateManager instanceof PositionBuilder) {
            if (tab.equals(ChessComponentStage.MOVES)) {
                final PositionBuilder positionBuilder = (PositionBuilder) this.stateManager;
                stateManager = new MovesBuilder(stockfishCmd, positionBuilder.getInitialPosition());
            }
        } else if (stateManager instanceof MovesBuilder) {
            final MovesBuilder movesBuilder = (MovesBuilder) this.stateManager;
            if (tab.equals(ChessComponentStage.INITIAL_POSITION)) {
                stateManager = new PositionBuilder(movesBuilder.getInitialPosition());
            } else if (tab.equals(ChessComponentStage.PRACTICE_SEQUENCE)) {
                movesBuilder.setPracticeMode(true);
            } else if (tab.equals(ChessComponentStage.MOVES)) {
                movesBuilder.setPracticeMode(false);
            }
        }
        return toView();
    }

    @Override
    public ChessComponentResponse toView() {
        final ChessComponentResponse response = stateManager.toView();
        response.getChessComponentView().setPgn(getPgn());
        return response;
    }

    private String getPgn() {
        if (stateManager instanceof PositionBuilder) {
            return new MovesBuilder(
                    stockfishCmd,
                    ((PositionBuilder) stateManager).getInitialPosition()
            ).toPgn();
        } else {
            return ((MovesBuilder)stateManager).toPgn();
        }
    }
}
