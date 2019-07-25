package org.igye.outline2.controllers;

import org.igye.outline2.dto.NodeDto;
import org.igye.outline2.manager.NodeManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequestMapping("/be")
@ResponseBody
public class BeController {
    @Autowired
    private NodeManager nodeManager;

    @GetMapping("/node")
    public NodeDto getNode(@RequestParam(required = false, defaultValue = "0") Integer depth) {
        // TODO: 22.07.2019 tc: if depth != 0 but the node doesn't have children then return empty array
        // TODO: 22.07.2019 tc: if depth == 0 - don't return childNodes attr at all disregarding presence of child nodes
        // TODO: 22.07.2019 tc: by default depth == 0
        return nodeManager.getNode(null, depth);
    }

    @GetMapping("/node/{id}")
    public NodeDto getNode(@PathVariable UUID id,
                           @RequestParam(required = false, defaultValue = "0") Integer depth) {
        // TODO: 22.07.2019 tc: if depth != 0 but the node doesn't have children then return empty array
        // TODO: 22.07.2019 tc: if depth == 0 - don't return childNodes attr at all disregarding presence of child nodes
        // TODO: 22.07.2019 tc: by default depth == 0
        return nodeManager.getNode(id, depth);
    }

    // TODO: 22.07.2019 tc: in PATCH method, absent attributes are not changed
}
