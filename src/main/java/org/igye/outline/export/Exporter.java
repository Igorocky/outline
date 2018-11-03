package org.igye.outline.export;

import org.apache.commons.io.FileUtils;
import org.igye.outline.common.OutlineUtils;
import org.igye.outline.data.repository.NodeRepository;
import org.igye.outline.htmlforms.SessionData;
import org.igye.outline.model.Content;
import org.igye.outline.model.Image;
import org.igye.outline.model.Node;
import org.igye.outline.model.Paragraph;
import org.igye.outline.model.Topic;
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

    private void export(Paragraph parent, Node node, File dir, File imgDir) throws IOException {
        Context ctx = new Context();
        ctx.setVariable("isParagraph", node instanceof Paragraph);
        ctx.setVariable("node", node);
        Node leftSibling = null;
        Node leftMostSibling = null;
        Node rightSibling = null;
        Node rightMostSibling = null;
        Node firstChild = null;
        final UUID id = node.getId();
        if (parent != null) {
            leftSibling = getNextSibling(parent.getChildNodes(), n -> id.equals(n.getId()), false).orElse(null);
            leftMostSibling = getFurthestSibling(parent.getChildNodes(), n -> id.equals(n.getId()), false).orElse(null);
            rightSibling = getNextSibling(parent.getChildNodes(), n -> id.equals(n.getId()), true).orElse(null);
            rightMostSibling = getFurthestSibling(parent.getChildNodes(), n -> id.equals(n.getId()), true).orElse(null);
        }
        if (isParagraphWithChildren(node)) {
            firstChild = ((Paragraph)node).getChildNodes().get(0);
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
        if (node instanceof Topic) {
            Topic topic = (Topic) node;
            for (Content c : topic.getContents()) {
                if (c instanceof Image) {
                    FileUtils.copyFile(
                            OutlineUtils.getImgFile(imagesLocation, c.getId()),
                            new File(imgDir, c.getId().toString())
                    );
                }
            }
        }
        if (isParagraphWithChildren(node)) {
            Paragraph paragraph = (Paragraph) node;
            for (Node cn : paragraph.getChildNodes()) {
                export(paragraph, cn, dir, imgDir);
            }
        }
    }

    private boolean isParagraphWithChildren(Node node) {
        return (node instanceof Paragraph) && ((Paragraph)node).getHasChildren();
    }
}
