package org.igye.outline.data;

import com.google.common.collect.ImmutableSet;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.igye.outline.common.OutlineUtils;
import org.igye.outline.data.repository.ContentRepository;
import org.igye.outline.data.repository.ImageRepository;
import org.igye.outline.data.repository.NodeRepository;
import org.igye.outline.data.repository.OldParagraphRepository;
import org.igye.outline.data.repository.OldUserRepository;
import org.igye.outline.data.repository.ParagraphRepository;
import org.igye.outline.data.repository.RoleRepository;
import org.igye.outline.data.repository.TopicRepository;
import org.igye.outline.data.repository.UserRepository;
import org.igye.outline.exceptions.OutlineException;
import org.igye.outline.htmlforms.ContentForForm;
import org.igye.outline.htmlforms.EditTopicForm;
import org.igye.outline.htmlforms.ReorderNodeChildren;
import org.igye.outline.htmlforms.SessionData;
import org.igye.outline.model.Content;
import org.igye.outline.model.Paragraph;
import org.igye.outline.model.SynopsisTopic;
import org.igye.outline.model.Text;
import org.igye.outline.model.User;
import org.igye.outline.modelv2.ContentV2;
import org.igye.outline.modelv2.ImageV2;
import org.igye.outline.modelv2.NodeV2;
import org.igye.outline.modelv2.ParagraphV2;
import org.igye.outline.modelv2.RoleV2;
import org.igye.outline.modelv2.TextV2;
import org.igye.outline.modelv2.TopicV2;
import org.igye.outline.modelv2.UserV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import static org.igye.outline.common.OutlineUtils.map;
import static org.igye.outline.common.OutlineUtils.mapToSet;
import static org.igye.outline.common.OutlineUtils.toMap;
import static org.igye.outline.htmlforms.ContentForForm.ContentTypeForForm.IMAGE;
import static org.igye.outline.htmlforms.ContentForForm.ContentTypeForForm.TEXT;

@Component
public class NodeDao {
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private SessionData sessionData;
    @Autowired
    private OldUserRepository oldUserRepository;
    @Autowired
    private OldParagraphRepository oldParagraphRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private NodeRepository nodeRepository;
    @Autowired
    private TopicRepository topicRepository;
    @Autowired
    private ParagraphRepository paragraphRepository;
    @Autowired
    private ImageRepository imageRepository;
    @Autowired
    private ContentRepository contentRepository;

    @Transactional
    public List<NodeV2> getRootNodes() {
        return nodeRepository.findByOwnerAndParentNodeIsNullOrderByName(sessionData.getCurrentUser());
    }

    @Transactional
    public ParagraphV2 getParagraphById(UUID id) {
        ParagraphV2 paragraph = paragraphRepository.findByOwnerAndId(sessionData.getCurrentUser(), id);
        paragraph.getChildNodes().forEach(ch -> {
            if (ch instanceof ParagraphV2) {
                Hibernate.initialize(((ParagraphV2)ch).getChildNodes());
            }
        });
        return paragraph;
    }

    @Transactional
    public ParagraphV2 createParagraph(UUID parentId, String name) {
        ParagraphV2 parent = paragraphRepository.findByOwnerAndId(sessionData.getCurrentUser(), parentId);
        ParagraphV2 paragraph = new ParagraphV2();
        paragraph.setName(name);
        parent.addChildNode(paragraph);
        return paragraph;
    }


    @Transactional
    public void updateParagraph(UUID id, Consumer<ParagraphV2> updates) {
        ParagraphV2 paragraph = paragraphRepository.findByOwnerAndId(sessionData.getCurrentUser(), id);
        updates.accept(paragraph);
    }

    @Transactional
    public TopicV2 getTopicById(UUID id) {
        TopicV2 topic = topicRepository.findByOwnerAndId(sessionData.getCurrentUser(), id);
        Hibernate.initialize(topic.getContents());
        return topic;
    }

    @Transactional
    public ImageV2 getImageById(UUID id) {
        ImageV2 image = imageRepository.findByOwnerAndId(sessionData.getCurrentUser(), id);
        return image;
    }

    @Transactional
    public UUID createTopic(EditTopicForm request) {
        TopicV2 topic = new TopicV2();
        if (request.getParentId() != null) {
            paragraphRepository.findByOwnerAndId(sessionData.getCurrentUser(), request.getParentId()).addChildNode(
                topic
            );
        } else {
            topic.setOwner(
                userRepository.findById(sessionData.getCurrentUser().getId()).get()
            );
        }
        topic.setName(request.getName());
        request.getContent().stream().forEach(contentForForm -> {
            if (TEXT.equals(contentForForm.getType())) {
                TextV2 text = new TextV2();
                text.setText(contentForForm.getText());
                topic.addContent(text);
            } else if (IMAGE.equals(contentForForm.getType())) {
                ImageV2 image = imageRepository.findByOwnerAndId(sessionData.getCurrentUser(), contentForForm.getId());
                if (image == null) {
                    throw new OutlineException("image == null for id = '" + contentForForm.getId() + "'");
                }
                topic.addContent(image);
            } else {
                throw new OutlineException("Unexpected type of content - '" + contentForForm.getType() + "'");
            }
        });
        return topicRepository.save(topic).getId();
    }

