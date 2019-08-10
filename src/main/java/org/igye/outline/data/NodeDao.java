package org.igye.outline.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import fj.F2;
import org.apache.commons.io.FileUtils;
import org.hibernate.Hibernate;
import org.igye.outline.common.OutlineUtils;
import org.igye.outline.data.repository.ContentRepository;
import org.igye.outline.data.repository.IconRepository;
import org.igye.outline.data.repository.ImageRepository;
import org.igye.outline.data.repository.NodeRepository;
import org.igye.outline.data.repository.ParagraphRepository;
import org.igye.outline.data.repository.TopicRepository;
import org.igye.outline.data.repository.UserRepository;
import org.igye.outline.dto.NodeClass;
import org.igye.outline.dto.NodeDto;
import org.igye.outline.dto.TagId;
import org.igye.outline.dto.TagValueDto;
import org.igye.outline.exceptions.OutlineException;
import org.igye.outline.htmlforms.ContentForForm;
import org.igye.outline.htmlforms.EditParagraphForm;
import org.igye.outline.htmlforms.EditTopicForm;
import org.igye.outline.htmlforms.ReorderNodeChildren;
import org.igye.outline.htmlforms.SessionData;
import org.igye.outline.model.Content;
import org.igye.outline.model.Icon;
import org.igye.outline.model.Image;
import org.igye.outline.model.Node;
import org.igye.outline.model.Paragraph;
import org.igye.outline.model.Text;
import org.igye.outline.model.Topic;
import org.igye.outline.model.User;
import org.igye.outline.selection.ActionType;
import org.igye.outline.selection.ObjectType;
import org.igye.outline.selection.Selection;
import org.igye.outline.selection.SelectionPart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.igye.outline.common.OutlineUtils.getImgFile;
import static org.igye.outline.common.OutlineUtils.mapToSet;
import static org.igye.outline.common.OutlineUtils.nullSafeGetter;
import static org.igye.outline.common.OutlineUtils.toMap;
import static org.igye.outline.htmlforms.ContentForForm.ContentTypeForForm.IMAGE;
import static org.igye.outline.htmlforms.ContentForForm.ContentTypeForForm.TEXT;

@Component
public class NodeDao {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private SessionData sessionData;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private NodeRepository nodeRepository;
    @Autowired
    private TopicRepository topicRepository;
    @Autowired
    private ParagraphRepository paragraphRepository;
    @Autowired
    private ImageRepository imageRepository;
    @Autowired
    private IconRepository iconRepository;
    @Autowired
    private ContentRepository contentRepository;
    @Autowired
    private WordsDao wordsDao;
    @PersistenceContext
    private EntityManager entityManager;
    @Value("${h2.version}")
    private String h2Version;
    @Value("${backup.dir}")
    private String backupDirPath;
    @Value("${export.dir}")
    private String exportDirPath;
    @Value("${topic.images.location}")
    private String imagesLocation;

    @Transactional
    public List<Node> getRootNodes() {
        return nodeRepository.findByOwnerAndParentNodeIsNullOrderByName(sessionData.getCurrentUser());
    }

    @Transactional
    public Paragraph getParagraphById(UUID id) {
        Paragraph paragraph = paragraphRepository.findByOwnerAndId(sessionData.getCurrentUser(), id);
        if (paragraph == null) {
            throw new OutlineException("Paragraph with id " + id + " was not found.");
        }
        paragraph.getChildNodes().forEach(ch -> {
            if (ch instanceof Paragraph) {
                Hibernate.initialize(((Paragraph)ch).getChildNodes());
            }
        });
        return paragraph;
    }

    @Transactional
    public Paragraph getParagraphSummary(UUID id) {
        Paragraph paragraph = paragraphRepository.findByOwnerAndId(sessionData.getCurrentUser(), id);
        if (paragraph == null) {
            throw new OutlineException("Paragraph with id " + id + " was not found.");
        }
        return paragraph;
    }

