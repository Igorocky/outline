package org.igye.outline.data;

import org.hibernate.Session;
import org.igye.outline.model.Paragraph;
import org.igye.outline.model.Tag;
import org.igye.outline.model.Topic;
import org.igye.outline.model.User;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class TestDataBuilder {
    private Session session;
    private User currentUser;
    private Paragraph parentParagraph;
    private Object currentObject;
    private Set<Tag> tags = new HashSet<>();
    private List<Object> results = new ArrayList<>();

    public TestDataBuilder(Session session) {
        this.session = session;
        session.createQuery("delete from Topic").executeUpdate();
        session.createQuery("delete from Paragraph").executeUpdate();
        session.createQuery("delete from User").executeUpdate();
        session.createQuery("delete from Tag").executeUpdate();
    }

    public TestDataBuilder(Session session, Object currentObject, User currentUser, Paragraph parentParagraph, Set<Tag> tags) {
        this.session = session;
        this.currentObject = currentObject;
        this.currentUser = currentUser;
        this.parentParagraph = parentParagraph;
        this.tags = tags;
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
        Tag tag = tags.stream().filter(t -> t.getName().equals(tagStr)).findFirst().orElseGet(() -> {
            Tag t = new Tag();
            t.setName(tagStr);
            session.persist(t);
            return t;
        });
        processForCurrentObject(
                user -> null,
                paragraph -> paragraph.getTags().add(tag),
                topic -> topic.getTags().add(tag)
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
                user -> new TestDataBuilder(session, currentObject, currentUser, parentParagraph, tags),
                paragraph -> new TestDataBuilder(session, currentObject, currentUser, (Paragraph) currentObject, tags),
                topic -> new TestDataBuilder(session, currentObject, currentUser, parentParagraph, tags)
        );
        results.addAll(childrenBuilder.apply(newBuilder).getResults());
        return this;
    }

    public TestDataBuilder save() {
        results.add(currentObject);
        return this;
    }

    public List<Object> getResults() {
        return results;
    }
}
