package org.igye.outline2.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import org.igye.outline2.dto.NodeDto;
import org.igye.outline2.manager.ExportImportManager;
import org.igye.outline2.manager.ImageManager;
import org.igye.outline2.rpc.RpcDispatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

@Controller
@RequestMapping("/be")
@ResponseBody
public class BeController {
    @Autowired
    private ImageManager imageManager;
    @Autowired
    private ExportImportManager exportImportManager;
    @Autowired
    private RpcDispatcher rpcDispatcher;

    @PatchMapping("/rpc/{methodName}")
    public Object rpcEntry(@PathVariable String methodName, @RequestBody JsonNode passedParams) throws IOException, InvocationTargetException, IllegalAccessException {
        return rpcDispatcher.dispatchRpcCall(methodName, passedParams);
    }

    @PostMapping("/uploadImage")
    @ResponseBody
    public NodeDto uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        return imageManager.createImage(file);
    }

    @GetMapping("/image/{id}")
    @ResponseBody
    public byte[] getImage(@PathVariable UUID id) {
        return imageManager.getImgFileById(id);
    }

    @PostMapping("/importFromFile/{parentId}")
    @ResponseBody
    public NodeDto importFromFile(
            @RequestParam("file") MultipartFile file,
            @PathVariable String parentId) throws IOException {
        return exportImportManager.importFromFile(file, "null".equals(parentId)?null:UUID.fromString(parentId));
    }

    @PostMapping("/exportToFile/{parentId}")
    @ResponseBody
    public void exportToFile(@PathVariable String parentId) throws IOException {
        exportImportManager.exportToFile("null".equals(parentId)?null:UUID.fromString(parentId));
    }

}
