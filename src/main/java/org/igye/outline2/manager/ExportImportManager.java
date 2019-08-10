package org.igye.outline2.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.igye.outline2.dto.NodeDto;
import org.igye.outline2.dto.OptVal;
import org.igye.outline2.dto.TagDto;
import org.igye.outline2.exceptions.OutlineException;
import org.igye.outline2.pm.NodeClass;
import org.igye.outline2.pm.TagId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.igye.outline2.OutlineUtils.getImgFile;
import static org.igye.outline2.OutlineUtils.map;
import static org.igye.outline2.OutlineUtils.nullSafeGetter;
import static org.igye.outline2.OutlineUtils.nullSafeGetterWithDefault;
import static org.igye.outline2.OutlineUtils.setOf;

@Component
public class ExportImportManager {
    private static final Pattern IMAGE_ENTRY_PATTERN = Pattern.compile(
            "^.*images/[a-zA-Z0-9]{2}/([a-zA-Z0-9]{8}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{12})$");
    private static final Pattern NODES_ENTRY_PATTERN = Pattern.compile("^.*nodes\\.json$");
    private static final Set<TagId> IMAGE_TAG_IDS = setOf(TagId.IMG_ID, TagId.ICON);
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ImageManager imageManager;
    @Autowired
    private NodeManager nodeManager;
    @Autowired
    private NodeRepository nodeRepository;
    @Value("${tmp.dir}")
    private String tmpDir;
    @Value("${images.location}")
    private String imagesLocation;
    @Value("${export.dir}")
    private String exportDirPath;

    @Transactional
    public NodeDto importFromFile(MultipartFile file, UUID parentId) throws IOException {
        File dataFile = saveDataFile(file);
        Map<UUID, UUID> imageIdsMap = new HashMap<>();
        importImages(dataFile, imageIdsMap);
        UUID newNodeId = importNodes(dataFile, parentId, imageIdsMap);
        return nodeManager.getNode(newNodeId, 0, false);
    }

    @Transactional
    public void exportToFile(UUID nodeId) throws IOException {
        NodeDto root = nodeManager.getNode(nodeId, Integer.MAX_VALUE, false);
        root.setPath(null);
        String nodeName = nullSafeGetterWithDefault(
                root.getTagSingleValue(TagId.NAME),
                "Node without name"
        );
        String exportName = nodeName.replaceAll("[^a-zA-Z0-9-_\\.]", "_");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        String time = ZonedDateTime.now().format(formatter);
        File exportDir = new File(exportDirPath + "/" + exportName + "--" + time);
        exportDir.mkdirs();
        Set<UUID> images = new HashSet<>();
        FileUtils.writeStringToFile(
                new File(exportDir, "nodes.json"),
                objectMapper.writeValueAsString(root),
                StandardCharsets.UTF_8
        );

        File srcImagesLocationFile = new File(imagesLocation);
        File dstImagesLocationFile = new File(exportDir, "images");
        collectImages(root, setOf(TagId.ICON), images);
        images.forEach(imgId -> copyImage(srcImagesLocationFile, dstImagesLocationFile, imgId));
    }

    private void copyImage(File srcDir, File dstDir, UUID imageId) {
        File srcImgFile = getImgFile(srcDir.getAbsolutePath(), imageId);
        File dstImgFile = getImgFile(dstDir.getAbsolutePath(), imageId);
        try {
            FileUtils.copyFile(srcImgFile, dstImgFile);
        } catch (IOException e) {
            throw new OutlineException(e);
        }
    }

    private void collectImages(NodeDto nodeDto, Set<TagId> imageTagIds, Set<UUID> images) {
        imageTagIds.forEach(tagId ->
                images.addAll(map(nodeDto.getTagsValues(tagId), UUID::fromString))
        );

        nullSafeGetter(nodeDto.getChildNodes(), children -> {
            children.forEach(ch -> collectImages(ch, imageTagIds, images));
            return null;
        });
    }

    private UUID importNodes(File dataFile, UUID parentId, Map<UUID, UUID> imageIdsMap) throws IOException {
        return saveNodeToDatabase(extractNodes(dataFile), parentId, imageIdsMap);
    }

    private UUID saveNodeToDatabase(NodeDto nodeDto, UUID parentId, Map<UUID, UUID> imageIdsMap) {
        UUID newNodeId = null;
        if (!nodeDto.getClazz().equals(NodeClass.TOP_CONTAINER)) {
            nodeDto.setId(null);
            nodeDto.setParentId(new OptVal<>(parentId));
            updateImageRefs(nodeDto, IMAGE_TAG_IDS, imageIdsMap);
            newNodeId = nodeManager.patchNode(nodeDto);
            if (nodeDto.getCreatedWhen() != null) {
                nodeRepository.getOne(newNodeId).setCreatedWhen(nodeDto.getCreatedWhen());
            }
        } else {
            newNodeId = parentId;
        }
        UUID finalNewNodeId = newNodeId;
        nullSafeGetter(
                nodeDto.getChildNodes(),
                children -> {
                    children.forEach(ch -> saveNodeToDatabase(ch, finalNewNodeId, imageIdsMap));
                    return null;
                }
        );
        return newNodeId;
    }

    private void updateImageRefs(NodeDto nodeDto, Set<TagId> imageTagIds, Map<UUID, UUID> imageIdsMap) {
        for (TagId imageTagId : imageTagIds) {
            for (TagDto imageTag : nodeDto.getTags(imageTagId)) {
                String newImgId = imageIdsMap.get(imageTag.getValue()).toString();
                if (newImgId == null) {
                    throw new OutlineException("newImgId == null");
                }
                imageTag.setValue(OptVal.of(newImgId));
            }
        }
    }

    private void importImages(File dataFile, Map<UUID, UUID> imageIdsMap) throws IOException {
        try (ZipFile zipFile = new ZipFile(dataFile)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while(entries.hasMoreElements()){
                ZipEntry entry = entries.nextElement();
                UUID imgId = getImageId(entry);
                if (imgId != null) {
                    UUID newImgId = UUID.randomUUID();
                    imageIdsMap.put(imgId, newImgId);
                    File imgFile = getImgFile(imagesLocation, newImgId);
                    FileUtils.copyInputStreamToFile(zipFile.getInputStream(entry), imgFile);
                }
            }
        }
    }

    private NodeDto extractNodes(File dataFile) throws IOException {
        try (ZipFile zipFile = new ZipFile(dataFile)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while(entries.hasMoreElements()){
                ZipEntry entry = entries.nextElement();
                if (isNodesEntry(entry)) {
                    return objectMapper.readValue(zipFile.getInputStream(entry), NodeDto.class);
                }
            }
        }
        return null;
    }

    private UUID getImageId(ZipEntry entry) {
        Matcher matcher = IMAGE_ENTRY_PATTERN.matcher(entry.getName());
        if (matcher.matches()) {
            return UUID.fromString(matcher.group(1));
        } else {
            return null;
        }
    }

    private boolean isNodesEntry(ZipEntry entry) {
        return NODES_ENTRY_PATTERN.matcher(entry.getName()).matches();
    }

    private File saveDataFile(MultipartFile file) throws IOException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        String time = ZonedDateTime.now().format(formatter);
        File importDir = new File(tmpDir + "/import--" + time + "--" + file.getOriginalFilename());
        importDir.mkdirs();
        File dataFile = new File(importDir.getAbsolutePath() + "/" + file.getOriginalFilename());
        file.transferTo(dataFile);
        return dataFile;
    }
}
