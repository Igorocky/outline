package org.igye.outline2.chess.manager;

import org.igye.outline2.chess.dto.CellDto;
import org.igye.outline2.chess.dto.ChessBoardComponentStateDto;
import org.igye.outline2.chess.dto.ChessBoardDto;
import org.igye.outline2.rpc.RpcMethod;
import org.igye.outline2.rpc.RpcMethodsCollection;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@RpcMethodsCollection
@Component
public class ChessBoardManager {
    List<List<CellDto>> cells = new ArrayList<>();

    public ChessBoardManager() {
        for (int x = 0; x < 8; x++) {
            cells.add(new ArrayList<>());
            for (int y = 0; y < 8; y++) {
                cells.get(x).add(
                        CellDto.builder()
                                .x(x)
                                .y(y)
                                .backgroundColor((x+y)%2==0?"lightseagreen":"white")
                                .build()
                );
            }
        }
    }

    @RpcMethod
    public ChessBoardComponentStateDto cellClicked(int x, int y) {
        cells.get(x).get(y).setHighlighted(true);
        return chessboard();
    }

    @RpcMethod
    public ChessBoardComponentStateDto initialChessboard() {
        return ChessBoardComponentStateDto.builder()
                .chessBoard(ChessBoardDto.builder()
                        .cells(cells)
                        .build()
                ).build();
    }

    private ChessBoardComponentStateDto chessboard() {
        return ChessBoardComponentStateDto.builder()
                .chessBoard(ChessBoardDto.builder()
                        .cells(cells)
                        .build()
                ).build();
    }
}
