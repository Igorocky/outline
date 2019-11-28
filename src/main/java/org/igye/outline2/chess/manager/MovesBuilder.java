package org.igye.outline2.chess.manager;

import org.igye.outline2.chess.dto.ChessBoardCellView;
import org.igye.outline2.chess.dto.ChessBoardView;
import org.igye.outline2.chess.dto.ChessComponentView;
import org.igye.outline2.chess.dto.ChessViewConverter;
import org.igye.outline2.chess.dto.HistoryView;
import org.igye.outline2.chess.dto.MoveView;
import org.igye.outline2.chess.model.CellCoords;
import org.igye.outline2.chess.model.ChessBoard;
import org.igye.outline2.chess.model.ChessmanColor;
import org.igye.outline2.chess.model.ChessmanType;
import org.igye.outline2.chess.model.Move;
import org.igye.outline2.chess.model.ParseMoveException;
import org.igye.outline2.chess.model.PieceShape;
import org.igye.outline2.common.Function4;
import org.igye.outline2.exceptions.OutlineException;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.igye.outline2.chess.manager.MovesBuilderState.MAX_DEPTH;
import static org.igye.outline2.chess.manager.MovesBuilderState.MAX_MOVE_TIME;
import static org.igye.outline2.chess.model.ChessmanColor.BLACK;
import static org.igye.outline2.chess.model.ChessmanColor.WHITE;
import static org.igye.outline2.chess.model.ChessmanType.BLACK_BISHOP;
import static org.igye.outline2.chess.model.ChessmanType.BLACK_KNIGHT;
import static org.igye.outline2.chess.model.ChessmanType.BLACK_QUEEN;
import static org.igye.outline2.chess.model.ChessmanType.BLACK_ROOK;
import static org.igye.outline2.chess.model.ChessmanType.WHITE_BISHOP;
import static org.igye.outline2.chess.model.ChessmanType.WHITE_KNIGHT;
import static org.igye.outline2.chess.model.ChessmanType.WHITE_QUEEN;
import static org.igye.outline2.chess.model.ChessmanType.WHITE_ROOK;
import static org.igye.outline2.common.OutlineUtils.listOf;

public class MovesBuilder implements ChessComponentStateManager {
    private static final String PREPARED_TO_MOVE_COLOR = "#FFFF00";
    private static final String AVAILABLE_TO_MOVE_TO_COLOR = "#90EE90";
    private static final String CHOOSE_CHESSMAN_TYPE_COLOR = "#0000AA";
    private static final String PREV_POSITION_CMD = "p";
    private static final String NEXT_POSITION_CMD = "n";
    private static final String GO_TO_POSITION_CMD = "g";
    private static final String GO_TO_END_POSITION_CMD = "e";
    private static final String GO_TO_START_POSITION_CMD = "s";
    private static final String DELETE_ALL_TO_THE_RIGHT_CMD = "rr";
    private static final String GENERATE_NEXT_MOVE_CMD = "nn";
    private static final String AUTO_RESPONSE_CMD = "aa";
    private static final String HIDE_SHOW_CHESSBOARD_CMD = "b";
    private static final String SET_DEPTH_CMD = "d";
    private static final String SET_MOVE_TIME_CMD = "t";

    private final String runStockfishCmd;
    private MovesBuilderState state;
    private Map<String, Consumer<String[]>> commands;

    public MovesBuilder(String runStockfishCmd, Move initialPosition) {
        this.runStockfishCmd = runStockfishCmd;
        state = new MovesBuilderState(initialPosition);
        initCommandMap();
    }

    @Override
    public ChessComponentView toView() {
        ChessComponentView chessComponentView = new ChessComponentView();
        chessComponentView.setTab(ChessComponentStage.MOVES);
        if (!state.isChessbordIsHidden()) {
            renderChessboard(chessComponentView);
        }
        chessComponentView.setHistory(createHistoryView());
        if (state.getCommandErrorMsg() != null) {
            chessComponentView.setCommandErrorMsg(state.getCommandErrorMsg());
        }
        if (state.getCommandResponseMsg() != null) {
            chessComponentView.setCommandResponseMsg(state.getCommandResponseMsg());
        }
        return chessComponentView;
    }

