package org.igye.outline2.chess.manager;

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
        stateManager = new PositionBuilder();
        return stateManager.toDto();
    }

    @Override
    @RpcMethod
    public ChessComponentDto cellLeftClicked(CellCoords coords) {
        return stateManager.cellLeftClicked(coords);
    }

    @Override
    public ChessComponentDto toDto() {
        return stateManager.toDto();
    }
}
