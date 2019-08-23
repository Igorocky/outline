package org.igye.outline2.chess.manager;

import lombok.Getter;
import lombok.Setter;
import org.igye.outline2.chess.model.ChessmanColor;
import org.igye.outline2.chess.model.Move;
import org.igye.outline2.exceptions.OutlineException;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class MovesBuilderState {
    public static final int MAX_DEPTH = 30;
    public static final int MAX_MOVE_TIME = 10;

    private GamePosition initialPosition;
    private GamePosition currPosition;
    private List<Move> preparedMoves = new ArrayList<>();
    private boolean choseChessmanTypeDialogOpened;
    private String commandErrorMsg;
    private String commandResponseMsg;
    private ChessmanColor autoResponseForColor;
    private boolean chessbordIsHidden;
    private int depth = MAX_DEPTH;
    private int movetimeSec = MAX_MOVE_TIME;

    public MovesBuilderState(Move initialPosition) {
        this.initialPosition = new GamePosition(initialPosition);
        currPosition = this.initialPosition;
    }

    public void appendPreparedMoveToHistory() {
        if (preparedMoves == null || preparedMoves.size() != 1) {
            throw new OutlineException("preparedMoves == null || preparedMoves.size() != 1");
        }
        GamePosition newPosition = new GamePosition(currPosition, preparedMoves.get(0));
        currPosition.getChildren().add(newPosition);
        currPosition = newPosition;
        preparedMoves = null;
    }
}