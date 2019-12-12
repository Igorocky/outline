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
    public ChessComponentResponse getCurrentState() {
        return stateManager.toView();
    }

    @RpcMethod
    public ChessComponentResponse getPgnToSave() {
        if (stateManager instanceof PositionBuilder) {
            return ChessComponentResponse.builder()
                    .savePgn(
                            new MovesBuilder(
                                    stockfishCmd,
                                    ((PositionBuilder) stateManager).getInitialPosition()
                            ).toPgn()
                    )
                    .build();
        } else {
            return ChessComponentResponse.builder().savePgn(((MovesBuilder)stateManager).toPgn()).build();
        }
    }

    @RpcMethod
    public ChessComponentResponse loadFromPgn(@Default("\"\"") String pgn, ChessComponentStage tabToOpen) {
        final MovesBuilder movesBuilder = new MovesBuilder(stockfishCmd, new Move(EMPTY_BOARD_FEN));
        this.stateManager = movesBuilder;
        if (!StringUtils.isBlank(pgn)) {
            movesBuilder.loadFromPgn(pgn);
        }
        return chessTabSelected(tabToOpen);
    }

    @Override
    @RpcMethod
    public ChessComponentResponse cellLeftClicked(CellCoords coords) {
        return stateManager.cellLeftClicked(coords);
    }

    @RpcMethod
    @Override
    public ChessComponentResponse execChessCommand(String command) {
        if (!StringUtils.isBlank(command)) {
            return stateManager.execChessCommand(command);
        } else {
            return stateManager.toView();
        }
    }

    @RpcMethod
    @Override
    public ChessComponentResponse setColorToMove(ChessmanColor colorToMove) {
        return stateManager.setColorToMove(colorToMove);
    }

    @RpcMethod
    @Override
    public ChessComponentResponse changeCastlingAvailability(ChessmanColor color, boolean isLong) {
        return stateManager.changeCastlingAvailability(color, isLong);
    }

    @RpcMethod
    @Override
    public ChessComponentResponse setPositionFromFen(String fen) {
        return stateManager.setPositionFromFen(fen);
    }

    @RpcMethod
    @Override
    public ChessComponentResponse showCorrectMove() {
        return stateManager.showCorrectMove();
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
        return stateManager.toView();
    }

    @Override
    public ChessComponentResponse toView() {
        return stateManager.toView();
    }
}
