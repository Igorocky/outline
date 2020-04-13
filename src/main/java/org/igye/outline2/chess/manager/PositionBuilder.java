package org.igye.outline2.chess.manager;

import org.apache.commons.lang3.StringUtils;
import org.igye.outline2.chess.dto.ChessBoardCellView;
import org.igye.outline2.chess.dto.ChessComponentResponse;
import org.igye.outline2.chess.dto.ChessComponentView;
import org.igye.outline2.chess.dto.ChessViewConverter;
import org.igye.outline2.chess.dto.InitialPositionView;
import org.igye.outline2.chess.model.CellCoords;
import org.igye.outline2.chess.model.ChessBoard;
import org.igye.outline2.chess.model.ChessmanColor;
import org.igye.outline2.chess.model.ChessmanType;
import org.igye.outline2.chess.model.Move;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import static org.igye.outline2.common.OutlineUtils.nullSafeGetterWithDefault;

public class PositionBuilder implements ChessComponentStateManager {
    public static final int RECYCLE_BIN_CODE = 10007;
    private static final String SELECTED_CELL_BACKGROUND_COLOR = "yellow";
    private static final String NOT_SELECTED_CELL_BACKGROUND_COLOR = "white";
    private static final String EMPTY_BOARD_FEN = "8/8/8/8/8/8/8/8 w - - 0 1";

    private ChessBoard chessBoard;
    private String chessBoardText;
    private ChessmanColor colorToMove;
    private boolean whiteLongCastlingIsAvailable;
    private boolean whiteShortCastlingIsAvailable;
    private boolean blackLongCastlingIsAvailable;
    private boolean blackShortCastlingIsAvailable;
    private String enPassantTargetSquare;
    private int halfmoveClock;
    private int fullmoveNumber;
    private String commandResponseMsg;
    private String commandErrorMsg;

    private ChessBoardCellView[][] availablePieces;
    private int selectedCode;
    public static final Map<Character, ChessmanType> chessmenTypesMap = Collections.unmodifiableMap(
            Stream.of(ChessmanType.values())
                    .collect(Collectors.toMap(
                            (ChessmanType ct) -> ct.getPieceColor() == ChessmanColor.WHITE
                                    ? ct.getPieceShape().getSymbol().toUpperCase().charAt(0)
                                    : ct.getPieceShape().getSymbol().toLowerCase().charAt(0),
                            Function.identity()
                    ))
    );
    private Map<String, Consumer<String[]>> commands = new HashMap<>();
    private static final String PLACE_PIECES_RANDOMLY_CMD = "rnd";

    public PositionBuilder(String initialPositionFen) {
        Move initialPosition = new Move(initialPositionFen);
        chessBoard = initialPosition.getResultPosition();
        colorToMove = initialPosition.getColorOfWhoToMove();
        whiteShortCastlingIsAvailable = initialPosition.isWhiteKingCastleAvailable();
        whiteLongCastlingIsAvailable = initialPosition.isWhiteQueenCastleAvailable();
        blackShortCastlingIsAvailable = initialPosition.isBlackKingCastleAvailable();
        blackLongCastlingIsAvailable = initialPosition.isBlackQueenCastleAvailable();
        enPassantTargetSquare = initialPosition.getEnPassantTargetSquare();
        halfmoveClock = initialPosition.getHalfmoveClock();
        fullmoveNumber = initialPosition.getFullmoveNumber();
        initAvailablePieces();
        unhighlightAvailablePieces();
        availablePieces[6][1] = createCell(6,1, RECYCLE_BIN_CODE);

        ChessBoardCellView selectedPiece = availablePieces[0][1];
        selectedPiece.setBackgroundColor(SELECTED_CELL_BACKGROUND_COLOR);
        selectedCode = selectedPiece.getCode();

        commands.put(PLACE_PIECES_RANDOMLY_CMD, args -> placePiecesRandomly(args[1]));
    }

