package org.igye.outline.data;

import org.hibernate.Session;
import org.igye.outline.exceptions.OutlineException;
import org.igye.outline.model.Image;
import org.igye.outline.model.Paragraph;
import org.igye.outline.model.Role;
import org.igye.outline.model.Text;
import org.igye.outline.model.Topic;
import org.igye.outline.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import static org.igye.outline.data.UserDao.ADMIN_ROLE_NAME;

public class TestDataBuilder {
    public static final String CURRENT_USER = "CURRENT_USER";

    private final Session session;

    private User parentUser;
    private Paragraph parentParagraph;
    private Topic parentTopic;

    private Object currentObject;

    private Map<String, Object> results = new HashMap<>();

    public TestDataBuilder(Session session) {
        this.session = session;
    }

    public TestDataBuilder(TestDataBuilder parent) {
        this.session = parent.session;
        parentUser = parent.parentUser;
        parentParagraph = parent.parentParagraph;
        parentTopic = parent.parentTopic;
    }

    public TestDataBuilder user(String name) {
        User user = new User();
        user.setName(name);
        user.setPassword("p");
        session.persist(user);
        parentUser = user;
        currentObject = user;
        return this;
    }

    public TestDataBuilder admin() {
        return role(ADMIN_ROLE_NAME);
    }

    public TestDataBuilder role(String roleName) {
        if (!(currentObject instanceof User)) {
            throw new OutlineException("!(currentObject instanceof User)");
        }
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
        ((User)currentObject).getRoles().add(role);
        return this;
    }

    public TestDataBuilder paragraph(String name) {
        Paragraph paragraph = new Paragraph();
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

    public TestDataBuilder topic(String name) {
        Topic topic = new Topic();
        topic.setName(name);
        if (parentParagraph != null) {
            parentParagraph.addChildNode(topic);
        } else {
            topic.setOwner(parentUser);
            session.persist(topic);
        }
        currentObject = topic;
        return this;
    }

    public TestDataBuilder image() {
        Image image = new Image();
        parentTopic.addContent(image);
        currentObject = image;
        return this;
    }

    public TestDataBuilder text(String txt) {
        Text text = new Text();
        text.setText(txt);
        parentTopic.addContent(text);
        currentObject = text;
        return this;
    }

    public TestDataBuilder currentUser() {
        if (!(currentObject instanceof User)) {
            throw new OutlineException("!(currentObject instanceof User)");
        }
        return save(CURRENT_USER, currentObject);
    }

    public TestDataBuilder children(Function<TestDataBuilder, TestDataBuilder> childrenBuilder) {
        TestDataBuilder newBuilder = processForCurrentObject(
                user -> new TestDataBuilder(this).setParentUser(user),
                paragraph -> new TestDataBuilder(this).setParentParagraph(paragraph),
                topic -> new TestDataBuilder(this).setParentTopic(topic),
                image -> {
                    throw new OutlineException("Image cannot have children.");
                },
                text -> {
                    throw new OutlineException("Text cannot have children.");
                }
        );
        results.putAll(childrenBuilder.apply(newBuilder).getResults());
        return this;
    }

    public TestDataBuilder save(String name) {
        return save(name, currentObject);
    }

    public TestDataBuilder saveId(String name) {
        UUID id = processForCurrentObject(
                User::getId,
                Paragraph::getId,
                Topic::getId,
                Image::getId,
                Text::getId
        );
        return save(name, id);
    }

    private TestDataBuilder save(String name, Object object) {
        if (results.containsKey(name)) {
            throw new OutlineException("Duplicated key.");
        }
        results.put(name, object);
        return this;
    }

    public Map<String, Object> getResults() {
        return results;
    }

    public TestDataBuilder setCurrentObject(Object currentObject) {
        this.currentObject = currentObject;
        return this;
    }

    public TestDataBuilder setParentUser(User parentUser) {
        this.parentUser = parentUser;
        return this;
    }

    public TestDataBuilder setParentParagraph(Paragraph parentParagraph) {
        this.parentParagraph = parentParagraph;
        return this;
    }

    public TestDataBuilder setParentTopic(Topic parentTopic) {
        this.parentTopic = parentTopic;
        return this;
    }

    private <T> T  processForCurrentObject(Function<User, T> userConsumer,
                                           Function<Paragraph, T> paragraphConsumer,
                                           Function<Topic, T> topicConsumer,
                                           Function<Image, T> imageConsumer,
                                           Function<Text, T> textConsumer) {
        if (currentObject instanceof User) {
            return userConsumer.apply((User) currentObject);
        } else if (currentObject instanceof Paragraph) {
            return paragraphConsumer.apply((Paragraph) currentObject);
        } else if (currentObject instanceof Topic) {
            return topicConsumer.apply((Topic) currentObject);
        } else if (currentObject instanceof Image) {
            return imageConsumer.apply((Image) currentObject);
        } else if (currentObject instanceof Text) {
            return textConsumer.apply((Text) currentObject);
        } else {
            throw new TestDataBuilderException("Unexpected type of currentObject: " + currentObject);
        }
    }
}