    @Transactional
    public Paragraph createParagraph(UUID parentId, EditParagraphForm form) {
        User currUser = sessionData.getCurrentUser();
        Paragraph paragraph = new Paragraph();
        paragraph.setName(form.getName());
        paragraph.setIcon(iconRepository.findByOwnerAndId(currUser, form.getIconId()));
        paragraph.setSol(form.isSol());
        paragraph.setOwner(currUser);
        saveNode(parentId, paragraph, node -> paragraphRepository.save((Paragraph) node));
        return paragraph;
    }


    @Transactional
    public void updateParagraph(EditParagraphForm form) {
        User currentUser = sessionData.getCurrentUser();
        Paragraph paragraph = paragraphRepository.findByOwnerAndId(currentUser, form.getId());
        paragraph.setName(form.getName());
        paragraph.setSol(form.isSol());
        paragraph.setIcon(iconRepository.findByOwnerAndId(currentUser, form.getIconId()));
    }

    @Transactional
    public Topic getTopicById(UUID id) {
        Topic topic = topicRepository.findByOwnerAndId(sessionData.getCurrentUser(), id);
        if (topic == null) {
            throw new OutlineException("Topic with id " + id + " was not found.");
        }
        Hibernate.initialize(topic.getContents());
        return topic;
    }

    @Transactional
    public Image getImageById(UUID id) {
        return imageRepository.findByOwnerAndId(sessionData.getCurrentUser(), id);
    }

    @Transactional
    public Icon getIconById(UUID id) {
        return iconRepository.findByOwnerAndId(sessionData.getCurrentUser(), id);
    }

    @Transactional
    public UUID createTopic(EditTopicForm request) {
        Topic topic = new Topic();
        User currentUser = sessionData.getCurrentUser();
        topic.setOwner(currentUser);
        saveNode(request.getParentId(), topic, node -> topicRepository.save((Topic) node));
        topic.setName(request.getName());
        topic.setIcon(iconRepository.findByOwnerAndId(currentUser, request.getIconId()));
        topic.setSol(request.isSol());
        final Topic finalTopic = topic;
        request.getContent().stream().forEach(contentForForm -> {
            if (TEXT.equals(contentForForm.getType())) {
                Text text = new Text();
                text.setText(contentForForm.getText());
                finalTopic.addContent(text);
            } else if (IMAGE.equals(contentForForm.getType())) {
                Image image = imageRepository.findByOwnerAndId(currentUser, contentForForm.getId());
                if (image == null) {
                    throw new OutlineException("image == null for id = '" + contentForForm.getId() + "'");
                }
                finalTopic.addContent(image);
            } else {
                throw new OutlineException("Unexpected type of content - '" + contentForForm.getType() + "'");
            }
        });
        return finalTopic.getId();
    }

    @Transactional
    public void updateTopic(EditTopicForm form) {
        User currentUser = sessionData.getCurrentUser();
        Topic topic = topicRepository.findByOwnerAndId(currentUser, form.getId());
        topic.setName(form.getName());
        topic.setIcon(iconRepository.findByOwnerAndId(currentUser, form.getIconId()));
        topic.setSol(form.isSol());
        Map<UUID, Content> oldContents = toMap(topic.getContents(), Content::getId);
        oldContents.values().forEach(topic::detachContentById);
        for (ContentForForm content : form.getContent()) {
            if (TEXT.equals(content.getType())) {
                if (content.getId() != null) {
                    Text text = (Text) oldContents.remove(content.getId());
                    text.setText(content.getText());
                    topic.addContent(text);
                } else {
                    Text text = new Text();
                    text.setText(content.getText());
                    topic.addContent(text);
                }
            } else if (IMAGE.equals(content.getType())) {
                if (content.getId() != null) {
                    UUID imgId = content.getId();
                    Content oldImg = oldContents.remove(imgId);
                    topic.addContent(
                            oldImg != null
                                    ? oldImg
                                    : imageRepository.findByOwnerAndId(currentUser, imgId)
                    );
                } else {
                    throw new OutlineException("Unexpected condition:  image.getId() == null");
                }
            } else {
                throw new OutlineException("Unexpected type of content - '" + content.getType() + "'");
            }
        }
        oldContents.values().forEach(contentRepository::delete);
    }

