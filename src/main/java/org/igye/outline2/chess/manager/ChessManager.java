package org.igye.outline2.chess.manager;

import org.apache.commons.lang3.StringUtils;
import org.igye.outline2.chess.dto.ChessComponentDto;
import org.igye.outline2.chess.model.CellCoords;
import org.igye.outline2.rpc.RpcMethod;
import org.igye.outline2.rpc.RpcMethodsCollection;
import org.springframework.stereotype.Component;

@RpcMethodsCollection
@Component
public class ChessManager implements ChessComponentStateManager {
    private ChessComponentStateManager stateManager;

    @RpcMethod
    public ChessComponentDto initialState() {
        stateManager = new PositionBuilder(StringUtils.EMPTY);
        return stateManager.toDto();
    }

    @Override
    @RpcMethod
    public ChessComponentDto cellLeftClicked(CellCoords coords) {
        return stateManager.cellLeftClicked(coords);
    }

    @RpcMethod
    public ChessComponentDto chessTabSelected(ChessComponentStage tab) {
        if (stateManager instanceof PositionBuilder) {
            if (tab.equals(ChessComponentStage.MOVES)) {
                final PositionBuilder positionBuilder = (PositionBuilder) this.stateManager;
                stateManager = new MovesBuilder(positionBuilder.getNextMoveColor(), positionBuilder.getPosition());
            }
        } else if (stateManager instanceof MovesBuilder) {
            if (tab.equals(ChessComponentStage.INITIAL_POSITION)) {
                final MovesBuilder movesBuilder = (MovesBuilder) this.stateManager;
                stateManager = new PositionBuilder(movesBuilder.getInitialPosition());
            }
        }
        return stateManager.toDto();
    }

    @Override
    public ChessComponentDto toDto() {
        return stateManager.toDto();
    }
}
