package org.igye.outline.data;

import org.hibernate.Session;
import org.igye.outline.exceptions.OutlineException;
import org.igye.outline.modelv2.ImageV2;
import org.igye.outline.modelv2.ParagraphV2;
import org.igye.outline.modelv2.RoleV2;
import org.igye.outline.modelv2.TopicV2;
import org.igye.outline.modelv2.UserV2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.igye.outline.data.UserDao.ADMIN_ROLE_NAME;

public class TestDataBuilderV2 {
    public static final String CURRENT_USER = "CURRENT_USER";

    private final Session session;

    private UserV2 parentUser;
    private ParagraphV2 parentParagraph;
    private TopicV2 parentTopic;

    private Object currentObject;

    private Map<String, Object> results = new HashMap<>();

    public TestDataBuilderV2(Session session) {
        this.session = session;
    }

    public TestDataBuilderV2(TestDataBuilderV2 parent) {
        this.session = parent.session;
        parentUser = parent.parentUser;
        parentParagraph = parent.parentParagraph;
        parentTopic = parent.parentTopic;
    }

    public TestDataBuilderV2 user(String name) {
        UserV2 user = new UserV2();
        user.setName(name);
        user.setPassword("p");
        session.persist(user);
        parentUser = user;
        currentObject = user;
        return this;
    }

    public TestDataBuilderV2 admin() {
        return role(ADMIN_ROLE_NAME);
    }

    public TestDataBuilderV2 role(String roleName) {
        if (!(currentObject instanceof UserV2)) {
            throw new OutlineException("!(currentObject instanceof UserV2)");
        }
        List<RoleV2> roles = session.createQuery("from RoleV2 where name = :name", RoleV2.class)
                .setParameter("name", roleName)
                .getResultList();
        RoleV2 role = null;
        if (roles.isEmpty()) {
            role = new RoleV2();
            role.setName(roleName);
            session.persist(role);
        } else if (roles.size() > 1) {
            throw new TestDataBuilderException("roles.size() > 1");
        } else {
            role = roles.get(0);
        }
        ((UserV2)currentObject).getRoles().add(role);
        return this;
    }

    public TestDataBuilderV2 paragraph(String name) {
        ParagraphV2 paragraph = new ParagraphV2();
        paragraph.setName(name);
        if (parentParagraph != null) {
            parentParagraph.addChildNode(paragraph);
        } else {
            paragraph.setOwner(parentUser);
            session.persist(paragraph);
        }
        currentObject = paragraph;
        return this;
    }

    public TestDataBuilderV2 topic(String name) {
        TopicV2 topic = new TopicV2();
        topic.setName(name);
        if (parentParagraph != null) {
            parentParagraph.addChildNode(topic);
        } else {
            throw new TestDataBuilderException("Topic '" + name + "' can't be outside of a paragraph");
        }
        currentObject = topic;
        return this;
    }

    public TestDataBuilderV2 image() {
        ImageV2 image = new ImageV2();
        parentTopic.addContent(image);
        currentObject = image;
        return this;
    }

    public TestDataBuilderV2 currentUser() {
        if (!(currentObject instanceof UserV2)) {
            throw new OutlineException("!(currentObject instanceof UserV2)");
        }
        return save(CURRENT_USER, currentObject);
    }

    public TestDataBuilderV2 children(Function<TestDataBuilderV2, TestDataBuilderV2> childrenBuilder) {
        TestDataBuilderV2 newBuilder = processForCurrentObject(
                user -> new TestDataBuilderV2(this).setParentUser(user),
                paragraph -> new TestDataBuilderV2(this).setParentParagraph(paragraph),
                topic -> new TestDataBuilderV2(this).setParentTopic(topic),
                image -> {
                    throw new OutlineException("Image cannot have children.");
                }
        );
        results.putAll(childrenBuilder.apply(newBuilder).getResults());
        return this;
    }

    public TestDataBuilderV2 save(String name) {
        return save(name, currentObject);
    }

    private TestDataBuilderV2 save(String name, Object object) {
        if (results.containsKey(name)) {
            throw new OutlineException("Duplicated key.");
        }
        results.put(name, object);
        return this;
    }

    public Map<String, Object> getResults() {
        return results;
    }

    public TestDataBuilderV2 setCurrentObject(Object currentObject) {
        this.currentObject = currentObject;
        return this;
    }

    public TestDataBuilderV2 setParentUser(UserV2 parentUser) {
        this.parentUser = parentUser;
        return this;
    }

    public TestDataBuilderV2 setParentParagraph(ParagraphV2 parentParagraph) {
        this.parentParagraph = parentParagraph;
        return this;
    }

    public TestDataBuilderV2 setParentTopic(TopicV2 parentTopic) {
        this.parentTopic = parentTopic;
        return this;
    }

    private <T> T  processForCurrentObject(Function<UserV2, T> userConsumer,
                                           Function<ParagraphV2, T> paragraphConsumer,
                                           Function<TopicV2, T> topicConsumer,
                                           Function<ImageV2, T> imageConsumer) {
        if (currentObject instanceof UserV2) {
            return userConsumer.apply((UserV2) currentObject);
        } else if (currentObject instanceof ParagraphV2) {
            return paragraphConsumer.apply((ParagraphV2) currentObject);
        } else if (currentObject instanceof TopicV2) {
            return topicConsumer.apply((TopicV2) currentObject);
        } else if (currentObject instanceof ImageV2) {
            return imageConsumer.apply((ImageV2) currentObject);
        } else {
            throw new TestDataBuilderException("Unexpected type of currentObject: " + currentObject);
        }
    }
}