    @Transactional
    public UUID createImage() {
        Image img = new Image();
        img.setOwner(userRepository.findById(sessionData.getCurrentUser().getId()).get());
        return imageRepository.save(img).getId();
    }

    @Transactional
    public UUID createIcon() {
        Icon icon = new Icon();
        icon.setOwner(userRepository.findById(sessionData.getCurrentUser().getId()).get());
        return iconRepository.save(icon).getId();
    }


    @Transactional
    public void reorderNodeChildren(ReorderNodeChildren request) {
        Paragraph parent = paragraphRepository.findByOwnerAndId(sessionData.getCurrentUser(), request.getParentId());
        List<Node> children = parent.getChildNodes();
        Set<UUID> oldIdSet = mapToSet(children, Node::getId);
        Set<UUID> newIdSet = ImmutableSet.copyOf(request.getChildren());
        if (!oldIdSet.equals(newIdSet)) {
            throw new OutlineException("!oldIdSet.equals(newIdSet)");
        }
        Map<UUID, Node> childrenMap = toMap(children, Node::getId);
        parent.getChildNodes().clear();
        request.getChildren().forEach(id -> parent.addChildNode(childrenMap.get(id)));
    }

    @Transactional
    public void performActionOnSelectedObjects(Selection selection, UUID destId) {
        if (ActionType.MOVE.equals(selection.getActionType())) {
            for (SelectionPart selectionPart : selection.getSelections()) {
                if (ObjectType.PARAGRAPH.equals(selectionPart.getObjectType())) {
                    moveParagraph(selectionPart.getSelectedId(), destId);
                } else if (ObjectType.TOPIC.equals(selectionPart.getObjectType())) {
                    moveNode(selectionPart.getSelectedId(), destId);
                } else if (ObjectType.IMAGE.equals(selectionPart.getObjectType())) {
                    moveImage(selectionPart.getSelectedId(), destId);
                } else if (ObjectType.ENG_TEXT.equals(selectionPart.getObjectType())) {
                    moveNode(selectionPart.getSelectedId(), destId);
                } else if (ObjectType.ENG_WORD.equals(selectionPart.getObjectType())) {
                    wordsDao.moveWord(selectionPart.getSelectedId(), destId);
                } else {
                    throw new OutlineException("Object type '" + selectionPart.getObjectType() + "' is not supported.");
                }
            }
        } else {
            throw new OutlineException("Action '" + selection.getActionType() + "' is not supported.");
        }

    }

    @Transactional
    public Node loadNodeById(UUID id) {
        return nodeRepository.findByOwnerAndId(sessionData.getCurrentUser(), id);
    }

    @Transactional
    public Optional<?> nextSibling(UUID id, boolean toTheRight) {
        return getSibling(id, (list, comp) -> OutlineUtils.getNextSibling(list, comp, toTheRight));
    }

    @Transactional
    public Optional<?> furthestSibling(UUID id, boolean toTheRight) {
        return getSibling(id, (list, comp) -> OutlineUtils.getFurthestSibling(list, comp, toTheRight));
    }

    @Transactional
    public Optional<?> firstChild(Optional<UUID> id) {
        if (id.isPresent()) {
            Paragraph paragraph = paragraphRepository.findByOwnerAndId(sessionData.getCurrentUser(), id.get());
            if (!paragraph.getChildNodes().isEmpty()) {
                return Optional.of(paragraph.getChildNodes().get(0));
            } else {
                return Optional.empty();
            }
        } else {
            List<Node> rootNodes = getRootNodes();
            if (rootNodes.isEmpty()) {
                return Optional.empty();
            } else {
                return Optional.of(rootNodes.get(0));
            }
        }

    }

    @Transactional
    public Optional<Paragraph> loadParent(UUID id) {
        return Optional.ofNullable((Paragraph) loadNodeById(id).getParentNode());
    }

    @Transactional
    public void saveNode(UUID parentId, Node node, Consumer<Node> saver) {
        if (parentId == null) {
            saver.accept(node);
        } else {
            Paragraph parent = getParagraphSummary(parentId);
            if (parent != null) {
                parent.addChildNode(node);
            } else {
                saver.accept(node);
            }
        }
    }

