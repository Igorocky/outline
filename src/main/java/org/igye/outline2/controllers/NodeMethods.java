package org.igye.outline2.controllers;

import org.igye.outline2.dto.NodeDto;
import org.igye.outline2.manager.NodeManager;
import org.igye.outline2.rpc.Default;
import org.igye.outline2.rpc.RpcMethod;
import org.igye.outline2.rpc.RpcMethodsCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class NodeMethods implements RpcMethodsCollection {
    @Autowired
    private NodeManager nodeManager;

    @RpcMethod
    public NodeDto rpcGetNode(@Default("null") UUID id,
                              @Default("0") Integer depth,
                              @Default("false") Boolean includeCanPaste) {
        return nodeManager.getNode(id, depth, includeCanPaste);
    }
}
