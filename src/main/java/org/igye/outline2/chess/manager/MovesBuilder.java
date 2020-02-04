package org.igye.outline2.chess.manager;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
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
import org.igye.outline2.chess.model.PieceShape;
import org.igye.outline2.common.Function4;
import org.igye.outline2.exceptions.OutlineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.igye.outline2.chess.manager.MovesBuilderState.MAX_DEPTH;
import static org.igye.outline2.chess.manager.MovesBuilderState.MAX_MOVE_TIME;
import static org.igye.outline2.chess.model.ChessmanColor.BLACK;
import static org.igye.outline2.chess.model.ChessmanColor.WHITE;
import static org.igye.outline2.chess.model.ChessmanType.BLACK_BISHOP;
import static org.igye.outline2.chess.model.ChessmanType.BLACK_KING;
import static org.igye.outline2.chess.model.ChessmanType.BLACK_KNIGHT;
import static org.igye.outline2.chess.model.ChessmanType.BLACK_PAWN;
import static org.igye.outline2.chess.model.ChessmanType.BLACK_QUEEN;
import static org.igye.outline2.chess.model.ChessmanType.BLACK_ROOK;
import static org.igye.outline2.chess.model.ChessmanType.WHITE_BISHOP;
import static org.igye.outline2.chess.model.ChessmanType.WHITE_KING;
import static org.igye.outline2.chess.model.ChessmanType.WHITE_KNIGHT;
import static org.igye.outline2.chess.model.ChessmanType.WHITE_PAWN;
import static org.igye.outline2.chess.model.ChessmanType.WHITE_QUEEN;
import static org.igye.outline2.chess.model.ChessmanType.WHITE_ROOK;

