package org.igye.outline.export;

import org.apache.commons.io.FileUtils;
import org.igye.outline.common.OutlineUtils;
import org.igye.outline.data.repository.NodeRepository;
import org.igye.outline.htmlforms.SessionData;
import org.igye.outline.modelv2.ContentV2;
import org.igye.outline.modelv2.ImageV2;
import org.igye.outline.modelv2.NodeV2;
import org.igye.outline.modelv2.ParagraphV2;
import org.igye.outline.modelv2.TopicV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.UUID;

import static org.igye.outline.common.OutlineUtils.getFurthestSibling;
import static org.igye.outline.common.OutlineUtils.getNextSibling;

@Service
public class Exporter {
    @Autowired
    private TemplateEngine templateEngine;
    @Autowired
    private NodeRepository nodeRepository;
    @Value("${tmp.dir}")
    private String tmpDir;
    @Value("${topic.images.location}")
    private String imagesLocation;
    @Autowired
    private SessionData sessionData;

    @Transactional
    public void export(UUID id) throws IOException, InterruptedException {
        File dir = new File(tmpDir + "/" + sessionData.getCurrentUser().getName(), id.toString());
        File imgDir = new File(dir, "img");
        File cssDir = new File(dir, "css");
        File jsDir = new File(dir, "js");

        if (dir.exists()) {
            FileUtils.deleteDirectory(dir);
            Thread.sleep(2000L);
        }
        dir.mkdirs();
        imgDir.mkdirs();
        cssDir.mkdirs();
        jsDir.mkdirs();

        copyResource(cssDir, "static-version-resources/static.css", "static.css");
        copyResource(jsDir, "static-version-resources/static.js", "static.js");

        export(null, nodeRepository.findByOwnerAndId(sessionData.getCurrentUser(), id), dir, imgDir);
    }

    private void copyResource(File cssDir, String srcPath, String dstPath) throws IOException {
        FileUtils.copyInputStreamToFile(
                getClass().getClassLoader().getResourceAsStream(srcPath),
                new File(cssDir, dstPath)
        );
    }

    private void export(ParagraphV2 parent, NodeV2 node, File dir, File imgDir) throws IOException {
        Context ctx = new Context();
        ctx.setVariable("isParagraph", node instanceof ParagraphV2);
        ctx.setVariable("node", node);
        NodeV2 leftSibling = null;
        NodeV2 leftMostSibling = null;
        NodeV2 rightSibling = null;
        NodeV2 rightMostSibling = null;
        NodeV2 firstChild = null;
        final UUID id = node.getId();
        if (parent != null) {
            leftSibling = getNextSibling(parent.getChildNodes(), n -> id.equals(n.getId()), false).orElse(null);
            leftMostSibling = getFurthestSibling(parent.getChildNodes(), n -> id.equals(n.getId()), false).orElse(null);
            rightSibling = getNextSibling(parent.getChildNodes(), n -> id.equals(n.getId()), true).orElse(null);
            rightMostSibling = getFurthestSibling(parent.getChildNodes(), n -> id.equals(n.getId()), true).orElse(null);
        }
        if (isParagraphWithChildren(node)) {
            firstChild = ((ParagraphV2)node).getChildNodes().get(0);
        }
        ctx.setVariable("hasLeftSibling", node);
        ctx.setVariable("leftSibling", leftSibling);
        ctx.setVariable("leftMostSibling", leftMostSibling);
        ctx.setVariable("rightSibling", rightSibling);
        ctx.setVariable("rightMostSibling", rightMostSibling);
        ctx.setVariable("firstChild", firstChild);
        ctx.setVariable("parent", parent);
        try(Writer writer = new FileWriter(new File(dir, id.toString() + ".html"))) {
            templateEngine.process(
                    "export/staticView",
                    ctx,
                    writer
            );

        }
        if (node instanceof TopicV2) {
            TopicV2 topic = (TopicV2) node;
            for (ContentV2 c : topic.getContents()) {
                if (c instanceof ImageV2) {
                    FileUtils.copyFile(
                            OutlineUtils.getImgFile(imagesLocation, c.getId()),
                            new File(imgDir, c.getId().toString())
                    );
                }
            }
        }
        if (isParagraphWithChildren(node)) {
            ParagraphV2 paragraph = (ParagraphV2) node;
            for (NodeV2 cn : paragraph.getChildNodes()) {
                export(paragraph, cn, dir, imgDir);
            }
        }
    }

    private boolean isParagraphWithChildren(NodeV2 node) {
        return (node instanceof ParagraphV2) && ((ParagraphV2)node).getHasChildren();
    }
}
