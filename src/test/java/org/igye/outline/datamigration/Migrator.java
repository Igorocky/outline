package org.igye.outline.datamigration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.igye.outline.controllers.Authenticator;
import org.igye.outline.model.Paragraph;
import org.igye.outline.model.Role;
import org.igye.outline.model.Topic;
import org.igye.outline.model.User;
import org.igye.outline.oldmodel.ParagraphOld;
import org.igye.outline.oldmodel.TopicOld;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.igye.outline.model.Paragraph.ROOT_NAME;


@Component
public class Migrator {
    private static final Logger LOG = LogManager.getLogger(Migrator.class);

    @Autowired
    @Qualifier("sessionFactoryOldDb")
    private SessionFactory sessionFactoryOldDb;

    @Autowired
    @Qualifier("sessionFactoryNewDb")
    private SessionFactory sessionFactoryNewDb;

    @Autowired
    private TagCollection tagCollection;

    @Transactional(transactionManager = "transactionManagerOldDb", readOnly = true)
    public List<ParagraphOld> loadOldData() {
        Session session = sessionFactoryOldDb.getCurrentSession();
        List<ParagraphOld> res = session.createQuery(
                "from ParagraphOld p where p.parent is null",
                ParagraphOld.class
        ).getResultList();
        Collections.sort(res, (o1, o2) -> (int) (o1.getId() - o2.getId()));
        loadChildren(res);

        return res;
    }

    @Transactional(transactionManager = "transactionManagerNewDb")
    public void createSchema() {
        Session session = sessionFactoryNewDb.getCurrentSession();

        User admin = new User();
        admin.setName("admin");
        admin.setPassword(BCrypt.hashpw("admin", BCrypt.gensalt(Authenticator.BCRYPT_SALT_ROUNDS)));

        Role adminRole = new Role();
        adminRole.setName("ADMIN");
        admin.getRoles().add(adminRole);

        session.persist(adminRole);
        session.persist(admin);
    }

    @Transactional(transactionManager = "transactionManagerNewDb")
    public void saveNewData(String ownerUserName, List<ParagraphOld> paragraphsOld) {
        Session session = sessionFactoryNewDb.getCurrentSession();

        User owner = session.createQuery("from User u where u.name = :userName", User.class)
                .setParameter("userName", ownerUserName)
                .getSingleResult();


        Paragraph rootParagraph = new Paragraph();
        rootParagraph.setName(ROOT_NAME);
        rootParagraph.setOwner(owner);
        for (ParagraphOld paragraphOld: paragraphsOld) {
            rootParagraph.addChildParagraph(convertOldParagraph(paragraphOld));
        }
        setOwnerRecursively(owner, rootParagraph);

        session.persist(rootParagraph);
    }

    @Transactional(transactionManager = "transactionManagerNewDb", readOnly = true)
    public void migrateImages(String oldImagesDir, String newImagesDir, List<ParagraphOld> paragraphsOld) {
        Session session = sessionFactoryNewDb.getCurrentSession();
        List<Topic> newTopics = new ArrayList<>();
        List<TopicOld> oldTopics = new ArrayList<>();
        List<Paragraph> paragraphs = session.createQuery(
                "from Paragraph p where p.parentParagraph is null",
                Paragraph.class
        ).getResultList();
        for (Paragraph paragraph: paragraphs) {
            newTopics.addAll(getAllTopics(paragraph));
        }
        for (ParagraphOld paragraph: paragraphsOld) {
            oldTopics.addAll(getAllTopics(paragraph));
        }
        Map<Long, Long> map = buildOldNewIdMap(oldTopics, newTopics);
        map.forEach((oldId, newId) -> copyImages(oldId, newId, oldImagesDir, newImagesDir));
    }

