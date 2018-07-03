package org.igye.outline.datamigration;

import com.google.common.collect.ImmutableSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.igye.outline.common.OutlineUtils;
import org.igye.outline.data.Dao;
import org.igye.outline.data.UserDao;
import org.igye.outline.model.*;
import org.igye.outline.oldmodel.ParagraphOld;
import org.igye.outline.oldmodel.TopicOld;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Component
public class Migrator {
    private static final Logger LOG = LogManager.getLogger(Migrator.class);

    @Autowired
    @Qualifier("sessionFactoryOldDb")
    private SessionFactory sessionFactoryOldDb;

    @Autowired
    private SessionFactory sessionFactoryNewDb;

    @Autowired
    private UserDao userDao;

    @Autowired
    private Dao dao;

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

    @Transactional
    public void createSchema() {
        Session session = sessionFactoryNewDb.getCurrentSession();

        Role adminRole = new Role();
        adminRole.setName(UserDao.ADMIN_ROLE_NAME);
        session.save(adminRole);

        User fakeAdminUser = new User();
        fakeAdminUser.getRoles().add(adminRole);
        userDao.createUser(fakeAdminUser, "admin", "admin", ImmutableSet.of(adminRole.getId()));
    }

    @Transactional
    public void saveNewData(String ownerUserName, List<ParagraphOld> paragraphsOld) {
        Session session = sessionFactoryNewDb.getCurrentSession();

        User owner = session.createQuery("from User u where u.name = :userName", User.class)
                .setParameter("userName", ownerUserName)
                .getSingleResult();


        Paragraph rootParagraph = dao.loadRootParagraph(owner);
        for (ParagraphOld paragraphOld: paragraphsOld) {
            rootParagraph.addChildParagraph(convertOldParagraph(paragraphOld));
        }
        setOwnerRecursively(owner, rootParagraph);
    }

    @Transactional(readOnly = true)
    public void migrateImages(String oldImagesDir, String newImagesDir, List<ParagraphOld> paragraphsOld) {
        Session session = sessionFactoryNewDb.getCurrentSession();
        List<Topic> newTopics = new ArrayList<>();
        List<TopicOld> oldTopics = new ArrayList<>();
        List<Paragraph> paragraphs = session.createQuery(
                "from Paragraph p where p.name in (:names)",
                Paragraph.class
        ).setParameter("names", paragraphsOld.stream().map(ParagraphOld::getName).collect(Collectors.toList()))
                .getResultList();
        for (Paragraph paragraph: paragraphs) {
            newTopics.addAll(getAllTopics(paragraph));
        }
        for (ParagraphOld paragraph: paragraphsOld) {
            oldTopics.addAll(getAllTopics(paragraph));
        }
        oldTopics = oldTopics.stream().filter(t -> t != null).collect(Collectors.toList());
        Map<Long, TopicOld> oldTopicsMap = oldTopics.stream().collect(Collectors.toMap(TopicOld::getId, Function.identity()));
        Map<UUID, Topic> newTopicsMap = newTopics.stream().collect(Collectors.toMap(Topic::getId, Function.identity()));
        Map<Long, UUID> map = buildOldNewIdMap(oldTopics, newTopics);
        map.forEach(
                (oldId, newId) -> copyImages(
                        oldTopicsMap.get(oldId),
                        (SynopsisTopic) newTopicsMap.get(newId),
                        oldImagesDir,
                        newImagesDir
                )
        );
    }

    private void copyImages(TopicOld oldTopic, SynopsisTopic newTopic, String oldImagesDir, String newImagesDir) {
        File oldDir = new File(oldImagesDir + "/" + oldTopic.getId());
        if (oldDir.listFiles() != null) {
            LOG.info("oldDir = '" + oldDir.getAbsolutePath() + "'");
            for (int i = 0; i < oldTopic.getImages().size(); i++) {
                String oldImgStr = oldTopic.getImages().get(i);
                File oldFile = new File(oldDir, oldImgStr);
                File newFile = OutlineUtils.getImgFile(newImagesDir, newTopic.getContents().get(i).getId());
                if (!newFile.getParentFile().exists()) {
                    newFile.getParentFile().mkdirs();
                }
                LOG.info("Copying '" + oldFile.getAbsolutePath() +
                        "' to '" + newFile.getAbsolutePath() + "'");
                try {
                    FileCopyUtils.copy(oldFile, newFile);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                LOG.info("Copied '" + oldFile.getAbsolutePath() +
                        "' to '" + newFile.getAbsolutePath() + "'");
            }
        } else {
            LOG.warn("old dir doesn't exist: '" + oldDir.getAbsolutePath() + "'");
        }
    }

    private Topic convertOldTopic(TopicOld topicOld) {
        SynopsisTopic topic = new SynopsisTopic();
        topic.setName(topicOld.getTitle());
        for (String img : topicOld.getImages()) {
            topic.addContent(new Image());
        }
//        topic.getTags().addAll(topicOld.getTags().stream().map(str -> tagCollection.getTag(str)).collect(Collectors.toList()));
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

    private Map<Long, UUID> buildOldNewIdMap(List<TopicOld> oldTopics, List<Topic> newTopics) {
        return oldTopics.stream().collect(Collectors.toMap(
                TopicOld::getId,
                topicOld -> findTopicByTitle(newTopics, topicOld.getTitle(), topicOld.getParagraph().getName()).getId()
        ));
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
                ((SynopsisTopic)topic).getContents().forEach(c -> c.setOwner(owner));
            }
        }
    }
}