    private void renderChessboard(ChessComponentView chessComponentView) {
        chessComponentView.setChessBoard(ChessViewConverter.toDto(getCurrentPosition()));
        final Move currMove = state.getCurrPosition().getMove();
        final ChessBoardView chessBoardView = chessComponentView.getChessBoard();
        if (/*state.getAutoResponseForColor() != null*/1 == 2) {
            state.getCurrPosition().getMove().getAllCellsAttackedBy(state.getAutoResponseForColor()).forEach(
                    c-> chessBoardView.setBorderColorForCell(c, "#F08080")
            );
        }
        if (!CollectionUtils.isEmpty(state.getPreparedMoves())) {
            if (state.isChoseChessmanTypeDialogOpened()) {
                final Move preparedMove = state.getPreparedMoves().get(0);
                final int x = preparedMove.getTo().getX();
                final ChessmanColor side = preparedMove.getColorOfWhoMadeMove();
                final int base = side == WHITE ? 7 : 3;
                putChessmanTypeToChoose(chessBoardView, x, base, side == WHITE ? WHITE_QUEEN : BLACK_QUEEN);
                putChessmanTypeToChoose(chessBoardView, x, base-1, side == WHITE ? WHITE_ROOK : BLACK_ROOK);
                putChessmanTypeToChoose(chessBoardView, x, base-2, side == WHITE ? WHITE_BISHOP : BLACK_BISHOP);
                putChessmanTypeToChoose(chessBoardView, x, base-3, side == WHITE ? WHITE_KNIGHT : BLACK_KNIGHT);
            } else {
                chessBoardView.setBorderColorForCell(
                        state.getPreparedMoves().get(0).getFrom(), PREPARED_TO_MOVE_COLOR
                );
                state.getPreparedMoves().forEach(move ->
                        chessBoardView.setBorderColorForCell(move.getTo(), AVAILABLE_TO_MOVE_TO_COLOR)
                );
            }
        } else {
            if (currMove.getFrom() != null) {
                chessBoardView.setBorderColorForCell(currMove.getFrom(), PREPARED_TO_MOVE_COLOR);
            }
            if (currMove.getTo() != null) {
                chessBoardView.setBorderColorForCell(currMove.getTo(), AVAILABLE_TO_MOVE_TO_COLOR);
            }
        }
    }

    private void putChessmanTypeToChoose(ChessBoardView chessBoard, int x, int y, ChessmanType chessmanType) {
        ChessBoardCellView cell = chessBoard.getCell(x, y);
        cell.setBorderColor(CHOOSE_CHESSMAN_TYPE_COLOR);
        cell.setCode(chessmanType.getCode());
    }

    private void initCommandMap() {
        commands = new HashMap<>();
        commands.put(HIDE_SHOW_CHESSBOARD_CMD, args -> hideShowChessboard());
        commands.put(PREV_POSITION_CMD, args -> goToPrevPosition());
        commands.put(NEXT_POSITION_CMD, args -> goToNextPosition());
        commands.put(GENERATE_NEXT_MOVE_CMD, args -> generateNextMove());
        commands.put(AUTO_RESPONSE_CMD, args -> setAutoresponse());
        commands.put(DELETE_ALL_TO_THE_RIGHT_CMD, args -> deleteAllToTheRight());
        commands.put(GO_TO_START_POSITION_CMD, args -> goToStartPosition());
        commands.put(GO_TO_END_POSITION_CMD, args -> goToEndPosition());
        commands.put(GO_TO_POSITION_CMD, args -> {
            final String destination = args[1];
            goToPosition(
                    Integer.parseInt(destination.substring(0, destination.length()-1)),
                    destination.charAt(destination.length()-1) == 'w' ? WHITE : BLACK
            );
        });
        commands.put(SET_DEPTH_CMD, args -> {
            final int depth = Integer.parseInt(args[1]);
            state.setDepth((1 <= depth && depth <= MAX_DEPTH) ? depth : MAX_DEPTH);
        });
        commands.put(SET_MOVE_TIME_CMD, args -> {
            final int moveTime = Integer.parseInt(args[1]);
            state.setMovetimeSec((1 <= moveTime && moveTime <= MAX_MOVE_TIME) ? moveTime : MAX_MOVE_TIME);
        });
    }

    private void goToPosition(int feMoveNumber, ChessmanColor color) {
        traverseHistory((position, currFeMoveNumber, move, selected) -> {
            if (currFeMoveNumber == feMoveNumber && move.getColorOfWhoMadeMove() == color) {
                state.setCurrPosition(position);
                return false;
            }
            return true;
        });
    }