    @Override
    public ChessComponentResponse toView() {
        ChessComponentView result = new ChessComponentView();
        result.setChessBoard(ChessViewConverter.toDto(chessBoard));
        result.setChessBoardText(ChessUtils.renderTextChessboard(chessBoard, colorToMove));
        result.getChessBoard().setBoardRotated(colorToMove == ChessmanColor.BLACK);
        result.setTab(ChessComponentStage.INITIAL_POSITION);
        result.setAvailableChessmanTypes(InitialPositionView.builder()
                .availableChessmanTypes(availablePieces)
                .colorToMove(colorToMove)
                .whiteLongCastlingIsAvailable(whiteLongCastlingIsAvailable)
                .whiteShortCastlingIsAvailable(whiteShortCastlingIsAvailable)
                .blackLongCastlingIsAvailable(blackLongCastlingIsAvailable)
                .blackShortCastlingIsAvailable(blackShortCastlingIsAvailable)
                .fen(getInitialPosition().toFen())
                .build()
        );
        result.setCommandResponseMsg(commandResponseMsg);
        result.setCommandErrorMsg(commandErrorMsg);
        result.setNoMovesRecorded(true);
        return ChessComponentResponse.builder().chessComponentView(result).build();
    }

    @Override
    public ChessComponentResponse cellLeftClicked(CellCoords coords) {
        if (coords.getX() >= 10) {
            ChessBoardCellView availablePiece = findAvailablePiece(coords);
            if (availablePiece.getCode() > 0) {
                unhighlightAvailablePieces();
                availablePiece.setBackgroundColor(SELECTED_CELL_BACKGROUND_COLOR);
                selectedCode = availablePiece.getCode();
            }
        } else if (selectedCode == RECYCLE_BIN_CODE) {
            chessBoard.placePiece(coords, null);
        } else {
            int codeOnTheCell = nullSafeGetterWithDefault(
                    chessBoard.getPieceAt(coords),
                    ChessmanType::getCode,
                    -1
            );
            if (selectedCode == codeOnTheCell) {
                chessBoard.placePiece(coords, null);
            } else {
                chessBoard.placePiece(coords, ChessmanType.fromCode(selectedCode));
            }
        }
        return toView();
    }

    @Override
    public ChessComponentResponse execChessCommand(String command, Consumer<String> progressCallback) {
        commandErrorMsg = null;
        commandResponseMsg = null;
        String[] parsedCommand = command.trim().split("\\s");
        if (commands.containsKey(parsedCommand[0])) {
            commands.get(parsedCommand[0]).accept(parsedCommand);
        } else {
            commandErrorMsg = "Unknown command: '" + command + "'";
        }
        return toView();
    }

    @Override
    public ChessComponentResponse setColorToMove(ChessmanColor colorToMove) {
        this.colorToMove = colorToMove;
        return toView();
    }

    @Override
    public ChessComponentResponse changeCastlingAvailability(ChessmanColor color, boolean isLong) {
        if (color == ChessmanColor.WHITE) {
            if (isLong) {
                whiteLongCastlingIsAvailable = !whiteLongCastlingIsAvailable;
            } else {
                whiteShortCastlingIsAvailable = !whiteShortCastlingIsAvailable;
            }
        } else {
            if (isLong) {
                blackLongCastlingIsAvailable = !blackLongCastlingIsAvailable;
            } else {
                blackShortCastlingIsAvailable = !blackShortCastlingIsAvailable;
            }
        }
        return toView();
    }

    @Override
    public ChessComponentResponse setPositionFromFen(String fen) {
        Move move = new Move(StringUtils.trim(fen));
        chessBoard = move.getResultPosition();
        colorToMove = move.getColorOfWhoToMove();
        whiteShortCastlingIsAvailable = move.isWhiteKingCastleAvailable();
        whiteLongCastlingIsAvailable = move.isWhiteQueenCastleAvailable();
        blackShortCastlingIsAvailable = move.isBlackKingCastleAvailable();
        blackLongCastlingIsAvailable = move.isBlackQueenCastleAvailable();
        enPassantTargetSquare = move.getEnPassantTargetSquare();
        halfmoveClock = move.getHalfmoveClock();
        fullmoveNumber = move.getFullmoveNumber();
        return toView();
    }

