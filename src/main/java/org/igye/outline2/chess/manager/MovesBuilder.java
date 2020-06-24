package org.igye.outline2.chess.manager;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.igye.outline2.chess.dto.ChessBoardCellView;
import org.igye.outline2.chess.dto.ChessBoardView;
import org.igye.outline2.chess.dto.ChessComponentResponse;
import org.igye.outline2.chess.dto.ChessComponentView;
import org.igye.outline2.chess.dto.ChessViewConverter;
import org.igye.outline2.chess.dto.ChessboardSequentialView;
import org.igye.outline2.chess.dto.ChessmenPositionQuizCard;
import org.igye.outline2.chess.dto.HistoryRow;
import org.igye.outline2.chess.dto.HistoryView;
import org.igye.outline2.chess.dto.ParsedPgnDto;
import org.igye.outline2.chess.dto.PositionDto;
import org.igye.outline2.chess.dto.PracticeStateView;
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
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.igye.outline2.chess.manager.ChessUtils.BLACK_SIDE_CELL_COMPARATOR;
import static org.igye.outline2.chess.manager.ChessUtils.WHITE_SIDE_CELL_COMPARATOR;
import static org.igye.outline2.chess.manager.MovesBuilderState.MAX_DEPTH;
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
import static org.igye.outline2.chess.model.PieceShape.BISHOP;
import static org.igye.outline2.chess.model.PieceShape.KING;
import static org.igye.outline2.chess.model.PieceShape.KNIGHT;
import static org.igye.outline2.chess.model.PieceShape.PAWN;
import static org.igye.outline2.chess.model.PieceShape.QUEEN;
import static org.igye.outline2.chess.model.PieceShape.ROOK;
import static org.igye.outline2.common.OutlineUtils.listOf;

public class MovesBuilder implements ChessComponentStateManager {
    private static final Logger LOG = LoggerFactory.getLogger(MovesBuilder.class);

    private static final String PREPARED_TO_MOVE_COLOR = "#FFFF00";
    private static final String AVAILABLE_TO_MOVE_TO_COLOR = "#90EE90";
    private static final String CHOOSE_CHESSMAN_TYPE_COLOR = "#0000AA";

    public static final String SET_DEPTH_CMD = "d";
    private static final String PREV_POSITION_CMD = "p";
    private static final String NEXT_POSITION_CMD = "n";
    private static final String GO_TO_POSITION_CMD = "g";
    private static final String GO_TO_END_POSITION_CMD = "e";
    private static final String GO_TO_START_POSITION_CMD = "s";
    private static final String DELETE_ALL_TO_THE_RIGHT_CMD = "rr";
    private static final String GENERATE_NEXT_MOVE_CMD = "nn";
    private static final String AUTO_RESPONSE_CMD = "aa";
    private static final String HIDE_SHOW_CHESSBOARD_CMD = "b";
    private static final String GRAPHIC_MODE_CMD = "gm";
    private static final String TEXT_MODE_CMD = "tm";
    private static final String SEQUENCE_MODE_CMD = "sm";
    public static final String AUDIO_MODE_CMD = "am";
    public static final String CASE_INSENSITIVE_MODE_CMD = "ci";
    private static final String COMPARE_POSITION_CMD = "cmp";

    private static final String COMPUTER_IS_THINKING = "Computer is thinking...";

    private final String runStockfishCmd;
    private MovesBuilderState state;
    private Map<String, BiConsumer<String[],Consumer<String>>> commands;
    private static final List<PieceShape> SHAPES_TO_LIST_P1 = listOf(KING, QUEEN, ROOK);
    private static final List<PieceShape> SHAPES_TO_LIST_P2 = listOf(BISHOP, KNIGHT);

    public MovesBuilder(String runStockfishCmd, Move initialPosition) {
        this.runStockfishCmd = runStockfishCmd;
        state = new MovesBuilderState(initialPosition);
        initCommandMap();
    }

