package org.igye.outline.data;

import org.hibernate.Session;
import org.igye.outline.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class TestDataBuilder {
    private Session session;
    private User currentUser;
    private Paragraph parentParagraph;
    private Object currentObject;
    private Map<String, Object> results = new HashMap<>();

    public TestDataBuilder(Session session) {
        this.session = session;
    }

    public TestDataBuilder(Session session, Object currentObject, User currentUser, Paragraph parentParagraph) {
        this.session = session;
        this.currentObject = currentObject;
        this.currentUser = currentUser;
        this.parentParagraph = parentParagraph;
    }

    public TestDataBuilder user(String name) {
        User user = new User();
        user.setName(name);
        user.setPassword("p");
        session.persist(user);
        currentUser = user;
        currentObject = user;
        return this;
    }

    public TestDataBuilder role(String roleName) {
        List<Role> roles = session.createQuery("from Role where name = :name", Role.class)
                .setParameter("name", roleName)
                .getResultList();
        Role role = null;
        if (roles.isEmpty()) {
            role = new Role();
            role.setName(roleName);
            session.persist(role);
        } else if (roles.size() > 1) {
            throw new TestDataBuilderException("roles.size() > 1");
        } else {
            role = roles.get(0);
        }
        currentUser.getRoles().add(role);
        return this;
    }

    public TestDataBuilder paragraph(String name) {
        Paragraph paragraph = new Paragraph();
        paragraph.setName(name);
        if (parentParagraph != null) {
            parentParagraph.addChildParagraph(paragraph);
        } else {
            paragraph.setOwner(currentUser);
            session.persist(paragraph);
        }
        currentObject = paragraph;
        return this;
    }

    public TestDataBuilder topic(String name) {
        Topic topic = new Topic();
        topic.setName(name);
        if (parentParagraph != null) {
            parentParagraph.addTopic(topic);
        } else {
            throw new TestDataBuilderException("Topic '" + name + "' can't be outside of a paragraph");
        }
        currentObject = topic;
        return this;
    }

    public TestDataBuilder tag(String tagStr) {
        List<Tag> tags = session.createQuery("from Tag where name = :name", Tag.class)
                .setParameter("name", tagStr)
                .getResultList();
        Tag tag = null;
        if (tags.isEmpty()) {
            tag = new Tag();
            tag.setName(tagStr);
            session.persist(tag);
        } else if (tags.size() > 1) {
            throw new TestDataBuilderException("tags.size() > 1");
        } else {
            tag = tags.get(0);
        }
        Tag finalTag = tag;

        processForCurrentObject(
                user -> null,
                paragraph -> paragraph.getTags().add(finalTag),
                topic -> topic.getTags().add(finalTag)
        );
        return this;
    }

    private <T> T  processForCurrentObject(Function<User, T> userConsumer,
                                           Function<Paragraph, T> paragraphConsumer,
                                           Function<Topic, T> topicConsumer) {
        if (currentObject instanceof User) {
            return userConsumer.apply((User) currentObject);
        } else if (currentObject instanceof Paragraph) {
            return paragraphConsumer.apply((Paragraph) currentObject);
        } else if (currentObject instanceof Topic) {
            return topicConsumer.apply((Topic) currentObject);
        } else {
            throw new TestDataBuilderException("Unexpected type of currentObject: " + currentObject);
        }
    }

    public TestDataBuilder children(Function<TestDataBuilder, TestDataBuilder> childrenBuilder) {
        TestDataBuilder newBuilder = processForCurrentObject(
                user -> new TestDataBuilder(session, currentObject, currentUser, parentParagraph),
                paragraph -> new TestDataBuilder(session, currentObject, currentUser, (Paragraph) currentObject),
                topic -> new TestDataBuilder(session, currentObject, currentUser, parentParagraph)
        );
        results.putAll(childrenBuilder.apply(newBuilder).getResults());
        return this;
    }

    public TestDataBuilder save(String name) {
        results.put(name, currentObject);
        return this;
    }

    public Map<String, Object> getResults() {
        return results;
    }
}