    @Override
    public ChessComponentResponse showCorrectMove() {
        notSupported();
        return null;
    }

    @Override
    public ChessComponentResponse setAutoResponseForOpponent() {
        notSupported();
        return null;
    }

    @Override
    public ChessComponentResponse hideCommandResponseMsg() {
        commandResponseMsg = null;
        commandErrorMsg = null;
        return toView();
    }

    public Move getInitialPosition() {
        return new Move(
                chessBoard,
                colorToMove,
                whiteShortCastlingIsAvailable,
                whiteLongCastlingIsAvailable,
                blackShortCastlingIsAvailable,
                blackLongCastlingIsAvailable,
                enPassantTargetSquare,
                halfmoveClock,
                fullmoveNumber
        );
    }

    public ChessmanColor getColorToMove() {
        return colorToMove;
    }

    private void initAvailablePieces() {
        ChessmanType[][] availableChessmen = new ChessmanType[][]{
                {WHITE_PAWN, BLACK_PAWN},
                {WHITE_KNIGHT, BLACK_KNIGHT},
                {WHITE_BISHOP, BLACK_BISHOP},
                {WHITE_ROOK, BLACK_ROOK},
                {WHITE_QUEEN, BLACK_QUEEN},
                {WHITE_KING, BLACK_KING},
                {null, null},
        };
        this.availablePieces = ChessUtils.emptyBoard(
                ChessBoardCellView[].class, ChessBoardCellView.class, 7, 2, (x,y)->
                createCell(x, y, availableChessmen[x][y])
        );
    }

    private void traverseCells(ChessBoardCellView[][] cells, Consumer<ChessBoardCellView> consumer) {
        for (ChessBoardCellView[] row : cells) {
            for (ChessBoardCellView cell : row) {
                consumer.accept(cell);
            }
        }
    }

    private ChessBoardCellView createCell(int x, int y, int code) {
        return ChessBoardCellView.builder()
                .backgroundColor(NOT_SELECTED_CELL_BACKGROUND_COLOR)
                .code(code)
                .coords(new CellCoords(x + 10, y))
                .build();
    }

    private ChessBoardCellView createCell(int x, int y, ChessmanType chessmanType) {
        return createCell(x, y, chessmanType ==null?0: chessmanType.getCode());
    }

    private void unhighlightAvailablePieces() {
        traverseCells(availablePieces, cell -> {
            if (cell!=null) {
                cell.setBackgroundColor(NOT_SELECTED_CELL_BACKGROUND_COLOR);
            }
        });
    }

    private ChessBoardCellView findAvailablePiece(CellCoords coords) {
        final ChessBoardCellView[] result = new ChessBoardCellView[1];
        traverseCells(availablePieces, cell -> {
            if (cell!=null && cell.getCoords().equals(coords)) {
                result[0] = cell;
            }
        });
        return result[0];
    }

    private void placePiecesRandomly(String pieces) {
        for (char ch : pieces.toCharArray()) {
            if (!chessmenTypesMap.containsKey(ch)) {
                commandErrorMsg = "Unknown chessman type '" + ch + "'";
                return;
            }
        }
        setPositionFromFen(EMPTY_BOARD_FEN);
        List<CellCoords> availableCells = Stream.iterate(0, i->i+1).limit(8)
                .flatMap(x -> Stream.iterate(0, i->i+1).limit(8).map(y -> new CellCoords(x,y)))
                .collect(Collectors.toList());
        Random rnd = new Random();
        for (char ch : pieces.toCharArray()) {
            chessBoard.placePiece(availableCells.get(rnd.nextInt(availableCells.size())), chessmenTypesMap.get(ch));
        }
    }
}