    @Override
    public ChessComponentResponse execChessCommand(String command, Consumer<String> progressCallback) {
        state.setCommandErrorMsg(null);
        state.setCommandResponseMsg(null);
        String[] parsedCommand = command.trim().split("\\s");
        try {
            if (commands.containsKey(parsedCommand[0])) {
                commands.get(parsedCommand[0]).accept(parsedCommand, progressCallback);
            } else {
                processSelectedMove(
                        state.getCurrPosition().getMove().makeMove(processCaseInsensitiveMove(command)),
                        progressCallback
                );
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
                processSelectedMove(selectedMove, null);
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
                        processSelectedMove(movesChosen.get(0), null);
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
            state.getPracticeState().setLastMoveWasIncorrect(null);
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
    public ChessComponentResponse hideCommandResponseMsg() {
        state.setCommandResponseMsg(null);
        state.setCommandErrorMsg(null);
        return toView();
    }

    @Override
    public synchronized ChessComponentResponse toView() {
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
        chessComponentView.setDepth(state.getDepth());
        return ChessComponentResponse.builder().chessComponentView(chessComponentView).build();
    }

    public String getInitialPositionFen() {
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

    public void loadFromPgn(ParsedPgnDto parsedPgn) {
        state.setChoseChessmanTypeDialogOpened(false);
        state.setPracticeState(null);
        state.setInitialPosition(new GamePosition(new Move(parsedPgn.getInitialPositionFen())));
        state.setCurrPosition(state.getInitialPosition());
        for (List<PositionDto> fullMove : parsedPgn.getPositions()) {
            for (PositionDto halfMove : fullMove) {
                if (!"...".equals(halfMove.getNotation())) {
                    execChessCommand(halfMove.getNotation(), null);
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

    private void processSelectedMove(Move selectedMove, Consumer<String> progressCallback) {
        if (state.getPracticeState() != null) {
            List<GamePosition> expectedNextMoves = state.getCurrPosition().getChildren();
            if (expectedNextMoves.size() == 1) {
                final GamePosition expectedNextGamePosition = expectedNextMoves.get(0);
                ChessBoard expectedNextResultPosition = expectedNextGamePosition.getMove().getResultPosition();
                if (selectedMove.getResultPosition().equalsTo(expectedNextResultPosition)) {
                    state.getPracticeState().setLastMoveWasIncorrect(null);
                    state.setCurrPosition(expectedNextGamePosition);
                    state.setPreparedMoves(null);
                    state.setChoseChessmanTypeDialogOpened(false);
                } else {
                    state.getPracticeState().setFailed(true);
                    state.getPracticeState().setLastMoveWasIncorrect(selectedMove.getShortNotation());
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
        generateAutoresponseIfNecessary(progressCallback);
    }

    private void setPracticeState(ChessComponentView chessComponentView) {
        chessComponentView.setPractiseState(new PracticeStateView());
        final PracticeStateView practiceStateView = chessComponentView.getPractiseState();
        practiceStateView.setWaitingForNextMove(
                !state.getCurrPosition().getChildren().isEmpty()
        );
        practiceStateView.setIncorrectMove(
                state.getPracticeState().getLastMoveWasIncorrect()
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
            chessComponentView.setChessBoardText(ChessUtils.renderTextChessboard(
                    getCurrentPosition().getMove().getResultPosition(),
                    state.getInitialPosition().getMove().getColorOfWhoToMove()
            ));
        } else if (state.getChessboardMode() == ChessboardMode.SEQUENCE) {
            renderSequentialChessboard(chessComponentView, false);
        } else if (state.getChessboardMode() == ChessboardMode.AUDIO) {
            renderSequentialChessboard(chessComponentView, true);
        }
    }

    private void renderSequentialChessboard(ChessComponentView chessComponentView, boolean audioMode) {
        chessComponentView.setChessBoardSequence(ChessboardSequentialView.builder()
                .numberOfPieces(getStartPosition().getMove().getResultPosition().findAll(ct -> true).size())
                .quiz(createQuizCards(audioMode))
                .build()
        );
    }

    private List<ChessmenPositionQuizCard> createQuizCards(boolean audioMode) {
        List<ChessmenPositionQuizCard> result = new ArrayList<>();
        final ChessmanColor firstColor = getStartPosition().getMove().getColorOfWhoToMove();
        final ChessmanColor secondColor = firstColor.invert();
        final Comparator<CellCoords> comparator = firstColor == WHITE
                ? WHITE_SIDE_CELL_COMPARATOR
                : BLACK_SIDE_CELL_COMPARATOR;
        result.add(createSummaryQuizCard(firstColor, audioMode));
        result.add(createQuizCard(listOf(PAWN), firstColor, false, comparator, audioMode?(firstColor + " pawns positions"):null));
        result.add(createQuizCard(SHAPES_TO_LIST_P1, firstColor, true, comparator, audioMode?(firstColor + " generals positions"):null));
        result.add(createQuizCard(SHAPES_TO_LIST_P2, firstColor, true, comparator, audioMode?(firstColor + " officers positions"):null));
        result.add(createQuizCard(listOf(PAWN), secondColor, false, comparator, audioMode?(secondColor + " pawns positions"):null));
        result.add(createQuizCard(SHAPES_TO_LIST_P1, secondColor, true, comparator, audioMode?(secondColor + " generals positions"):null));
        result.add(createQuizCard(SHAPES_TO_LIST_P2, secondColor, true, comparator, audioMode?(secondColor + " officers positions"):null));
        return result;
    }

    private ChessmenPositionQuizCard createQuizCard(
            List<PieceShape> shapesInCard, ChessmanColor color, boolean putSymbol, Comparator<CellCoords> comparator,
            String question) {
        List<ChessmanType> piecesInCard = shapesInCard.stream()
                .map(shape -> ChessmanType.getByColorAndShape(color, shape))
                .collect(Collectors.toList());
        List<String> answer = new ArrayList<>();
        for (ChessmanType pieceInCard : piecesInCard) {
            final Stream<CellCoords> answerStream = getStartPosition().getMove().getResultPosition()
                    .findAll(ct -> ct == pieceInCard)
                    .stream()
                    .sorted(comparator);
            if (question == null) {
                answer.addAll(
                        answerStream.map(
                                coords -> (putSymbol ? pieceInCard.getSymbol() : "")
                                        + ChessUtils.coordsToString(coords)
                        ).collect(Collectors.toList())
                );
            } else {
                answer.addAll(
                        answerStream.map(
                                coords -> (putSymbol ? (pieceShapeToPhoneticWord(pieceInCard.getPieceShape()) + ", ") : "")
                                        + ChessUtils.coordsToPhonetic(coords)
                        ).collect(Collectors.toList())
                );
            }
        }
        return ChessmenPositionQuizCard.builder()
                .question(
                        question != null ? question :
                        piecesInCard.stream()
                                .map(ChessmanType::getSymbol)
                                .reduce("", (a, b) -> a + b)
                )
                .answer(answer.isEmpty()?listOf("-"):answer)
                .build();
    }

    private String pieceShapeToPhoneticWord(PieceShape pieceShape) {
        switch (pieceShape) {
            case PAWN:
                return "papa";
            case KNIGHT:
                return "november";
            case BISHOP:
                return "bravo";
            case ROOK:
                return "romeo";
            case QUEEN:
                return "quebec";
            case KING:
                return "kilo";
            default:
                return "unexpected piece shape";
        }
    }

    private List<String> createSummaryOfPosition(ChessmanColor color, boolean audioMode) {
        final int numOfPawns = countPieces(ChessmanType.getByColorAndShape(color, PAWN));
        final int numOfQueens = countPieces(ChessmanType.getByColorAndShape(color, QUEEN));
        final int numOfRooks = countPieces(ChessmanType.getByColorAndShape(color, ROOK));
        final int numOfBishops = countPieces(ChessmanType.getByColorAndShape(color, BISHOP));
        final int numOfKnights = countPieces(ChessmanType.getByColorAndShape(color, KNIGHT));

        final ArrayList<String> result = new ArrayList<>();
        if (!audioMode) {
            result.add(color == WHITE ? "W" : "B");
            result.add((numOfPawns==0?"":numOfPawns) +"");
            result.add(StringUtils.repeat('*', numOfQueens));
            result.add(
                    numOfRooks == 0 ? ""
                            : numOfRooks == 1 ? "T"
                            : numOfRooks == 2 ? "H"
                            : StringUtils.repeat('T', numOfRooks)

            );
            result.add(
                    numOfBishops == 0 ? ""
                            : numOfBishops == 1 ? "/"
                            : numOfBishops == 2 ? "X"
                            : StringUtils.repeat('/', numOfBishops)
            );
            result.add(
                    numOfKnights == 0 ? ""
                            : numOfKnights == 1 ? "o"
                            : numOfKnights == 2 ? "8"
                            : StringUtils.repeat('o', numOfKnights)
            );
        } else {
            result.add((color == WHITE ? "White" : "Black") + " pieces");
            addNumberOfPieces(result, numOfPawns, ", papa");
            addNumberOfPieces(result, numOfQueens, ", quebec");
            addNumberOfPieces(result, numOfRooks, ", romeo");
            addNumberOfPieces(result, numOfBishops, ", bravo");
            addNumberOfPieces(result, numOfKnights, ", november");
        }

        return result;
    }

    private void addNumberOfPieces(ArrayList<String> result, int numOfPieces, String name) {
        if (numOfPieces > 0) {
            result.add(numOfPieces + " " + name);
        }
    }

    private int countPieces(ChessmanType chessmanType) {
        return (int) getStartPosition().getMove().getResultPosition()
                .findAll(ct -> ct == chessmanType)
                .stream()
                .count();
    }

    private ChessmenPositionQuizCard createSummaryQuizCard(ChessmanColor firstColor, boolean audioMode) {
        final ArrayList<String> answer = new ArrayList<>();
        answer.addAll(createSummaryOfPosition(firstColor, audioMode));
        if (!audioMode) {
            answer.add(" ");
        }
        answer.addAll(createSummaryOfPosition(firstColor.invert(), audioMode));
        return ChessmenPositionQuizCard.builder()
                .question(audioMode ? "Start position summary" : "")
                .answer(answer)
                .build();
    }

    private void renderGraphicalChessboard(ChessComponentView chessComponentView) {
        chessComponentView.setChessBoard(ChessViewConverter.toDto(getCurrentPosition().getMove().getResultPosition()));
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
        addCommand(HIDE_SHOW_CHESSBOARD_CMD, (args, prgCallback) -> hideShowChessboard());
        addCommand(PREV_POSITION_CMD, (args, prgCallback) -> goToPrevPosition());
        addCommand(NEXT_POSITION_CMD, (args, prgCallback) -> goToNextPosition());
        addCommand(GENERATE_NEXT_MOVE_CMD, (args, prgCallback) -> generateNextMove(prgCallback));
        addCommand(AUTO_RESPONSE_CMD, (args, prgCallback) -> setAutoresponse(prgCallback));
        addCommand(DELETE_ALL_TO_THE_RIGHT_CMD, (args, prgCallback) -> deleteAllToTheRight());
        addCommand(GO_TO_START_POSITION_CMD, (args, prgCallback) -> goToStartPosition());
        addCommand(GO_TO_END_POSITION_CMD, (args, prgCallback) -> goToEndPosition());
        addCommand(GO_TO_POSITION_CMD, (args, prgCallback) -> {
            final String destination = args[1];
            goToPosition(
                    Integer.parseInt(destination.substring(0, destination.length()-1)),
                    destination.charAt(destination.length()-1) == 'w' ? WHITE : BLACK
            );
        });
        addCommand(SET_DEPTH_CMD, (args, prgCallback) -> {
            final int depth = Integer.parseInt(args[1]);
            if (1 <= depth && depth <= MAX_DEPTH) {
                state.setDepth(depth);
            } else {
                state.setCommandErrorMsg("Incorrect value of depth: " + depth);
            }
        });
        addCommand(GRAPHIC_MODE_CMD, (args, prgCallback) -> state.setChessboardMode(ChessboardMode.GRAPHIC));
        addCommand(TEXT_MODE_CMD, (args, prgCallback) -> state.setChessboardMode(ChessboardMode.TEXT));
        addCommand(SEQUENCE_MODE_CMD, (args, prgCallback) -> state.setChessboardMode(ChessboardMode.SEQUENCE));
        addCommand(AUDIO_MODE_CMD, (args, prgCallback) -> state.setChessboardMode(ChessboardMode.AUDIO));
        addCommand(CASE_INSENSITIVE_MODE_CMD, (args, prgCallback) -> {
            state.setCaseInsensitiveMode(!state.isCaseInsensitiveMode());
        });
        addCommand(COMPARE_POSITION_CMD, this::comparePosition);
    }

    private void addCommand(String commandName, BiConsumer<String[],Consumer<String>> commandHandler) {
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
        if (state.getPracticeState() != null) {
            state.getPracticeState().setFailed(true);
        }
    }

    private void generateNextMove(Consumer<String> progressCallback) {
        if (canMakeMove()) {
            final Move currMove = state.getCurrPosition().getMove();
            if (!currMove.isStaleMate() && !currMove.isCheckMate()) {
                Move nextMove;
                try {
                    if (progressCallback != null) {
                        progressCallback.accept(COMPUTER_IS_THINKING);
                    }
                    final long[] lastUpdateSentAt = {System.currentTimeMillis()};
                    nextMove = StockFishRunner.getNextMove(
                            runStockfishCmd,
                            currMove,
                            state.getDepth(),
                            depthInfo -> {
                                if (progressCallback != null && System.currentTimeMillis() - lastUpdateSentAt[0] >= 5000) {
                                    lastUpdateSentAt[0] = System.currentTimeMillis();
                                    progressCallback.accept(
                                            COMPUTER_IS_THINKING + " "
                                                    + depthInfo.getLeft() + "/" + depthInfo.getRight()
                                    );
                                }
                            }
                    );
                } catch (IOException ex) {
                    throw new OutlineException(ex);
                } finally {
                    if (progressCallback != null) {
                        progressCallback.accept(null);
                    }
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
        if (state.getPracticeState() != null) {
            state.getPracticeState().setFailed(true);
        }
    }

    private void goToStartPosition() {
        state.setCurrPosition(state.getInitialPosition());
        state.setPreparedMoves(null);
    }

    private void deleteAllToTheRight() {
        state.getCurrPosition().setChildren(new ArrayList<>());
    }

    private void setAutoresponse(Consumer<String> progressCallback) {
        if (state.getAutoResponseForColor()!=null) {
            state.setAutoResponseForColor(null);
        } else {
            if (canMakeMove()) {
                state.setAutoResponseForColor(state.getCurrPosition().getMove().getColorOfWhoMadeMove());
                execChessCommand(GENERATE_NEXT_MOVE_CMD, progressCallback);
            }
        }
    }

    private void generateAutoresponseIfNecessary(Consumer<String> progressCallback) {
        if (state.getAutoResponseForColor() == state.getCurrPosition().getMove().getColorOfWhoMadeMove().invert()) {
            if (state.getPracticeState() != null && !state.getCurrPosition().getChildren().isEmpty()) {
                processSelectedMove(state.getCurrPosition().getChildren().get(0).getMove(), progressCallback);
            } else {
                execChessCommand(GENERATE_NEXT_MOVE_CMD, progressCallback);
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
                        shapeCodeSelected == 7 ? (whoMadeMove == WHITE ? QUEEN : KNIGHT) :
                        shapeCodeSelected == 6 ? (whoMadeMove == WHITE ? ROOK : BISHOP) :
                        shapeCodeSelected == 5 ? (whoMadeMove == WHITE ? BISHOP : ROOK) :
                        shapeCodeSelected == 4 ? (whoMadeMove == WHITE ? KNIGHT : QUEEN) : null;
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

    private GamePosition getCurrentPosition() {
        return state.getCurrPosition();
    }

    private GamePosition getStartPosition() {
        return state.getInitialPosition();
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

    private void comparePosition(String[] allArgs, Consumer<String> progressCallback) {
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
