package org.igye.outline2.chess.manager;

import org.igye.outline2.chess.dto.ChessBoardCellView;
import org.igye.outline2.chess.dto.ChessComponentView;
import org.igye.outline2.chess.dto.ChessViewConverter;
import org.igye.outline2.chess.dto.ChoseChessmanTypeDialogView;
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
import org.igye.outline2.common.Randoms;
import org.igye.outline2.exceptions.OutlineException;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.igye.outline2.OutlineUtils.listOf;

public class MovesBuilder implements ChessComponentStateManager {
    private static final String PREPARED_TO_MOVE_COLOR = "#FFFF00";
    private static final String AVAILABLE_TO_MOVE_TO_COLOR = "#90EE90";
    private static final String PREV_POSITION_CMD = "p";
    private static final String NEXT_POSITION_CMD = "n";
    private static final String GO_TO_POSITION_CMD = "g";
    private static final String GO_TO_END_POSITION_CMD = "e";
    private static final String GO_TO_START_POSITION_CMD = "s";
    private static final String DELETE_ALL_TO_THE_RIGHT_CMD = "rr";
    private static final String GENERATE_RANDOM_MOVE = "nn";

    private MovesBuilderState state;
    private Map<String, Consumer<String[]>> commands;

    public MovesBuilder(Move initialPosition) {
        state = new MovesBuilderState(initialPosition);
        initCommandMap();
    }

    @Override
    public ChessComponentView toView() {
        ChessComponentView chessComponentView = new ChessComponentView();
        chessComponentView.setChessBoard(ChessViewConverter.toDto(getCurrentPosition()));
        chessComponentView.setTab(ChessComponentStage.MOVES);
        final Move currMove = state.getCurrPosition().getMove();
        if (!CollectionUtils.isEmpty(state.getPreparedMoves())) {
            chessComponentView.getChessBoard().setBorderColorForCell(
                    state.getPreparedMoves().get(0).getFrom(), PREPARED_TO_MOVE_COLOR
            );
            state.getPreparedMoves().forEach(move ->
                    chessComponentView.getChessBoard().setBorderColorForCell(move.getTo(), AVAILABLE_TO_MOVE_TO_COLOR)
            );
        } else {
            if (currMove.getFrom() != null) {
                chessComponentView.getChessBoard().setBorderColorForCell(currMove.getFrom(), PREPARED_TO_MOVE_COLOR);
            }
            if (currMove.getTo() != null) {
                chessComponentView.getChessBoard().setBorderColorForCell(currMove.getTo(), AVAILABLE_TO_MOVE_TO_COLOR);
            }
        }
        chessComponentView.setHistory(createHistoryView());
        createChoseChessmanTypeDialogViewIfNecessary(chessComponentView);
        if (state.getCommandErrorMsg() != null) {
            chessComponentView.setCommandErrorMsg(state.getCommandErrorMsg());
        }
        return chessComponentView;
    }

