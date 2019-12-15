package org.igye.outline2.chess.manager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.igye.outline2.chess.dto.ParsedPgnDto;
import org.igye.outline2.chess.manager.analyse.PgnParser;
import org.igye.outline2.chess.model.CellCoords;
import org.igye.outline2.exceptions.OutlineException;
import org.igye.outline2.manager.NodeManager;
import org.igye.outline2.manager.NodeRepository;
import org.igye.outline2.pm.Node;
import org.igye.outline2.pm.NodeClasses;
import org.igye.outline2.pm.TagIds;
import org.igye.outline2.rpc.RpcMethod;
import org.igye.outline2.rpc.RpcMethodsCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.UUID;

@RpcMethodsCollection
@Component
public class ChessPuzzleManager {
    @Autowired
    private NodeManager nodeManager;
    @Autowired
    private NodeRepository nodeRepository;
    @Autowired(required = false)
    private Clock clock = Clock.systemUTC();
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ApplicationContext applicationContext;

    @RpcMethod
    @Transactional
    public void rpcSaveChessPuzzleAttempt(UUID puzzleId, Boolean passed, String pauseDuration) {
        Node puzzle = nodeRepository.getOne(puzzleId);
        Node attempt = nodeRepository.getOne(nodeManager.rpcCreateNode(puzzleId, NodeClasses.CHESS_PUZZLE_ATTEMPT));
        final Long delaySeconds = calculateDelaySeconds(pauseDuration);
        final String activation = delaySeconds==null?null:clock.instant().plusSeconds(delaySeconds).toString();

        puzzle.setTagSingleValue(TagIds.CHESS_PUZZLE_PASSED, passed.toString());
        puzzle.setTagSingleValue(TagIds.CHESS_PUZZLE_DELAY, pauseDuration);
        puzzle.setTagSingleValue(TagIds.CHESS_PUZZLE_DELAY_MS,
                delaySeconds==null?null:Long.valueOf(delaySeconds*1000).toString()
        );
        puzzle.setTagSingleValue(TagIds.CHESS_PUZZLE_ACTIVATION, activation);

        attempt.setTagSingleValue(TagIds.CHESS_PUZZLE_PASSED, passed.toString());
        attempt.setTagSingleValue(TagIds.CHESS_PUZZLE_DELAY, pauseDuration);
    }

    @RpcMethod
    @Transactional
    public void rpcSavePgn(UUID gameId, String pgn) throws JsonProcessingException {
        Node game = nodeRepository.getOne(gameId);
        game.setTagSingleValue(TagIds.CHESS_GAME_PGN, pgn);
        ParsedPgnDto parsedPgnDto = PgnParser.parsePgn(pgn);
        game.setTagSingleValue(TagIds.CHESS_GAME_PARSED_PGN, objectMapper.writeValueAsString(parsedPgnDto));
        String url = PgnParser.getAttrValue(pgn, "Link");
        if (url != null) {
            game.setTagSingleValue(TagIds.CHESS_GAME_URL, url);
        }
    }

    @RpcMethod
    @Transactional
    public UUID rpcCreatePuzzleFromGame(UUID gameId, String fen, String move) {
        Node game = nodeRepository.getOne(gameId);
        UUID puzzleId = nodeManager.rpcCreateNode(game.getParentNode().getId(), NodeClasses.CHESS_PUZZLE);
        ChessManager chessManager = (ChessManager) applicationContext.getBean(ChessManager.CHESSBOARD);
        chessManager.setPositionFromFen(fen);
        chessManager.chessTabSelected(ChessComponentStage.MOVES);
        chessManager.cellLeftClicked(new CellCoords(
                ChessUtils.strCoordToInt(move.charAt(0)),
                ChessUtils.strCoordToInt(move.charAt(1))
        ));
        chessManager.cellLeftClicked(new CellCoords(
                ChessUtils.strCoordToInt(move.charAt(2)),
                ChessUtils.strCoordToInt(move.charAt(3))
        ));
        rpcSavePgnForPuzzle(puzzleId, chessManager.getPgnToSave().getSavePgn());
        return puzzleId;
    }

    @RpcMethod
    @Transactional
    public void rpcSavePgnForPuzzle(UUID puzzleId, String pgn) {
        Node puzzle = nodeRepository.getOne(puzzleId);
        if (!StringUtils.isBlank(pgn)) {
            puzzle.setTagSingleValue(TagIds.CHESS_PUZZLE_PGN, pgn);
            ParsedPgnDto parsedPgnDto = PgnParser.parsePgn(pgn);
            puzzle.setTagSingleValue(TagIds.CHESS_PUZZLE_FEN, parsedPgnDto.getInitialPositionFen());
        } else {
            puzzle.removeTags(TagIds.CHESS_PUZZLE_PGN);
            puzzle.removeTags(TagIds.CHESS_PUZZLE_FEN);
        }
    }

    private Long calculateDelaySeconds(String pauseDuration) {
        if (StringUtils.isBlank(pauseDuration)) {
            return null;
        }
        long amount = Long.parseLong(pauseDuration.substring(0,pauseDuration.length()-1));
        String unit = pauseDuration.substring(pauseDuration.length()-1);
        if ("M".equals(unit)) {
            amount *= 30;
            unit = "d";
        }
        return getChronoUnit(unit).getDuration().getSeconds()*amount;
    }

    private TemporalUnit getChronoUnit(String unit) {
        switch (unit) {
            case "m": return ChronoUnit.MINUTES;
            case "h": return ChronoUnit.HOURS;
            case "d": return ChronoUnit.DAYS;
            default: throw new OutlineException("Unrecognized time interval unit: " + unit);
        }
    }
}
