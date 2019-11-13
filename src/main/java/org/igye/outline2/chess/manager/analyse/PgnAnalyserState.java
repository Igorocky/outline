package org.igye.outline2.chess.manager.analyse;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.igye.outline2.chess.dto.ParsedPgnDto;
import org.igye.outline2.manager.NodeRepository;
import org.igye.outline2.pm.Node;
import org.igye.outline2.pm.TagIds;
import org.igye.outline2.rpc.RpcMethod;
import org.igye.outline2.websocket.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.UUID;

@Component("PgnAnalyser")
@Scope("prototype")
public class PgnAnalyserState extends State {
    @Value("${chess.stockfish.cmd:null}")
    private String stockfishCmd;
    @Value("${chess.stockfish.proc-num}")
    private int stockfishProcNum;
    @Value("${chess.stockfish.depth}")
    private int stockfishDepth;
    @Autowired
    private NodeRepository nodeRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @RpcMethod
    @Transactional
    public void analyseGame(UUID gameId) throws IOException {
        Node game = nodeRepository.getOne(gameId);
        ParsedPgnDto parsedPgnDto = PgnAnalyser.analysePgn(
                stockfishCmd,
                game.getTagSingleValue(TagIds.CHESS_GAME_PGN),
                stockfishDepth,
                null,
                stockfishProcNum,
                analysisProgressInfo -> sendMessageToFe(analysisProgressInfo)
        );
        game.setTagSingleValue(TagIds.CHESS_GAME_PARSED_PGN, objectMapper.writeValueAsString(parsedPgnDto));
        sendMessageToFe("done");
    }

}
