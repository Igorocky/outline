package org.igye.outline2.controllers;

import org.igye.outline2.dto.NodeDto;
import org.igye.outline2.manager.NodeManager;
import org.igye.outline2.rpc.RpcMethod;
import org.igye.outline2.rpc.RpcMethodsCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

import static org.igye.outline2.OutlineUtils.nullSafeGetter;

@Component
public class NodeMethods implements RpcMethodsCollection {
    @Autowired
    private NodeManager nodeManager;

    @RpcMethod
    public NodeDto getNode(Optional<UUID> id, Optional<Integer> depth, Optional<Boolean> includeCanPaste) {
        return nodeManager.getNode(
                nullSafeGetter(id, opt->opt.orElse(null)),
                nullSafeGetter(depth, opt->opt.orElse(0)),
                nullSafeGetter(includeCanPaste, opt->opt.orElse(false))
        );
    }
}