    @Transactional
    public void backup() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        String time = ZonedDateTime.now().format(formatter);
        File backupDir = new File(backupDirPath + "/" + time);
        backupDir.mkdirs();
        OutlineUtils.getCurrentSession(entityManager).doWork(connection -> {
            connection
                    .prepareStatement(
                            "BACKUP TO '" + backupDir.getAbsolutePath() + "/outline-db-" + h2Version + ".zip'"
                    )
                    .executeUpdate();
        });
    }

    @Transactional
    public void export(UUID paragraphId) throws IOException {
        Paragraph root = paragraphRepository.findById(paragraphId).get();
        String exportName = root.getName().replaceAll("[^a-zA-Z0-9-_\\.]", "_");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        String time = ZonedDateTime.now().format(formatter);
        File exportZipFile = new File(exportDirPath + "/" + exportName + "--" + time + ".zip");
        exportZipFile.getParentFile().mkdirs();
        Set<UUID> images = new HashSet<>();

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(exportZipFile))) {
            ZipEntry nodesEntry = new ZipEntry("nodes.json");
            zos.putNextEntry(nodesEntry);
            byte[] nodesBytes = objectMapper.writeValueAsString(convertToDto(root, images)).getBytes(StandardCharsets.UTF_8);
            zos.write(nodesBytes, 0, nodesBytes.length);
            zos.closeEntry();

            File srcImagesLocationFile = new File(imagesLocation);
            images.forEach(imgId -> copyImage(srcImagesLocationFile, zos, imgId));
        }
    }

    private void copyImage(File srcDir, ZipOutputStream zos, UUID imageId) {
        try {
            File srcImgFile = getImgFile(srcDir.getAbsolutePath(), imageId);
            ZipEntry imageEntry = new ZipEntry("images/" + imageId.toString().substring(0,2) + "/" + imageId);
            zos.putNextEntry(imageEntry);
            byte[] imageBytes = FileUtils.readFileToByteArray(srcImgFile);
            zos.write(imageBytes, 0, imageBytes.length);
            zos.closeEntry();
        } catch (IOException e) {
            throw new OutlineException(e);
        }
    }

    private NodeDto convertToDto(Object node, Set<UUID> images) {
        NodeDto nodeDto = new NodeDto();
        nodeDto.setId(getId(node));
        nodeDto.setClazz(getObjectClass(node));
        if (nodeDto.getClazz().equals(NodeClass.CONTAINER)) {
            nodeDto.getTags().put(TagId.NAME, Arrays.asList(
                    TagValueDto.builder().value(((Node) node).getName()).build()
            ));
            final UUID icon = getIcon(node);
            if (icon!=null) {
                nodeDto.getTags().put(TagId.ICON, Arrays.asList(
                        TagValueDto.builder().ref(icon).build()
                ));
                images.add(icon);
            }
        } else if (nodeDto.getClazz().equals(NodeClass.IMAGE)) {
            final UUID imgId = ((Image) node).getId();
            nodeDto.getTags().put(TagId.IMG_ID, Arrays.asList(
                    TagValueDto.builder().ref(imgId).build()
            ));
            images.add(imgId);
        } else if (nodeDto.getClazz().equals(NodeClass.TEXT)) {
            nodeDto.getTags().put(TagId.TEXT, Arrays.asList(
                    TagValueDto.builder().value(((Text) node).getText()).build()
            ));
        }

        nodeDto.setChildNodes(getChildren(node, images));
        return nodeDto;
    }

    private List<NodeDto> getChildren(Object node, Set<UUID> images) {
        List<NodeDto> result = new ArrayList<>();
        if (node instanceof Paragraph) {
            for (Node childNode : ((Paragraph) node).getChildNodes()) {
                result.add(convertToDto(childNode, images));
            }
        } else if (node instanceof Topic) {
            for (Content childNode : ((Topic) node).getContents()) {
                result.add(convertToDto(childNode, images));
            }
        }
        return result;
    }

    private UUID getIcon(Object node) {
        if (node instanceof Paragraph) {
            return nullSafeGetter(((Paragraph) node).getIcon(), i->i.getId());
        } else if (node instanceof Topic) {
            return nullSafeGetter(((Topic) node).getIcon(), i->i.getId());
        }
        return null;
    }

    private NodeClass getObjectClass(Object node) {
        if (node instanceof Paragraph || node instanceof Topic) {
            return NodeClass.CONTAINER;
        } else if (node instanceof Image) {
            return NodeClass.IMAGE;
        } else if (node instanceof Text) {
            return NodeClass.TEXT;
        }
        throw new OutlineException("Can't determine object class.");
    }

    private UUID getId(Object node) {
        if (node instanceof Node) {
            return ((Node) node).getId();
        } else if (node instanceof Content) {
            return ((Content) node).getId();
        }
        throw new OutlineException("Cant determine id.");
    }

    @Transactional
    public Node loadAllNodesRecursively(UUID rootId) {
        Node root = nodeRepository.findByOwnerAndId(sessionData.getCurrentUser(), rootId);
        Queue<Node> queue = new ArrayDeque<>();
        queue.addAll(((Paragraph)root).getChildNodes());
        while (!queue.isEmpty()) {
            Node currNode = queue.remove();
            if (currNode instanceof Paragraph) {
                queue.addAll(((Paragraph)currNode).getChildNodes());
            }
        }
        return root;
    }

    private <T> Optional<T> getSibling(UUID id,
                                       F2<List<T>, Function<T,Boolean>, Optional<T>> getter) {
        Node node = loadNodeById(id);
        List<Node> siblings;
        if (node.getParentNode() == null) {
            siblings = getRootNodes();
        } else {
            siblings = ((Paragraph)node.getParentNode()).getChildNodes();
        }
        return getter.f(
                (List<T>) siblings,
                sib -> ((Node)sib).getId().equals(node.getId())
        );

    }

    private void moveParagraph(UUID parToMoveId, UUID parToMoveToId) {
        Paragraph parToMove = paragraphRepository.findByOwnerAndId(sessionData.getCurrentUser(), parToMoveId);
        if (parToMoveToId != null) {
            Paragraph parToMoveTo = paragraphRepository.findByOwnerAndId(sessionData.getCurrentUser(), parToMoveToId);
            Set<UUID> pathToRoot = new HashSet<>();
            Node currNode = parToMoveTo;
            while(currNode != null) {
                pathToRoot.add(currNode.getId());
                currNode = currNode.getParentNode();
            }
            if (pathToRoot.contains(parToMove.getId())) {
                throw new OutlineException("pathToRoot.contains(parToMove.getId())");
            }
            if (parToMove.getParentNode() != null) {
                ((Paragraph)parToMove.getParentNode()).removeChildNodeById(parToMove.getId());
            }
            parToMoveTo.addChildNode(parToMove);
        } else if (parToMove.getParentNode() != null) {
            ((Paragraph)parToMove.getParentNode()).removeChildNodeById(parToMove.getId());
        }

    }

    private void moveNode(UUID topicToMoveId, UUID parToMoveToId) {
        Node nodeToMove = nodeRepository.findByOwnerAndId(sessionData.getCurrentUser(), topicToMoveId);
        if (parToMoveToId != null) {
            Paragraph parToMoveTo = paragraphRepository.findByOwnerAndId(sessionData.getCurrentUser(), parToMoveToId);
            if (nodeToMove.getParentNode() != null) {
                ((Paragraph)nodeToMove.getParentNode()).removeChildNodeById(nodeToMove.getId());
            }
            parToMoveTo.addChildNode(nodeToMove);
        } else if (nodeToMove.getParentNode() != null) {
            ((Paragraph)nodeToMove.getParentNode()).removeChildNodeById(nodeToMove.getId());
        }
    }

    private void moveImage(UUID imageToMoveId, UUID topicToMoveToId) {
        Image imgToMove = imageRepository.findByOwnerAndId(sessionData.getCurrentUser(), imageToMoveId);
        Topic srcTopic = imgToMove.getTopic();
        Topic dstTopic = topicRepository.findByOwnerAndId(sessionData.getCurrentUser(), topicToMoveToId);
        srcTopic.detachContentById(imgToMove);
        dstTopic.addContent(imgToMove);
    }
}
