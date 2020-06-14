package org.igye.outline2.chess.manager;

import org.igye.outline2.chess.dto.ChessmenPositionQuizCard;
import org.igye.outline2.common.OutlineUtils;
import org.igye.outline2.dto.NodeDto;
import org.igye.outline2.exceptions.OutlineException;
import org.igye.outline2.manager.NodeManager;
import org.igye.outline2.pm.TagIds;
import org.igye.outline2.report.ReportManager;
import org.igye.outline2.report.ResultSetDto;
import org.igye.outline2.rpc.RpcMethod;
import org.igye.outline2.websocket.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component("ChessManagerAudio")
@Scope("prototype")
public class ChessManagerAudioState extends State {

    @Autowired
    private ReportManager reportManager;

    private UUID puzzleId;
    @Autowired
    private ChessManager chessManager;

    @Autowired
    private NodeManager nodeManager;

    @PostConstruct
    public void init() {
        loadNextPuzzle();
    }

    @RpcMethod
    public ChessManagerAudioStateDto getCurrentState() {
        List<ChessmenPositionQuizCard> quiz = chessManager.toView().getChessComponentView().getChessBoardSequence().getQuiz();
        return ChessManagerAudioStateDto.builder()
                .puzzleId(puzzleId)
                .startPosition(quiz)
                .build();
    }

    @Override
    protected Object getViewRepresentation() {
        return getCurrentState();
    }

    private void loadNextPuzzle() {
        ResultSetDto puzzlesToRepeat = reportManager.rpcRunReport("puzzles-to-repeat", Collections.emptyMap());
        UUID puzzleId = null;
        for (Map<String, Object> row : puzzlesToRepeat.getData()) {
            if (((Integer)row.get("HAS_PGN")) == 1) {
                puzzleId = (UUID) row.get("ID");
            }
        }
        if (puzzleId == null) {
            throw new OutlineException("Cannot find next puzzle to load.");
        }
        NodeDto puzzle = nodeManager.rpcGetNode(puzzleId, 0, false, false);
        final String puzzleDepthStr = puzzle.getTagSingleValue(TagIds.CHESS_PUZZLE_DEPTH);
        chessManager.loadFromPgn(
                puzzle.getTagSingleValue(TagIds.CHESS_PUZZLE_PGN),
                ChessComponentStage.MOVES,
                false,
                puzzleDepthStr == null ? null : Integer.parseInt(puzzleDepthStr),
                OutlineUtils.listOf(MovesBuilder.AUDIO_MODE_CMD)
        );
        this.puzzleId = puzzleId;
    }
}
