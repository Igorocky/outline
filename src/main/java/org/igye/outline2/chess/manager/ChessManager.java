package org.igye.outline2.chess.manager;

import org.apache.commons.lang3.StringUtils;
import org.igye.outline2.chess.dto.ChessComponentView;
import org.igye.outline2.chess.model.CellCoords;
import org.igye.outline2.chess.model.ChessmanColor;
import org.igye.outline2.rpc.RpcMethod;
import org.igye.outline2.websocket.State;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component("chessboard")
@Scope("prototype")
public class ChessManager extends State implements ChessComponentStateManager {
    @Value("${chess.stockfish.cmd:null}")
    private String stockfishCmd;
    private ChessComponentStateManager stateManager;

    @PostConstruct
    public void postConstruct() {
        stateManager = new PositionBuilder("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq -");
    }

    @RpcMethod
    public ChessComponentView getCurrentState() {
        return stateManager.toView();
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
    public ChessComponentView chessTabSelected(ChessComponentStage tab) {
        if (stateManager instanceof PositionBuilder) {
            if (tab.equals(ChessComponentStage.MOVES)) {
                final PositionBuilder positionBuilder = (PositionBuilder) this.stateManager;
                stateManager = new MovesBuilder(stockfishCmd, positionBuilder.getInitialPosition());
            }
        } else if (stateManager instanceof MovesBuilder) {
            if (tab.equals(ChessComponentStage.INITIAL_POSITION)) {
                final MovesBuilder movesBuilder = (MovesBuilder) this.stateManager;
                stateManager = new PositionBuilder(movesBuilder.getInitialPosition());
            }
        }
        return stateManager.toView();
    }

    @Override
    public ChessComponentView toView() {
        return stateManager.toView();
    }
}