    @Transactional
    public void updateTopic(EditTopicForm form) {
        TopicV2 topic = topicRepository.findByOwnerAndId(sessionData.getCurrentUser(), form.getId());
        topic.setName(form.getName());
        Map<UUID, ContentV2> oldContents = toMap(topic.getContents(), ContentV2::getId);
        oldContents.values().forEach(topic::detachContentById);
        for (ContentForForm content : form.getContent()) {
            if (TEXT.equals(content.getType())) {
                if (content.getId() != null) {
                    TextV2 text = (TextV2) oldContents.remove(content.getId());
                    text.setText(content.getText());
                    topic.addContent(text);
                } else {
                    TextV2 text = new TextV2();
                    text.setText(content.getText());
                    topic.addContent(text);
                }
            } else if (IMAGE.equals(content.getType())) {
                if (content.getId() != null) {
                    UUID imgId = content.getId();
                    ContentV2 oldImg = oldContents.remove(imgId);
                    topic.addContent(
                            oldImg != null
                                    ? oldImg
                                    : imageRepository.findByOwnerAndId(sessionData.getCurrentUser(), imgId)
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
        ImageV2 img = new ImageV2();
        img.setOwner(userRepository.findById(sessionData.getCurrentUser().getId()).get());
        return imageRepository.save(img).getId();
    }


    @Transactional
    public void migrateData() {
        Session session = OutlineUtils.getCurrentSession(entityManager);
        Map<String, RoleV2> rolesMap = toMap(roleRepository.findAll(), RoleV2::getName);

        List<UserV2> newUsers = userRepository.findAll();
        List<User> oldUsersToBeMigrated = oldUserRepository.findAllByIdNotIn(map(newUsers, UserV2::getId));
        oldUsersToBeMigrated.forEach(ou -> session.merge(
                UserV2.builder()
                .id(ou.getId())
                .name(ou.getName())
                .password(ou.getPassword())
                .roles(map(ou.getRoles(), r -> rolesMap.get(r.getName())))
                .build()
        ));

        Map<String, UserV2> usersMap = toMap(userRepository.findAll(), UserV2::getName);

        List<Paragraph> rootParagraphs = oldParagraphRepository.findByParentParagraphIsNull();
        map(rootParagraphs, p -> toNode(p, null, usersMap))
                .forEach(session::merge);
    }

    @Transactional
    public void reorderNodeChildren(ReorderNodeChildren request) {
        ParagraphV2 parent = paragraphRepository.findByOwnerAndId(sessionData.getCurrentUser(), request.getParentId());
        List<NodeV2> children = parent.getChildNodes();
        Set<UUID> oldIdSet = mapToSet(children, NodeV2::getId);
        Set<UUID> newIdSet = ImmutableSet.copyOf(request.getChildren());
        if (!oldIdSet.equals(newIdSet)) {
            throw new OutlineException("!oldIdSet.equals(newIdSet)");
        }
        Map<UUID, NodeV2> childrenMap = toMap(children, NodeV2::getId);
        parent.getChildNodes().clear();
        request.getChildren().forEach(id -> parent.addChildNode(childrenMap.get(id)));
    }

    private NodeV2 toNode(Paragraph oldParagraph, ParagraphV2 parentParagraph, Map<String, UserV2> usersMap) {
        ParagraphV2 newParagraph = new ParagraphV2();
        newParagraph.setId(oldParagraph.getId());
        if (parentParagraph != null) {
            parentParagraph.addChildNode(newParagraph);
        } else {
            newParagraph.setOwner(usersMap.get(oldParagraph.getOwner().getName()));
        }
        newParagraph.setName(oldParagraph.getName());
        map(oldParagraph.getChildParagraphs(), p -> toNode(p, newParagraph, usersMap));
        map(oldParagraph.getTopics(), t -> toNode((SynopsisTopic) t, newParagraph));
        return newParagraph;
    }

    private NodeV2 toNode(SynopsisTopic oldTopic, ParagraphV2 parentParagraph) {
        TopicV2 newTopic = new TopicV2();
        newTopic.setId(oldTopic.getId());
        parentParagraph.addChildNode(newTopic);
        newTopic.setName(oldTopic.getName());
        newTopic.setContents(map(oldTopic.getContents(), c -> toContentV2(c, newTopic)));
        return newTopic;
    }

    private ContentV2 toContentV2(Content content, TopicV2 parentTopic) {
        if (content instanceof Text) {
            TextV2 res = new TextV2();
            res.setId(content.getId());
            res.setText(((Text)content).getText());
            parentTopic.addContent(res);
            return res;
        } else {
            ImageV2 res = new ImageV2();
            res.setId(content.getId());
            parentTopic.addContent(res);
            return res;
        }
    }
}
