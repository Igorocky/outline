package org.igye.outline2.controllers;

import org.igye.outline2.dto.ImageDto;
import org.igye.outline2.dto.NodeDto;
import org.igye.outline2.manager.ImageManager;
import org.igye.outline2.manager.NodeManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Controller
@RequestMapping("/be")
@ResponseBody
public class BeController {
    @Autowired
    private NodeManager nodeManager;
    @Autowired
    private ImageManager imageManager;

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
    @PatchMapping("/node")
    public NodeDto patchNode(@RequestBody NodeDto request) {
        return nodeManager.patchNode(request);
    }

    @PostMapping("/uploadImage")
    @ResponseBody
    public ImageDto uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        return imageManager.createImage(file);
    }

    @GetMapping("/image/{id}")
    @ResponseBody
    public byte[] getImage(@PathVariable UUID id) {
        return imageManager.getImgFileById(id);
    }


}
