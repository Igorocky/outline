package org.igye.outline2.chess.manager;

import org.apache.commons.lang3.StringUtils;
import org.igye.outline2.exceptions.OutlineException;
import org.igye.outline2.manager.NodeManager;
import org.igye.outline2.manager.NodeRepository;
import org.igye.outline2.pm.Node;
import org.igye.outline2.pm.NodeClasses;
import org.igye.outline2.pm.TagIds;
import org.igye.outline2.rpc.RpcMethod;
import org.igye.outline2.rpc.RpcMethodsCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
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
    public void rpcSavePgn(UUID gameId, String pgn) {
        Node game = nodeRepository.getOne(gameId);
        game.setTagSingleValue(TagIds.CHESS_GAME_PGN, pgn);
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