    private void copyImages(Long oldId, Long newId, String oldImagesDir, String newImagesDir) {
        File oldDir = new File(oldImagesDir + "/" + oldId);
        File newDir = new File(newImagesDir + "/" + newId);
        if (oldDir.listFiles() != null) {
            LOG.info("oldDir = '" + oldDir.getAbsolutePath() + "'");
            LOG.info("Start creating directory '" + newDir.getAbsolutePath() + "'");
            if (!newDir.mkdir()) {
                throw new RuntimeException("Could not create directory: " + newDir.getAbsolutePath());
            }
            LOG.info("Successfully created '" + newDir.getAbsolutePath() + "'");

            for (File file: oldDir.listFiles()) {
                File newFile = new File(newDir, file.getName());
                LOG.info("Copying '" + file.getAbsolutePath() +
                        "' to '" + newFile.getAbsolutePath() + "'");
                try {
                    FileCopyUtils.copy(file, newFile);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                LOG.info("Copied '" + file.getAbsolutePath() +
                        "' to '" + newFile.getAbsolutePath() + "'");
            }
        } else {
            LOG.warn("old dir doesn't exist: '" + oldDir.getAbsolutePath() + "'");
        }
    }

    private Topic convertOldTopic(TopicOld topicOld) {
        Topic topic = new Topic();
        topic.setName(topicOld.getTitle());
        topic.setImages(topicOld.getImages());
        topic.getTags().addAll(topicOld.getTags().stream().map(str -> tagCollection.getTag(str)).collect(Collectors.toList()));
        return topic;
    }

    private Paragraph convertOldParagraph(ParagraphOld paragraphOld) {
        Paragraph paragraph = new Paragraph();
        paragraph.setName(paragraphOld.getName());
        for (TopicOld topicOld : paragraphOld.getTopics()) {
            if (topicOld != null) {
                paragraph.addTopic(convertOldTopic(topicOld));
            }
        }
        for (ParagraphOld childParagraphOld: paragraphOld.getChildParagraphs()) {
            paragraph.addChildParagraph(convertOldParagraph(childParagraphOld));
        }
        return paragraph;
    }

    private Map<Long, Long> buildOldNewIdMap(List<TopicOld> oldTopics, List<Topic> newTopics) {
        Map<Long, Long> map = new HashMap<>();
        for (TopicOld topicOld: oldTopics) {
            if (topicOld != null) {
                map.put(
                        topicOld.getId(),
                        findTopicByTitle(newTopics, topicOld.getTitle(), topicOld.getParagraph().getName()).getId()
                );
            }
        }
        return map;
    }

    private Topic findTopicByTitle(List<Topic> topics, String topicTitle, String paragraphTitle) {
        for (Topic topic: topics) {
            if (topic.getName().equals(topicTitle) && topic.getParagraph().getName().equals(paragraphTitle)) {
                return topic;
            }
        }
        throw new RuntimeException("Topic with title '" + topicTitle + "' not found.");
    }

    private List<TopicOld> getAllTopics(ParagraphOld paragraph) {
        List<TopicOld> topics = new ArrayList<>();
        topics.addAll(paragraph.getTopics());
        for (ParagraphOld childParagraph : paragraph.getChildParagraphs()) {
            topics.addAll(getAllTopics(childParagraph));
        }
        return topics;
    }

    private List<Topic> getAllTopics(Paragraph paragraph) {
        List<Topic> topics = new ArrayList<>();
        topics.addAll(paragraph.getTopics());
        for (Paragraph childParagraph : paragraph.getChildParagraphs()) {
            topics.addAll(getAllTopics(childParagraph));
        }
        return topics;
    }

    private void loadChildren(List<ParagraphOld> paragraphOlds) {
        for (ParagraphOld paragraphOld : paragraphOlds) {
            loadChildren(paragraphOld.getChildParagraphs());
            Hibernate.initialize(paragraphOld.getTopics());
        }
    }

    private void setOwnerRecursively(User owner, Paragraph paragraph) {
        paragraph.setOwner(owner);
        for (Paragraph childPar : paragraph.getChildParagraphs()) {
            setOwnerRecursively(owner, childPar);
            for (Topic topic : childPar.getTopics()) {
                topic.setOwner(owner);
            }
        }
    }
}
