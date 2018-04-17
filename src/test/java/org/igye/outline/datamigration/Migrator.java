package org.igye.outline.datamigration;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.igye.outline.oldmodel.ParagraphOld;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Component
public class Migrator {
    @Autowired
    @Qualifier("sessionFactoryOldDb")
    private SessionFactory sessionFactoryOldDb;

    @Transactional(transactionManager = "transactionManagerOldDb", readOnly = true)
    public List<ParagraphOld> loadAllTopics() {
        Session session = sessionFactoryOldDb.getCurrentSession();
        List<ParagraphOld> res = session.createQuery(
                "from ParagraphOld p where p.parent is null",
                ParagraphOld.class
        ).getResultList();
        loadChildren(res);

        return res;
    }

    private void loadChildren(List<ParagraphOld> paragraphOlds) {
        for (ParagraphOld paragraphOld : paragraphOlds) {
//            Hibernate.initialize(paragraph.getChildParagraphs());
            loadChildren(paragraphOld.getChildrenParagraphs());
            Hibernate.initialize(paragraphOld.getTopics());
        }
    }
}
