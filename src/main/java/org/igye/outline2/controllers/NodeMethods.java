package org.igye.outline2.controllers;

import org.igye.outline2.dto.NodeDto;
import org.igye.outline2.manager.Clipboard;
import org.igye.outline2.manager.NodeManager;
import org.igye.outline2.rpc.Default;
import org.igye.outline2.rpc.RpcMethod;
import org.igye.outline2.rpc.RpcMethodsCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class NodeMethods implements RpcMethodsCollection {
    @Autowired
    private NodeManager nodeManager;
    @Autowired
    private Clipboard clipboard;

    @RpcMethod
    public NodeDto rpcGetNode(@Default("null") UUID id,
                              @Default("0") Integer depth,
                              @Default("false") Boolean includeCanPaste) {
        return nodeManager.getNode(id, depth, includeCanPaste);
    }

    @RpcMethod
    public NodeDto rpcPatchNode(NodeDto request) {
        return nodeManager.patchNode(request);
    }

    @RpcMethod
    public void rpcReorderNode(UUID nodeId, int direction) {
        nodeManager.reorderNode(nodeId, direction);
    }

    @RpcMethod
    public void rpcPutNodeIdsToClipboard(List<UUID> ids) {
        clipboard.setNodeIds(ids);
    }

    @RpcMethod
    public void rpcPasteNodesFromClipboard(UUID to) {
        nodeManager.moveNodesFromClipboard(to);
    }
}
