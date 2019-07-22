package org.igye.outline2.controllers;

import org.igye.outline2.dto.NodeDto;
import org.igye.outline2.manager.NodeManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.UUID;

@Controller
@RequestMapping("/be")
@ResponseBody
public class BeController {
    @Autowired
    private NodeManager nodeManager;

    @GetMapping("/node")
    public NodeDto getNode() {
        return nodeManager.getNode(null);
    }

    @GetMapping("/node/{id}")
    public NodeDto getNode(UUID id) {
        return nodeManager.getNode(id);
    }
}
