package org.igye.outline.data;

import org.hibernate.Hibernate;
import org.hibernate.SessionFactory;
import org.igye.outline.model.Paragraph;
import org.igye.outline.model.Topic;
import org.igye.outline.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.igye.outline.model.Paragraph.ROOT_NAME;
import static org.springframework.transaction.annotation.Propagation.MANDATORY;

@Component
public class Dao {
    @Autowired
    private SessionFactory sessionFactory;

    @Transactional
    public Paragraph loadParagraphById(Optional<Long> idOpt, User owner) {
        Paragraph paragraph = idOpt
                .map(id -> loadParagraphByNotNullId(id, owner))
                .orElseGet(() -> loadRootParagraph(owner));
        Hibernate.initialize(paragraph.getChildParagraphs());
        Hibernate.initialize(paragraph.getTopics());
        Hibernate.initialize(paragraph.getTags());
        return paragraph;
    }

    @Transactional
    public Topic loadTopicById(Long id, User owner) {
        return sessionFactory.getCurrentSession().createQuery(
                "from Topic t where t.id = :id and owner = :owner", Topic.class
        )
                .setParameter("id", id)
                .setParameter("owner", owner)
                .getSingleResult();
    }

    @Transactional(propagation = MANDATORY)
    protected Paragraph loadRootParagraph(User owner) {
        return sessionFactory.getCurrentSession().createQuery(
                "from Paragraph p where p.name = :name and p.owner = :owner and p.parentParagraph is null",
                Paragraph.class
        )
                .setParameter("name", ROOT_NAME)
                .setParameter("owner", owner)
                .setComment("@loadRootParagraph")
                .getSingleResult();
    }

    @Transactional(propagation = MANDATORY)
    protected Paragraph loadParagraphByNotNullId(Long id, User owner) {
        return sessionFactory.getCurrentSession().createQuery(
                "from Paragraph p where p.id = :id and owner = :owner", Paragraph.class
        )
                .setParameter("id", id)
                .setParameter("owner", owner)
                .getSingleResult();
    }
}
