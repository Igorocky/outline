package org.igye.outline2.chess.manager;

import org.apache.commons.lang3.StringUtils;
import org.igye.outline2.chess.dto.ChessBoardCellView;
import org.igye.outline2.chess.dto.ChessBoardView;
import org.igye.outline2.chess.dto.ChessComponentResponse;
import org.igye.outline2.chess.dto.ChessComponentView;
import org.igye.outline2.chess.dto.ChessViewConverter;
import org.igye.outline2.chess.dto.HistoryRow;
import org.igye.outline2.chess.dto.HistoryView;
import org.igye.outline2.chess.dto.ParsedPgnDto;
import org.igye.outline2.chess.dto.PositionDto;
import org.igye.outline2.chess.dto.PracticeStateView;
import org.igye.outline2.chess.manager.analyse.PgnParser;
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
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.igye.outline2.chess.manager.MovesBuilderState.MAX_DEPTH;
import static org.igye.outline2.chess.manager.MovesBuilderState.MAX_MOVE_TIME;
import static org.igye.outline2.chess.model.ChessmanColor.BLACK;
import static org.igye.outline2.chess.model.ChessmanColor.WHITE;
import static org.igye.outline2.chess.model.ChessmanType.*;

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
    private static final String TEXT_MODE_CMD = "tm";

    private final String runStockfishCmd;
    private MovesBuilderState state;
    private Map<String, Consumer<String[]>> commands;

    public MovesBuilder(String runStockfishCmd, Move initialPosition) {
        this.runStockfishCmd = runStockfishCmd;
        state = new MovesBuilderState(initialPosition);
        initCommandMap();
    }

    @Override
    public ChessComponentResponse execChessCommand(String command) {
        state.setCommandErrorMsg(null);
        state.setCommandResponseMsg(null);
        String[] parsedCommand = command.trim().split("\\s");
        if (commands.containsKey(parsedCommand[0])) {
            commands.get(parsedCommand[0]).accept(parsedCommand);
        } else {
            try {
                processSelectedMove(state.getCurrPosition().getMove().makeMove(command));
            } catch (ParseMoveException ex) {
                state.setCommandErrorMsg(ex.getMessage());
            }
        }
        return toView();
    }

    @Override
    public ChessComponentResponse cellLeftClicked(CellCoords coordsClicked) {
        if (state.isChoseChessmanTypeDialogOpened()) {
            Move selectedMove = processPawnOnLastLine(coordsClicked);
            if (selectedMove != null) {
                processSelectedMove(selectedMove);
            }
        } else {
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
                    if (movesChosen.size() > 1) {
                        state.setPreparedMoves(movesChosen);
                        state.setChoseChessmanTypeDialogOpened(true);
                    } else {
                        processSelectedMove(movesChosen.get(0));
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
    public ChessComponentResponse setColorToMove(ChessmanColor colorToMove) {
        notSupported();
        return null;
    }

    @Override
    public ChessComponentResponse changeCastlingAvailability(ChessmanColor color, boolean isLong) {
        notSupported();
        return null;
    }

    @Override
    public ChessComponentResponse setPositionFromFen(String fen) {
        notSupported();
        return null;
    }

    @Override
    public ChessComponentResponse showCorrectMove() {
        List<GamePosition> expectedNextMoves = state.getCurrPosition().getChildren();
        if (!expectedNextMoves.isEmpty()) {
            state.setCurrPosition(expectedNextMoves.get(0));
            state.getPracticeState().setFailed(true);
            state.getPracticeState().setLastMoveWasIncorrect(false);
            state.setPreparedMoves(null);
            state.setChoseChessmanTypeDialogOpened(false);
        }
        return toView();
    }

    @Override
    public ChessComponentResponse setAutoResponseForOpponent() {
        state.setAutoResponseForColor(state.getCurrPosition().getMove().getColorOfWhoMadeMove());
        return toView();
    }

    @Override
    public ChessComponentResponse toView() {
        ChessComponentView chessComponentView = new ChessComponentView();
        chessComponentView.setTab(state.getPracticeState() == null
                ? ChessComponentStage.MOVES : ChessComponentStage.PRACTICE_SEQUENCE);
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
        if (state.getPracticeState() != null) {
            setPracticeState(chessComponentView);
        }
        return ChessComponentResponse.builder().chessComponentView(chessComponentView).build();
    }

    public String getInitialPosition() {
        return state.getInitialPosition().getMove().toFen();
    }

    public String toPgn() {
        StringBuilder sb = new StringBuilder();
        sb.append("[FEN \"").append(state.getInitialPosition().getMove().toFen()).append("\"]\n");
        sb.append("[White \"white\"]\n");
        sb.append("[Black \"black\"]\n");
        sb.append("\n\n");
        for (HistoryRow row : createHistoryView().getRows()) {
            sb.append(row.getFeMoveNumber()).append(". ")
                    .append(row.getWhitesMove()).append(" ");
            if (row.getBlacksMove() != null) {
                sb.append(row.getBlacksMove()).append(" ");
            }
        }
        return sb.toString();
    }

    public void loadFromPgn(String pgn) {
        state.setChoseChessmanTypeDialogOpened(false);
        state.setPracticeState(null);
        ParsedPgnDto parsedPgn = PgnParser.parsePgn(pgn);
        state.setInitialPosition(new GamePosition(new Move(parsedPgn.getInitialPositionFen())));
        state.setCurrPosition(state.getInitialPosition());
        for (List<PositionDto> fullMove : parsedPgn.getPositions()) {
            for (PositionDto halfMove : fullMove) {
                if (!"...".equals(halfMove.getNotation())) {
                    execChessCommand(halfMove.getNotation());
                    if (!StringUtils.isBlank(state.getCommandErrorMsg())) {
                        throw new OutlineException(state.getCommandErrorMsg());
                    }
                }
            }
        }
    }

    public void setPracticeMode(boolean isPracticeMode) {
        if (isPracticeMode) {
            state.setPracticeState(new MovesBuilderPracticeState());
        } else {
            state.setPracticeState(null);
        }
        state.setCurrPosition(state.getInitialPosition());
    }

    private void processSelectedMove(Move selectedMove) {
        if (state.getPracticeState() != null) {
            List<GamePosition> expectedNextMoves = state.getCurrPosition().getChildren();
            if (expectedNextMoves.size() == 1) {
                final GamePosition expectedNextGamePosition = expectedNextMoves.get(0);
                ChessBoard expectedNextResultPosition = expectedNextGamePosition.getMove().getResultPosition();
                if (selectedMove.getResultPosition().equalsTo(expectedNextResultPosition)) {
                    state.getPracticeState().setLastMoveWasIncorrect(false);
                    state.setCurrPosition(expectedNextGamePosition);
                    state.setPreparedMoves(null);
                    state.setChoseChessmanTypeDialogOpened(false);
                } else {
                    state.getPracticeState().setFailed(true);
                    state.getPracticeState().setLastMoveWasIncorrect(true);
                    state.setPreparedMoves(null);
                    state.setChoseChessmanTypeDialogOpened(false);
                }
            } else if (expectedNextMoves.size() > 1) {
                throw new OutlineException("expectedNextMoves.size() > 1");
            }
        } else {
            if (state.getCurrPosition().getChildren().isEmpty()) {
                state.appendSelectedMoveToHistory(selectedMove);
            } else {
                state.setPreparedMoves(null);
                state.setChoseChessmanTypeDialogOpened(false);
            }
        }
        generateAutoresponseIfNecessary();
    }

    private void setPracticeState(ChessComponentView chessComponentView) {
        chessComponentView.setPractiseState(new PracticeStateView());
        final PracticeStateView practiceStateView = chessComponentView.getPractiseState();
        practiceStateView.setWaitingForNextMove(
                !state.getCurrPosition().getChildren().isEmpty()
        );
        practiceStateView.setIncorrectMove(
                state.getPracticeState().isLastMoveWasIncorrect()
        );
        practiceStateView.setFailed(
                state.getPracticeState().isFailed()
        );
        practiceStateView.setColorToMove(state.getCurrPosition().getMove().getColorOfWhoToMove().toString());
    }

    private void renderChessboard(ChessComponentView chessComponentView) {
        if (state.isTextMode()) {
            renderTextChessboard(chessComponentView);
        } else {
            renderGraphicalChessboard(chessComponentView);
        }
    }

    private void renderTextChessboard(ChessComponentView chessComponentView) {
        StringBuilder sb = new StringBuilder();
        sb.append("White:\n");
        addLocationsOf(sb, "P", WHITE_PAWN);
        addLocationsOf(sb, "N", WHITE_KNIGHT);
        addLocationsOf(sb, "B", WHITE_BISHOP);
        addLocationsOf(sb, "R", WHITE_ROOK);
        addLocationsOf(sb, "Q", WHITE_QUEEN);
        addLocationsOf(sb, "K", WHITE_KING);
        sb.append("\n\nBlack:\n");
        addLocationsOf(sb, "P", BLACK_PAWN);
        addLocationsOf(sb, "N", BLACK_KNIGHT);
        addLocationsOf(sb, "B", BLACK_BISHOP);
        addLocationsOf(sb, "R", BLACK_ROOK);
        addLocationsOf(sb, "Q", BLACK_QUEEN);
        addLocationsOf(sb, "K", BLACK_KING);
        chessComponentView.setChessBoardText(sb.toString());
    }

    private void addLocationsOf(StringBuilder sb, String prefix, ChessmanType chessmanType) {
        sb.append("\n").append(prefix).append(":");
        getCurrentPosition().findAll(ct -> ct == chessmanType).stream()
                .sorted(Comparator.comparingInt(c -> (c.getY() * 8 + c.getX())))
                .forEach(cell -> sb.append(" ").append(ChessUtils.coordsToString(cell)));
    }

    private void renderGraphicalChessboard(ChessComponentView chessComponentView) {
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
                final int base = side == WHITE ? 7 : 0;
                final int delta = side == WHITE ? -1 : 1;
                putChessmanTypeToChoose(chessBoardView, x, base, side == WHITE ? WHITE_QUEEN : BLACK_QUEEN);
                putChessmanTypeToChoose(chessBoardView, x, base+delta, side == WHITE ? WHITE_ROOK : BLACK_ROOK);
                putChessmanTypeToChoose(chessBoardView, x, base+delta*2, side == WHITE ? WHITE_BISHOP : BLACK_BISHOP);
                putChessmanTypeToChoose(chessBoardView, x, base+delta*3, side == WHITE ? WHITE_KNIGHT : BLACK_KNIGHT);
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
        commands.put(TEXT_MODE_CMD, args -> {
            state.setTextMode(!state.isTextMode());
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
        state.setPreparedMoves(null);
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
                state.appendSelectedMoveToHistory(nextMove);
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
        state.setPreparedMoves(null);
    }

    private void goToStartPosition() {
        state.setCurrPosition(state.getInitialPosition());
        state.setPreparedMoves(null);
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

    private void generateAutoresponseIfNecessary() {
        if (state.getAutoResponseForColor() == state.getCurrPosition().getMove().getColorOfWhoMadeMove().invert()) {
            if (state.getPracticeState() != null && !state.getCurrPosition().getChildren().isEmpty()) {
                processSelectedMove(state.getCurrPosition().getChildren().get(0).getMove());
            } else {
                execChessCommand(GENERATE_NEXT_MOVE_CMD);
            }
        }
    }

    private boolean canMakeMove() {
        return state.getCurrPosition().getChildren().isEmpty()
                && !state.isChoseChessmanTypeDialogOpened()
                && state.getPracticeState() == null
                ;
    }

    private Move processPawnOnLastLine(CellCoords coordsClicked) {
        final Move preparedMode = state.getPreparedMoves().get(0);
        int expectedX = preparedMode.getTo().getX();
        final int xCoordClicked = coordsClicked.getX();
        if (expectedX == xCoordClicked) {
            final int yCoordClicked = coordsClicked.getY();
            final ChessmanColor whoMadeMove = preparedMode.getColorOfWhoMadeMove();
            int shapeCodeSelected = whoMadeMove == WHITE
                    ? yCoordClicked : (yCoordClicked + 4);
            if (4 <= shapeCodeSelected && shapeCodeSelected <= 7) {
                final PieceShape replacementShape =
                        shapeCodeSelected == 7 ? (whoMadeMove == WHITE ? PieceShape.QUEEN : PieceShape.KNIGHT) :
                        shapeCodeSelected == 6 ? (whoMadeMove == WHITE ? PieceShape.ROOK : PieceShape.BISHOP) :
                        shapeCodeSelected == 5 ? (whoMadeMove == WHITE ? PieceShape.BISHOP : PieceShape.ROOK) :
                        shapeCodeSelected == 4 ? (whoMadeMove == WHITE ? PieceShape.KNIGHT : PieceShape.QUEEN) : null;
                if (replacementShape == null) {
                    throw new OutlineException("replacementShape == null");
                }
                return findPreparedMoveByPawnReplacementType(replacementShape);
            }
        }
        return null;
    }

    private Move findPreparedMoveByPawnReplacementType(PieceShape pieceShape) {
        return state.getPreparedMoves().stream()
                .filter(move -> move.getPieceAt(move.getTo()).getPieceShape() == pieceShape)
                .findFirst()
                .get();
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
        final HistoryRow[] historyRow = {new HistoryRow()};
        traverseHistory((position, feMoveNumber, move, selected) -> {
            historyRow[0].setFeMoveNumber(feMoveNumber);
            if (feMoveNumber == 0) {
                historyView.setStartPositionSelected(selected);
            } else if (move.getColorOfWhoMadeMove() == WHITE) {
                historyRow[0].setWhitesMove(move.getShortNotation());
                historyRow[0].setWhitesMoveSelected(selected);
            } else {
                if (historyRow[0].getWhitesMove() == null) {
                    historyRow[0].setWhitesMove("...");
                }
                historyRow[0].setBlacksMove(move.getShortNotation());
                historyRow[0].setBlacksMoveSelected(selected);

                historyView.getRows().add(historyRow[0]);
                historyRow[0] = new HistoryRow();
            }
            return true;
        });
        if (historyRow[0].getWhitesMove() != null) {
            historyView.getRows().add(historyRow[0]);
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
