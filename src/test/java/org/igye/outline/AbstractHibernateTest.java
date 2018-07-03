package org.igye.outline;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest(classes = HibernateTestConfigClass.class)
public class AbstractHibernateTest {
    @Autowired
    private SessionFactory sessionFactory;
    @Autowired
    private PlatformTransactionManager transactionManager;

    protected TransactionTemplate transactionTemplate;

    @Before
    public void beforeClass() {
        transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        transactionTemplate.execute(status -> {
            Session session = getCurrentSession();
            session.createQuery("delete from Content").executeUpdate();
            session.createQuery("delete from Topic").executeUpdate();
            session.createQuery("delete from Paragraph").executeUpdate();
            session.createQuery("delete from User").executeUpdate();
            session.createQuery("delete from Tag").executeUpdate();
            return null;
        });
    }

    protected Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }
}
