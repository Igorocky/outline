package org.igye.outline.data;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.igye.outline.common.OutlineUtils;
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
import java.util.UUID;

import static org.igye.outline.common.OutlineUtils.map;
import static org.igye.outline.common.OutlineUtils.toMap;

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

    @Transactional
    public List<NodeV2> getRootNodes() {
        return nodeRepository.findByOwnerAndParentNodeIsNullOrderByName(sessionData.getCurrentUser());
    }

    @Transactional
    public ParagraphV2 getParagraphById(UUID id) {
        ParagraphV2 paragraph = (ParagraphV2) nodeRepository.findById(id).get();
        paragraph.getChildNodes().forEach(ch -> {
            if (ch instanceof ParagraphV2) {
                Hibernate.initialize(((ParagraphV2)ch).getChildNodes());
            }
        });
        return paragraph;
    }

    @Transactional
    public TopicV2 getTopicById(UUID id) {
        TopicV2 topic = (TopicV2) nodeRepository.findById(id).get();
        Hibernate.initialize(topic.getContents());
        return topic;
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