    private void hideShowChessboard() {
        state.setChessbordIsHidden(!state.isChessbordIsHidden());
    }

    private void goToPrevPosition() {
        final GamePosition prevPosition = state.getCurrPosition().getParent();
        if (prevPosition != null) {
            state.setCurrPosition(prevPosition);
        }
    }

    private void goToNextPosition() {
        final GamePosition nextPosition = getNextOnlyPosition(state.getCurrPosition());
        if (nextPosition != null) {
            state.setCurrPosition(nextPosition);
        }
    }

    private void generateNextMove() {
        if (canMakeMove()) {
            final Move currMove = state.getCurrPosition().getMove();
            if (!currMove.isStaleMate() && !currMove.isCheckMate()) {
                Move nextMove = null;
                try {
                    nextMove = StockFishRunner.getNextMove(
                            runStockfishCmd, currMove, state.getDepth(), state.getMovetimeSec()
                    );
                } catch (IOException ex) {
                    throw new OutlineException(ex);
                }
                state.setPreparedMoves(listOf(nextMove));
                state.appendPreparedMoveToHistory();
                state.setCommandResponseMsg(state.getCurrPosition().getMove().getShortNotation());
            }
        }
    }

    private void goToEndPosition() {
        GamePosition currPosition = state.getCurrPosition();
        while (getNextOnlyPosition(currPosition) != null) {
            currPosition = getNextOnlyPosition(currPosition);
        }
        state.setCurrPosition(currPosition);
    }

    private void goToStartPosition() {
        state.setCurrPosition(state.getInitialPosition());
    }

    private void deleteAllToTheRight() {
        state.getCurrPosition().setChildren(new ArrayList<>());
    }

    private void setAutoresponse() {
        if (state.getAutoResponseForColor()!=null) {
            state.setAutoResponseForColor(null);
        } else {
            if (canMakeMove()) {
                state.setAutoResponseForColor(state.getCurrPosition().getMove().getColorOfWhoMadeMove());
                execChessCommand(GENERATE_NEXT_MOVE_CMD);
            }
        }
    }

    @Override
    public ChessComponentView cellLeftClicked(CellCoords coordsClicked) {
        if (state.isChoseChessmanTypeDialogOpened()) {
            processPawnOnLastLine(coordsClicked);
        } else if (canMakeMove()) {
            final List<Move> preparedMoves = state.getPreparedMoves();
            if (CollectionUtils.isEmpty(preparedMoves)) {
                List<Move> possibleMoves = state.getCurrPosition().getMove().getPossibleNextMoves(coordsClicked);
                if (!possibleMoves.isEmpty()) {
                    state.setPreparedMoves(possibleMoves);
                }
            } else if (coordsClicked.equals(preparedMoves.get(0).getFrom())) {
                state.setPreparedMoves(null);
            } else {
                List<Move> movesChosen = state.getPreparedMoves().stream()
                        .filter(move -> move.getTo().equals(coordsClicked))
                        .collect(Collectors.toList());
                if (!movesChosen.isEmpty()) {
                    state.setPreparedMoves(movesChosen);
                    if (movesChosen.size() > 1) {
                        state.setChoseChessmanTypeDialogOpened(true);
                    } else {
                        state.appendPreparedMoveToHistory();
                        generateAutoresponseIfNecessary();
                    }
                } else {
                    state.setPreparedMoves(null);
                    cellLeftClicked(coordsClicked);
                }
            }
        }
        return toView();
    }

    private void generateAutoresponseIfNecessary() {
        if (state.getAutoResponseForColor() == state.getCurrPosition().getMove().getColorOfWhoMadeMove()) {
            execChessCommand(GENERATE_NEXT_MOVE_CMD);
        }
    }

    @Override
    public ChessComponentView execChessCommand(String command) {
        state.setCommandErrorMsg(null);
        state.setCommandResponseMsg(null);
        String[] parsedCommand = command.trim().split("\\s");
        if (commands.containsKey(parsedCommand[0])) {
            commands.get(parsedCommand[0]).accept(parsedCommand);
        } else {
            if (canMakeMove()) {
                try {
                    state.setPreparedMoves(listOf(state.getCurrPosition().getMove().makeMove(command)));
                    state.appendPreparedMoveToHistory();
                    generateAutoresponseIfNecessary();
                } catch (ParseMoveException ex) {
                    state.setCommandErrorMsg(ex.getMessage());
                }
            }
        }
        return toView();
    }

