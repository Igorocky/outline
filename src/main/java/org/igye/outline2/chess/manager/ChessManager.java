package org.igye.outline2.chess.manager;

import org.apache.commons.lang3.StringUtils;
import org.igye.outline2.chess.dto.ChessComponentResponse;
import org.igye.outline2.chess.dto.ParsedPgnDto;
import org.igye.outline2.chess.manager.analyse.PgnParser;
import org.igye.outline2.chess.model.CellCoords;
import org.igye.outline2.chess.model.ChessmanColor;
import org.igye.outline2.chess.model.Move;
import org.igye.outline2.rpc.Default;
import org.igye.outline2.rpc.RpcIgnore;
import org.igye.outline2.rpc.RpcMethod;
import org.igye.outline2.websocket.State;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.function.Consumer;

import static org.igye.outline2.chess.manager.MovesBuilder.SET_DEPTH_CMD;

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
    public synchronized ChessComponentResponse loadFromPgn(@Default("\"\"") String pgn,
                                                           @Default("null") ChessComponentStage tabToOpen,
                                                           @Default("false") boolean autoResponse,
                                                           @Default("null") Integer depth,
                                                           @Default("null") List<String> commands) {
        boolean samePgn = stateManager instanceof MovesBuilder && pgn.equals(((MovesBuilder) stateManager).toPgn());
        if (!samePgn) {
            final MovesBuilder movesBuilder = new MovesBuilder(stockfishCmd, new Move(EMPTY_BOARD_FEN));
            this.stateManager = movesBuilder;
            if (!StringUtils.isBlank(pgn)) {
                final ParsedPgnDto parsedPgn = PgnParser.parsePgn(pgn);
                movesBuilder.loadFromPgn(parsedPgn);
                if (tabToOpen == null) {
                    if (parsedPgn.getPositions().isEmpty()) {
                        tabToOpen = ChessComponentStage.MOVES;
                    } else {
                        tabToOpen = ChessComponentStage.PRACTICE_SEQUENCE;
                    }
                }
            }
            chessTabSelected(tabToOpen);
            if (autoResponse) {
                movesBuilder.setAutoResponseForOpponent();
            }
            if (depth != null) {
                execChessCommand(SET_DEPTH_CMD + " " + depth, null);
            }
            if (commands != null) {
                commands.forEach(cmd -> execChessCommand(cmd, null));
            }
        }
        return toView();
    }

    @RpcMethod
    public synchronized ChessComponentResponse loadFromFen(String fen, ChessComponentStage tabToOpen,
                                              @Default("false") boolean autoResponse,
                                              @Default("null") List<String> commands) {
        stateManager = new MovesBuilder(
                stockfishCmd,
                new Move(fen.replaceAll("!", "/").replaceAll("_", " "))
        );
        chessTabSelected(tabToOpen);
        if (autoResponse) {
            stateManager.setAutoResponseForOpponent();
        }
        if (commands != null) {
            commands.forEach(cmd -> execChessCommand(cmd, null));
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
    public ChessComponentResponse execChessCommand(String command, @RpcIgnore Consumer<String> progressCallback) {
        if (!StringUtils.isBlank(command)) {
            stateManager.execChessCommand(
                    command,
                    progressInfo -> {
                        ChessComponentResponse view = toView();
                        view.getChessComponentView().setCommandProgressMsg(progressInfo);
                        sendMessageToFe(view);
                    }
            );
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
    @Override
    public ChessComponentResponse hideCommandResponseMsg() {
        stateManager.hideCommandResponseMsg();
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
