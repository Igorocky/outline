package org.igye.outline2.chess.manager;

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

import java.util.UUID;

@RpcMethodsCollection
@Component
public class ChessPuzzleManager {
    @Autowired
    private NodeRepository nodeRepository;

//    @RpcMethod
//    @Transactional
//    public void rpcSaveChessPuzzleComment(UUID puzzleId, String text) {
//        Node puzzle = nodeRepository.getOne(puzzleId);
//        final Node comment = Node.builder().clazz(NodeClasses.CHESS_PUZZLE_COMMENT).build();
//        puzzle.addChild(comment);
//        comment.setTagSingleValue(TagIds.CHESS_PUZZLE_COMMENT_TEXT, text);
//    }
//
//    @RpcMethod
//    @Transactional
//    public void rpcUpdateChessPuzzleComment(UUID commentId, String newText) {
//        nodeRepository.getOne(commentId).setTagSingleValue(TagIds.CHESS_PUZZLE_COMMENT_TEXT, newText);
//    }
}