    private void initCommandMap() {
        commands = new HashMap<>();
        commands.put(PREV_POSITION_CMD, args -> goToPrevPosition());
        commands.put(NEXT_POSITION_CMD, args -> goToNextPosition());
        commands.put(GENERATE_RANDOM_MOVE, args -> generateRandomMove());
        commands.put(DELETE_ALL_TO_THE_RIGHT_CMD, args -> deleteAllToTheRight());
        commands.put(GO_TO_START_POSITION_CMD, args -> goToStartPosition());
        commands.put(GO_TO_END_POSITION_CMD, args -> goToEndPosition());
        commands.put(GO_TO_POSITION_CMD, args -> {
            final String destination = args[1];
            goToPosition(
                    Integer.parseInt(destination.substring(0, destination.length()-1)),
                    destination.charAt(destination.length()-1) == 'w'
                            ? ChessmanColor.WHITE
                            : ChessmanColor.BLACK
            );
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

    private void generateRandomMove() {
        if (canMakeMove()) {
            final Move currMove = state.getCurrPosition().getMove();
            ChessmanColor colorOfOpponent = currMove.getColorOfWhoMadeMove().invert();
            List<Move> availableMoves = currMove.getResultPosition()
                    .findAll(p -> p.getPieceColor() == colorOfOpponent)
                    .stream()
                    .flatMap(opponentCoords -> currMove.getPossibleNextMoves(opponentCoords).stream())
                    .collect(Collectors.toList());
            if (!availableMoves.isEmpty()) {
                state.setPreparedMoves(listOf(Randoms.element(availableMoves)));
                state.appendPreparedMoveToHistory();
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

    private void createChoseChessmanTypeDialogViewIfNecessary(ChessComponentView chessComponentView) {
        if (state.isChoseChessmanTypeDialogOpened()) {
            chessComponentView.setChoseChessmanTypeDialogView(
                    createChoseChessmanTypeDialogView(state.getPreparedMoves().get(0).getColorOfWhoMadeMove())
            );
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
                    }
                } else {
                    state.setPreparedMoves(null);
                    cellLeftClicked(coordsClicked);
                }
            }
        }
        return toView();
    }

    @Override
    public ChessComponentView execChessCommand(String command) {
        state.setCommandErrorMsg(null);
        String[] parsedCommand = command.trim().split("\\s");
        if (commands.containsKey(parsedCommand[0])) {
            commands.get(parsedCommand[0]).accept(parsedCommand);
        } else {
            if (canMakeMove()) {
                try {
                    state.setPreparedMoves(listOf(state.getCurrPosition().getMove().makeMove(command)));
                    state.appendPreparedMoveToHistory();
                } catch (ParseMoveException ex) {
                    state.setCommandErrorMsg(ex.getMessage());
                }
            }
        }
        return toView();
    }

    private boolean canMakeMove() {
        return state.getCurrPosition().getChildren().isEmpty()
                && !state.isChoseChessmanTypeDialogOpened();
    }

    private void processPawnOnLastLine(CellCoords coordsClicked) {
        final int xCoord = coordsClicked.getX();
        if (20 <= xCoord && xCoord <= 23) {
            final PieceShape replacementShape =
                    xCoord == 20 ? PieceShape.KNIGHT :
                    xCoord == 21 ? PieceShape.BISHOP :
                    xCoord == 22 ? PieceShape.ROOK :
                    xCoord == 23 ? PieceShape.QUEEN : null;
            if (replacementShape == null) {
                throw new OutlineException("replacementShape == null");
            }
            state.setPreparedMoves(listOf(findPreparedMoveByPawnReplacementType(replacementShape)));
            state.appendPreparedMoveToHistory();
            state.setChoseChessmanTypeDialogOpened(false);
        }
    }

    private Move findPreparedMoveByPawnReplacementType(PieceShape pieceShape) {
        return state.getPreparedMoves().stream()
                .filter(move -> move.getPieceAt(move.getTo()).getPieceShape() == pieceShape)
                .findFirst()
                .get();
    }

    private ChoseChessmanTypeDialogView createChoseChessmanTypeDialogView(ChessmanColor color) {
        ChoseChessmanTypeDialogView result = new ChoseChessmanTypeDialogView();
        result.getCellsToChoseFrom().add(
                ChessBoardCellView.builder()
                        .code(color.equals(ChessmanColor.WHITE)
                                ? ChessmanType.WHITE_KNIGHT.getCode()
                                : ChessmanType.BLACK_KNIGHT.getCode()
                        )
                        .coords(new CellCoords(20,0))
                        .build()
        );
        result.getCellsToChoseFrom().add(
                ChessBoardCellView.builder()
                        .code(color.equals(ChessmanColor.WHITE)
                                ? ChessmanType.WHITE_BISHOP.getCode()
                                : ChessmanType.BLACK_BISHOP.getCode()
                        )
                        .coords(new CellCoords(21,0))
                        .build()
        );
        result.getCellsToChoseFrom().add(
                ChessBoardCellView.builder()
                        .code(color.equals(ChessmanColor.WHITE)
                                ? ChessmanType.WHITE_ROOK.getCode()
                                : ChessmanType.BLACK_ROOK.getCode()
                        )
                        .coords(new CellCoords(22,0))
                        .build()
        );
        result.getCellsToChoseFrom().add(
                ChessBoardCellView.builder()
                        .code(color.equals(ChessmanColor.WHITE)
                                ? ChessmanType.WHITE_QUEEN.getCode()
                                : ChessmanType.BLACK_QUEEN.getCode()
                        )
                        .coords(new CellCoords(23,0))
                        .build()
        );
        return result;
    }

    public String getInitialPosition() {
        return state.getInitialPosition().getMove().getResultPosition().encode();
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
            if (currPosition.getMove().getColorOfWhoMadeMove() == ChessmanColor.BLACK) {
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
            } else if (move.getColorOfWhoMadeMove() == ChessmanColor.WHITE) {
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
