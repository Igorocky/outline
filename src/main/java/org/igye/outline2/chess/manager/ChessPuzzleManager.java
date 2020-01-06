package org.igye.outline2.chess.manager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.igye.outline2.chess.dto.ChessComponentResponse;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    @Value("${random-factor-default-percent}")
    private int randomFactorDefaultPercent;

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
        String gameName = game.getTagSingleValue(TagIds.NAME);
        if (StringUtils.isBlank(gameName)) {
            String wPlayer = PgnParser.getAttrValue(pgn, "White");
            String bPlayer = PgnParser.getAttrValue(pgn, "Black");
            game.setTagSingleValue(
                    TagIds.NAME,
                    (StringUtils.isBlank(wPlayer)?"???":wPlayer)
                            + " vs "
                            + (StringUtils.isBlank(bPlayer)?"???":bPlayer)
            );
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
        ChessComponentResponse resp = chessManager.cellLeftClicked(new CellCoords(
                ChessUtils.strCoordToInt(move.charAt(2)),
                ChessUtils.strCoordToInt(move.charAt(3))
        ));
        rpcSavePgnForPuzzle(puzzleId, resp.getChessComponentView().getPgn());
        Node puzzle = nodeRepository.getOne(puzzleId);
        puzzle.setTagSingleValue(TagIds.CHESS_PUZZLE_AUTO_RESPONSE, "true");
        puzzle.setTagSingleValue(TagIds.CHESS_PUZZLE_TEXT_MODE, "true");
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

    private Pattern attemptDelayPattern = Pattern.compile("^(\\d+)(M|d|h|m)(r(\\d)?)?$");
    protected Long calculateDelaySeconds(String pauseDuration) {
        if (StringUtils.isBlank(pauseDuration)) {
            return null;
        }
        Matcher matcher = attemptDelayPattern.matcher(pauseDuration);
        if(!matcher.matches()) {
            throw new OutlineException("Pause duration '" + pauseDuration + "' is in incorrect format.");
        }
        long amount = Long.parseLong(matcher.group(1));
        String unit = matcher.group(2);
        if ("M".equals(unit)) {
            amount *= 30;
            unit = "d";
        }
        double randomFactor = 1.0;
        if (matcher.group(3) != null) {
            int randomFactorPercent = matcher.group(4) == null
                    ? randomFactorDefaultPercent : Integer.parseInt(matcher.group(4));
            double delta = randomFactorPercent/10.0;
            randomFactor = randDoubleBetween(1.0-delta, 1.0+delta);
        }
        return Math.round(getChronoUnit(unit).getDuration().getSeconds()*amount*randomFactor);
    }

    private double randDoubleBetween(double left, double right) {
        return left + (new Random()).nextDouble()*(right-left);
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
