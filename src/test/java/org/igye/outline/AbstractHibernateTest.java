package org.igye.outline;

import org.hibernate.Session;
import org.igye.outline.common.OutlineUtils;
import org.igye.outline.data.TestDataBuilder;
import org.igye.outline.htmlforms.SessionData;
import org.igye.outline.model.User;
import org.junit.Before;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@DataJpaTest
@EnableAutoConfiguration
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class AbstractHibernateTest {
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private PlatformTransactionManager transactionManager;
    @MockBean
    private SessionData sessionData;

    protected TransactionTemplate transactionTemplate;

    @Before
    public void beforeClass() {
        transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        doInTransactionV(session -> {
            session.createQuery("delete from Content").executeUpdate();
            session.createQuery("delete from Topic").executeUpdate();
            session.createQuery("delete from Paragraph").executeUpdate();
            session.createQuery("delete from User").executeUpdate();
        });
    }

    protected Map<String, Object> prepareTestData(Function<TestDataBuilder, TestDataBuilder> builder) {
        return doInTransaction(session -> {
            Map<String, Object> savedObjects = builder.apply(new TestDataBuilder(session)).getResults();
            Mockito.when(sessionData.getCurrentUser()).thenReturn(
                    (User) savedObjects.get(TestDataBuilder.CURRENT_USER)
            );
            return savedObjects;
        });
    }

    protected void exploreDb() {
        TestUtils.exploreDB(getCurrentSession());
    }

    protected  <T> T doInTransaction(Function<Session, T> function) {
        return transactionTemplate.execute(status -> function.apply(getCurrentSession()));
    }

    protected void doInTransactionV(Consumer<Session> consumer) {
        doInTransaction(session -> {
            consumer.accept(getCurrentSession());
            return null;
        });

    }

    protected void doInTransactionV(Runnable runnable) {
        doInTransactionV(session -> runnable.run());
    }

    protected <T> T doInTransaction(Supplier<T> supplier) {
        return doInTransaction(session -> supplier.get());
    }

    protected Session getCurrentSession() {
        return OutlineUtils.getCurrentSession(entityManager);
    }
}
