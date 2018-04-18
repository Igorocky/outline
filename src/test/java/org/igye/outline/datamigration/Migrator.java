package org.igye.outline.datamigration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.igye.outline.model.Paragraph;
import org.igye.outline.model.Topic;
import org.igye.outline.model.User;
import org.igye.outline.oldmodel.ParagraphOld;
import org.igye.outline.oldmodel.TopicOld;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


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
        loadChildren(res);

        return res;
    }

    @Transactional(transactionManager = "transactionManagerNewDb")
    public void saveNewData(User owner, List<ParagraphOld> paragraphsOld) {
        Session session = sessionFactoryNewDb.getCurrentSession();

        Paragraph rootParagraph = new Paragraph();
        rootParagraph.setName("root");
        rootParagraph.setOwner(owner);
        for (ParagraphOld paragraphOld: paragraphsOld) {
            rootParagraph.addChildParagraph(convertOldParagraph(paragraphOld));
        }
        session.persist(owner);
        session.persist(rootParagraph);
    }

    private Topic convertOldTopic(TopicOld topicOld) {
        Topic topic = new Topic();
        topic.setTitle(topicOld.getTitle());
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

    private void loadChildren(List<ParagraphOld> paragraphOlds) {
        for (ParagraphOld paragraphOld : paragraphOlds) {
            loadChildren(paragraphOld.getChildParagraphs());
            Hibernate.initialize(paragraphOld.getTopics());
        }
    }
}
