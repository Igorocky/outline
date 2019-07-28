package org.igye.outline2.controllers;

import org.hibernate.Session;
import org.igye.outline2.App;
import org.igye.outline2.manager.ImageRepository;
import org.igye.outline2.manager.NodeManager;
import org.igye.outline2.manager.NodeRepository;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.igye.outline2.OutlineUtils.getCurrentSession;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = App.class)
@ContextConfiguration(classes = ComponentTestConfig.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class ComponentTestBase {
    @Autowired
    private PlatformTransactionManager transactionManager;
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    protected TestClock testClock;
    @Autowired
    protected JdbcTemplate jdbcTemplate;
    @Autowired
    protected NodeManager nodeManager;
    @Autowired
    protected NodeRepository nodeRepository;
    @Autowired
    protected ImageRepository imageRepository;

    protected TransactionTemplate transactionTemplate;

    @Before
    public void componentTestBaseBefore() {
        transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    protected void exploreDB() {
        doInTransactionV(session -> OutlineTestUtils.exploreDB(entityManager));
    }

    protected <T> T doInTransaction(Function<Session, T> function) {
        return transactionTemplate.execute(status -> function.apply(getCurrentSession(entityManager)));
    }

    protected  void doInTransactionV(Consumer<Session> consumer) {
        doInTransaction(session -> {
            consumer.accept(session);
            return null;
        });
    }
}