    @Override
    public ChessComponentView setColorToMove(ChessmanColor colorToMove) {
        notSupported();
        return null;
    }

    @Override
    public ChessComponentView changeCastlingAvailability(ChessmanColor color, boolean isLong) {
        notSupported();
        return null;
    }

    @Override
    public ChessComponentView setPositionFromFen(String fen) {
        notSupported();
        return null;
    }

    private boolean canMakeMove() {
        return state.getCurrPosition().getChildren().isEmpty()
                && !state.isChoseChessmanTypeDialogOpened();
    }

    private void processPawnOnLastLine(CellCoords coordsClicked) {
        final Move preparedMode = state.getPreparedMoves().get(0);
        int expectedX = preparedMode.getTo().getX();
        final int xCoordClicked = coordsClicked.getX();
        if (expectedX == xCoordClicked) {
            final int yCoordClicked = coordsClicked.getY();
            int shapeCodeSelected = preparedMode.getColorOfWhoMadeMove() == WHITE
                    ? yCoordClicked : (yCoordClicked + 4);
            if (4 <= shapeCodeSelected && shapeCodeSelected <= 7) {
                final PieceShape replacementShape =
                        shapeCodeSelected == 7 ? PieceShape.QUEEN :
                        shapeCodeSelected == 6 ? PieceShape.ROOK :
                        shapeCodeSelected == 5 ? PieceShape.BISHOP :
                        shapeCodeSelected == 4 ? PieceShape.KNIGHT : null;
                if (replacementShape == null) {
                    throw new OutlineException("replacementShape == null");
                }
                state.setPreparedMoves(listOf(findPreparedMoveByPawnReplacementType(replacementShape)));
                state.appendPreparedMoveToHistory();
                state.setChoseChessmanTypeDialogOpened(false);
            }
        }
    }

    private Move findPreparedMoveByPawnReplacementType(PieceShape pieceShape) {
        return state.getPreparedMoves().stream()
                .filter(move -> move.getPieceAt(move.getTo()).getPieceShape() == pieceShape)
                .findFirst()
                .get();
    }

    public String getInitialPosition() {
        return state.getInitialPosition().getMove().toFen();
    }

    private ChessBoard getCurrentPosition() {
        return state.getCurrPosition().getMove().getResultPosition();
    }

    private void traverseHistory(Function4<GamePosition, Integer, Move, Boolean, Boolean> moveVisitor) {
        GamePosition currPosition = state.getInitialPosition();
        if (!moveVisitor.apply(
                currPosition,
                0,
                currPosition.getMove(),
                state.getCurrPosition() == currPosition
        )) {
            return;
        }
        int feMoveNumber = 1;
        currPosition = getNextOnlyPosition(currPosition);
        while (currPosition != null) {
            if (!moveVisitor.apply(
                    currPosition,
                    feMoveNumber,
                    currPosition.getMove(),
                    state.getCurrPosition() == currPosition
            )) {
                return;
            }
            if (currPosition.getMove().getColorOfWhoMadeMove() == BLACK) {
                feMoveNumber++;
            }
            currPosition = getNextOnlyPosition(currPosition);
        }
    }

    private HistoryView createHistoryView() {
        HistoryView historyView = new HistoryView();
        final MoveView[] moveView = {new MoveView()};
        traverseHistory((position, feMoveNumber, move, selected) -> {
            moveView[0].setFeMoveNumber(feMoveNumber);
            if (feMoveNumber == 0) {
                historyView.setStartPositionSelected(selected);
            } else if (move.getColorOfWhoMadeMove() == WHITE) {
                moveView[0].setWhitesMove(move.getShortNotation());
                moveView[0].setWhitesMoveSelected(selected);
            } else {
                moveView[0].setBlacksMove(move.getShortNotation());
                moveView[0].setBlacksMoveSelected(selected);

                historyView.getMoves().add(moveView[0]);
                moveView[0] = new MoveView();
            }
            return true;
        });
        if (moveView[0].getWhitesMove() != null) {
            historyView.getMoves().add(moveView[0]);
        }
        return historyView;
    }

    private GamePosition getNextOnlyPosition(GamePosition position) {
        List<GamePosition> nextMoves = position.getChildren();
        if (!nextMoves.isEmpty()) {
            if (nextMoves.size() == 1) {
                return nextMoves.get(0);
            } else {
                throw new OutlineException("Exception in getNextOnlyMove()");
            }
        } else {
            return null;
        }
    }
}