public class MovesBuilder implements ChessComponentStateManager {
    private static final Logger LOG = LoggerFactory.getLogger(MovesBuilder.class);

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
    private static final String GRAPHIC_MODE_CMD = "gm";
    private static final String TEXT_MODE_CMD = "tm";
    private static final String SEQUENCE_MODE_CMD = "sm";
    private static final String CASE_INSENSITIVE_MODE_CMD = "ci";
    private static final String COMPARE_POSITION_CMD = "cmp";
    public static final Comparator<CellCoords> WHITE_SIDE_CELL_COMPARATOR =
            Comparator.comparingInt(c -> (c.getX() * 8 + c.getY()));
    public static final Comparator<CellCoords> BLACK_SIDE_CELL_COMPARATOR =
            Comparator.comparingInt(c -> -(c.getX() * 8 + c.getY()));

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
        try {
            if (commands.containsKey(parsedCommand[0])) {
                commands.get(parsedCommand[0]).accept(parsedCommand);
            } else {
                processSelectedMove(state.getCurrPosition().getMove().makeMove(processCaseInsensitiveMove(command)));
            }
        } catch (OutlineException ex) {
            LOG.error(ex.getMessage(), ex);
            state.setCommandErrorMsg(ex.getMessage());
        }
        return toView();
    }

    private final Pattern CASE_INSENSITIVE_MOVE_CMD_PATTERN =
            Pattern.compile("^([_kqrbnKQRBN])?([a-h]|[1-8])?([a-h][1-8])([qrbnQRBN])?$");
    private String processCaseInsensitiveMove(String moveCmd) {
        if (state.isCaseInsensitiveMode()) {
            Matcher matcher = CASE_INSENSITIVE_MOVE_CMD_PATTERN.matcher(moveCmd);
            if (matcher.matches()) {
                String firstChar = matcher.group(1);
                firstChar = firstChar == null ? "" : firstChar.toUpperCase();
                firstChar = "_".equals(firstChar) ? "b" : firstChar.toUpperCase();

                String additionalCoord = matcher.group(2);
                additionalCoord = additionalCoord == null ? "" : additionalCoord;

                String promotionChessman = matcher.group(4);
                promotionChessman = promotionChessman == null ? "" : promotionChessman.toUpperCase();

                return firstChar + additionalCoord + matcher.group(3) + promotionChessman;
            } else {
                return moveCmd;
            }
        } else {
            return moveCmd;
        }
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
        chessComponentView.setCurrPositionFen(state.getCurrPosition().getMove().toFen());
        chessComponentView.setHistory(createHistoryView(state.getPracticeState() != null));
        chessComponentView.setNoMovesRecorded(CollectionUtils.isEmpty(state.getInitialPosition().getChildren()));
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
        for (HistoryRow row : createHistoryView(false).getRows()) {
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
        if (state.getChessboardMode() == ChessboardMode.GRAPHIC) {
            renderGraphicalChessboard(chessComponentView);
        } else if (state.getChessboardMode() == ChessboardMode.TEXT) {
            renderTextChessboard(chessComponentView);
        } else {
            renderSequentialChessboard(chessComponentView);
        }
    }

    private void renderTextChessboard(ChessComponentView chessComponentView) {
        StringBuilder sb = new StringBuilder();
        if (state.getInitialPosition().getMove().getColorOfWhoToMove() == WHITE) {
            renderBlackPieces(WHITE_SIDE_CELL_COMPARATOR, sb);
            sb.append("\n\n");
            renderWhitePieces(WHITE_SIDE_CELL_COMPARATOR, sb);
        } else {
            renderWhitePieces(BLACK_SIDE_CELL_COMPARATOR, sb);
            sb.append("\n\n");
            renderBlackPieces(BLACK_SIDE_CELL_COMPARATOR, sb);
        }
        sb.append("\n");
        chessComponentView.setChessBoardText(sb.toString());
    }

    private void renderSequentialChessboard(ChessComponentView chessComponentView) {
        List<String> sequenceOfPieces = new ArrayList<>();
        if (state.getCurrPosition().getMove().getColorOfWhoToMove() == WHITE) {
            sequenceOfPieces.addAll(listWhitePieces(WHITE_SIDE_CELL_COMPARATOR));
            sequenceOfPieces.addAll(listBlackPieces(WHITE_SIDE_CELL_COMPARATOR));
        } else {
            sequenceOfPieces.addAll(listBlackPieces(BLACK_SIDE_CELL_COMPARATOR));
            sequenceOfPieces.addAll(listWhitePieces(BLACK_SIDE_CELL_COMPARATOR));
        }
        chessComponentView.setChessBoardSequence(sequenceOfPieces);
    }

    private void renderWhitePieces(Comparator<CellCoords> cellComparator, StringBuilder sb) {
        sb.append("White:\n");
        addLocationsOf(cellComparator, sb, "P", WHITE_PAWN);
        addLocationsOf(cellComparator, sb, "N", WHITE_KNIGHT);
        addLocationsOf(cellComparator, sb, "B", WHITE_BISHOP);
        addLocationsOf(cellComparator, sb, "R", WHITE_ROOK);
        addLocationsOf(cellComparator, sb, "Q", WHITE_QUEEN);
        addLocationsOf(cellComparator, sb, "K", WHITE_KING);
    }

    private void renderBlackPieces(Comparator<CellCoords> cellComparator, StringBuilder sb) {
        sb.append("Black:\n");
        addLocationsOf(cellComparator, sb, "p", BLACK_PAWN);
        addLocationsOf(cellComparator, sb, "n", BLACK_KNIGHT);
        addLocationsOf(cellComparator, sb, "b", BLACK_BISHOP);
        addLocationsOf(cellComparator, sb, "r", BLACK_ROOK);
        addLocationsOf(cellComparator, sb, "q", BLACK_QUEEN);
        addLocationsOf(cellComparator, sb, "k", BLACK_KING);
    }

    private List<String> listWhitePieces(Comparator<CellCoords> cellComparator) {
        return Stream.of(WHITE_PAWN, WHITE_KNIGHT, WHITE_BISHOP, WHITE_ROOK, WHITE_QUEEN, WHITE_KING)
                .flatMap(chessmanType -> listPieces(cellComparator, chessmanType))
                .collect(Collectors.toList());
    }

    private List<String> listBlackPieces(Comparator<CellCoords> cellComparator) {
        return Stream.of(BLACK_PAWN, BLACK_KNIGHT, BLACK_BISHOP, BLACK_ROOK, BLACK_QUEEN, BLACK_KING)
                .flatMap(chessmanType -> listPieces(cellComparator, chessmanType))
                .collect(Collectors.toList());
    }

    private Stream<String> listPieces(Comparator<CellCoords> cellComparator, ChessmanType chessmanType) {
        String pieceName = chessmanType.getPieceColor() == WHITE
                ? chessmanType.getSymbol().toUpperCase()
                : chessmanType.getSymbol().toLowerCase();
        return findLocationsOf(cellComparator, chessmanType)
            .map(cellCoords -> pieceName + " " + ChessUtils.coordsToString(cellCoords));
    }

    private void addLocationsOf(Comparator<CellCoords> cellComparator,
                                StringBuilder sb, String prefix, ChessmanType chessmanType) {
        sb.append("\n").append(prefix).append(":");
        findLocationsOf(cellComparator, chessmanType)
                .forEach(cell -> sb.append(" ").append(ChessUtils.coordsToString(cell)));
    }

    private Stream<CellCoords> findLocationsOf(Comparator<CellCoords> cellComparator, ChessmanType chessmanType) {
        return getCurrentPosition().findAll(ct -> ct == chessmanType).stream()
                .sorted(cellComparator);
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
        chessBoardView.setBoardRotated(state.getInitialPosition().getMove().getColorOfWhoToMove() == BLACK);
    }

    private void putChessmanTypeToChoose(ChessBoardView chessBoard, int x, int y, ChessmanType chessmanType) {
        ChessBoardCellView cell = chessBoard.getCell(x, y);
        cell.setBorderColor(CHOOSE_CHESSMAN_TYPE_COLOR);
        cell.setCode(chessmanType.getCode());
    }

    private void initCommandMap() {
        commands = new HashMap<>();
        addCommand(HIDE_SHOW_CHESSBOARD_CMD, args -> hideShowChessboard());
        addCommand(PREV_POSITION_CMD, args -> goToPrevPosition());
        addCommand(NEXT_POSITION_CMD, args -> goToNextPosition());
        addCommand(GENERATE_NEXT_MOVE_CMD, args -> generateNextMove());
        addCommand(AUTO_RESPONSE_CMD, args -> setAutoresponse());
        addCommand(DELETE_ALL_TO_THE_RIGHT_CMD, args -> deleteAllToTheRight());
        addCommand(GO_TO_START_POSITION_CMD, args -> goToStartPosition());
        addCommand(GO_TO_END_POSITION_CMD, args -> goToEndPosition());
        addCommand(GO_TO_POSITION_CMD, args -> {
            final String destination = args[1];
            goToPosition(
                    Integer.parseInt(destination.substring(0, destination.length()-1)),
                    destination.charAt(destination.length()-1) == 'w' ? WHITE : BLACK
            );
        });
        addCommand(SET_DEPTH_CMD, args -> {
            final int depth = Integer.parseInt(args[1]);
            state.setDepth((1 <= depth && depth <= MAX_DEPTH) ? depth : MAX_DEPTH);
        });
        addCommand(SET_MOVE_TIME_CMD, args -> {
            final int moveTime = Integer.parseInt(args[1]);
            state.setMovetimeSec((1 <= moveTime && moveTime <= MAX_MOVE_TIME) ? moveTime : MAX_MOVE_TIME);
        });
        addCommand(GRAPHIC_MODE_CMD, args -> state.setChessboardMode(ChessboardMode.GRAPHIC));
        addCommand(TEXT_MODE_CMD, args -> state.setChessboardMode(ChessboardMode.TEXT));
        addCommand(SEQUENCE_MODE_CMD, args -> state.setChessboardMode(ChessboardMode.SEQUENCE));
        addCommand(CASE_INSENSITIVE_MODE_CMD, args -> {
            state.setCaseInsensitiveMode(!state.isCaseInsensitiveMode());
        });
        addCommand(COMPARE_POSITION_CMD, this::comparePosition);
    }

    private void addCommand(String commandName, Consumer<String[]> commandHandler) {
        if (commands.containsKey(commandName)) {
            throw new OutlineException("Command " + commandName + " is already registered.");
        }
        commands.put(commandName, commandHandler);
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

    private HistoryView createHistoryView(boolean isPracticeMode) {
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
            return !isPracticeMode || !selected;
        });
        if (historyRow[0].getWhitesMove() != null) {
            historyView.getRows().add(historyRow[0]);
        } else if (historyView.getRows().isEmpty()
                && state.getInitialPosition().getMove().getColorOfWhoToMove() == BLACK) {
            historyRow[0] = new HistoryRow();
            historyRow[0].setFeMoveNumber(1);
            historyRow[0].setWhitesMove("...");
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

    private void comparePosition(String[] allArgs) {
        List<Pair<CellCoords,ChessmanType>> userPosition = parseUserPosition(allArgs);
        int matchedCnt = 0;
        final int[] missedCnt = {0};
        final int[] actualNumberOfPieces = {0};
        final Move currMove = state.getCurrPosition().getMove();
        for (Pair<CellCoords, ChessmanType> userCell : userPosition) {
            if (currMove.getPieceAt(userCell.getKey()) == userCell.getRight()) {
                matchedCnt++;
            }
        }
        currMove.getResultPosition().traverse((x,y,ct) -> {
            actualNumberOfPieces[0]++;
            List<Pair<CellCoords, ChessmanType>> matchedUSerCells = userPosition.stream()
                    .filter(userCell -> userCell.getLeft().getX() == x && userCell.getLeft().getY() == y)
                    .collect(Collectors.toList());
            if (matchedUSerCells.size() == 0) {
                missedCnt[0]++;
            } else if (matchedUSerCells.size() > 1) {
                throw new OutlineException(
                        "matchedUSerCells.size() > 1 for " + ChessUtils.coordsToString(new CellCoords(x,y)));
            }
            return true;
        });

        StringBuilder sb = new StringBuilder();
        sb.append("Matched ").append(matchedCnt).append(" of ").append(userPosition.size());
        sb.append(", missed ").append(missedCnt[0]).append(" of ").append(actualNumberOfPieces[0]);
        state.setCommandResponseMsg(sb.toString());
    }

    private List<Pair<CellCoords,ChessmanType>> parseUserPosition(String[] allArgs) {
        List<Pair<CellCoords,ChessmanType>> result = new ArrayList<>();
        for (int i = 1; i < allArgs.length; i++) {
            result.addAll(parsePositionPart(allArgs[i]));
        }
        return result;
    }

    private List<Pair<CellCoords,ChessmanType>> parsePositionPart(String positionPart) {
        final char chessmanTypeChar = positionPart.charAt(0);
        ChessmanType chessmanType = PositionBuilder.chessmenTypesMap.get(chessmanTypeChar);
        if (chessmanType == null) {
            throw new OutlineException(
                    "Unknown chessmanTypeChar = " + chessmanTypeChar + " in " + positionPart);
        }
        String positionsList = positionPart.substring(1);
        if (positionsList.length() == 0 || positionsList.length() % 2 != 0) {
            throw new OutlineException("incorrect positions list in " + positionPart);
        }
        int numOfCells = positionsList.length() / 2;
        List<Pair<CellCoords,ChessmanType>> result = new ArrayList<>();
        for (int i = 0; i < numOfCells; i++) {
            result.add(Pair.of(
                    new CellCoords(
                            ChessUtils.strCoordToInt(positionsList.charAt(i*2)),
                            ChessUtils.strCoordToInt(positionsList.charAt(i*2+1))
                    ),
                    chessmanType
            ));
        }
        return result;
    }
}
