package org.igye.outline2.chess.manager;

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

    @RpcMethod
    @Transactional
    public void rpcSaveChessPuzzleAttempt(UUID puzzleId, Boolean passed, String pauseDuration) {
        Node puzzle = nodeRepository.getOne(puzzleId);
        Node attempt = nodeRepository.getOne(nodeManager.rpcCreateNode(puzzleId, NodeClasses.CHESS_PUZZLE_ATTEMPT));
        final String activation = calculateActivation(pauseDuration);
        setAttemptTags(puzzle, passed, pauseDuration, activation);
        setAttemptTags(attempt, passed, pauseDuration, activation);
    }

    private void setAttemptTags(Node node, Boolean passed, String pauseDuration, String activation) {
        node.setTagSingleValue(TagIds.CHESS_PUZZLE_PASSED, passed.toString());
        node.setTagSingleValue(TagIds.CHESS_PUZZLE_DELAY, pauseDuration);
        node.setTagSingleValue(TagIds.CHESS_PUZZLE_ACTIVATION, activation);
    }

    private String calculateActivation(String pauseDuration) {
        long amount = Long.parseLong(pauseDuration.substring(0,pauseDuration.length()-1));
        String unit = pauseDuration.substring(pauseDuration.length()-1);
        if ("M".equals(unit)) {
            amount *= 30;
            unit = "d";
        }
        return Instant.now().plus(amount, getChronoUnit(unit)).toString();
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
