package org.igye.outline.data;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.igye.outline.AbstractHibernateTest;
import org.igye.outline.exceptions.OutlineException;
import org.igye.outline.htmlforms.ContentForForm;
import org.igye.outline.htmlforms.EditSynopsisTopicForm;
import org.igye.outline.htmlforms.ReorderParagraphChildren;
import org.igye.outline.model.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.NoResultException;
import java.util.*;

import static org.igye.outline.common.OutlineUtils.SQL_DEBUG_LOGGER_NAME;
import static org.igye.outline.htmlforms.ContentForForm.ContentTypeForForm.IMAGE;
import static org.igye.outline.htmlforms.ContentForForm.ContentTypeForForm.TEXT;
import static org.igye.outline.model.Paragraph.ROOT_NAME;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = Dao.class)
@TestPropertySource("/test.properties")
public class DaoTest extends AbstractHibernateTest {
    private static final Logger DEBUG_LOG = LogManager.getLogger(SQL_DEBUG_LOGGER_NAME);

    @Autowired
    private Dao dao;

    @Test
    public void loadParagraphById_should_select_correct_root_paragraph_when_there_are_several_paragraphs_with_name_root() {
        //given
        User owner = transactionTemplate.execute(status ->
                (User) new TestDataBuilder(getCurrentSession())
                    .user("owner1").save().children(b -> b
                        .paragraph(ROOT_NAME).tag("rootTag1").children(b2 -> b2
                            .paragraph(ROOT_NAME).tag("notRootTag1")
                        )
                    ).user("owner2").children(b -> b
                        .paragraph(ROOT_NAME).tag("rootTag2").children(b2 -> b2
                            .paragraph(ROOT_NAME).tag("notRootTag2")
                        )
                    ).getResults().get(0)
        );
//        TestUtils.exploreDB(getCurrentSession());

        //when
        Paragraph paragraph = dao.loadParagraphById(Optional.empty(), owner);

        //then
        assertEquals("rootTag1", paragraph.getTags().iterator().next().getName());
    }

    @Test
    public void loadParagraphById_should_select_paragraph_belonging_only_to_the_specified_user() {
        //given
        User owner = transactionTemplate.execute(status ->
                (User) new TestDataBuilder(getCurrentSession())
                        .user("owner1").children(b -> b
                                .paragraph(ROOT_NAME).tag("rootTag1").children(b2 -> b2
                                        .paragraph("P1")
                                )
                        ).user("owner2").save().children(b -> b
                                .paragraph(ROOT_NAME).tag("rootTag2").children(b2 -> b2
                                        .paragraph("P2")
                                )
                        ).getResults().get(0)
        );

        //when
        Paragraph paragraph = dao.loadParagraphById(Optional.empty(), owner);

        //then
        assertEquals("rootTag2", paragraph.getTags().iterator().next().getName());
    }

    @Test
    public void loadParagraphById_should_select_paragraph_by_id() {
        //given
        List<Object> saved = transactionTemplate.execute(status ->
                new TestDataBuilder(getCurrentSession())
                        .user("owner1").children(b -> b
                                .paragraph(ROOT_NAME).children(b2 -> b2
                                        .paragraph("P1")
                                )
                        ).user("owner2").save().children(b -> b
                                .paragraph(ROOT_NAME).children(b2 -> b2
                                        .paragraph("P2")
                                        .paragraph("P3").save().children(b3->b3
                                            .paragraph("C1")
                                            .paragraph("C2")
                                            .paragraph("C3")
                                        )
                                        .paragraph("P4")
                                )
                        ).getResults()
        );
        User owner = (User) saved.get(0);
        Paragraph savedParagraph = (Paragraph) saved.get(1);

        //when
        Paragraph paragraph = dao.loadParagraphById(Optional.of(savedParagraph.getId()), owner);

        //then
        assertEquals("P3", paragraph.getName());
    }

    @Test
    public void loadParagraphById_should_return_child_paragraphs_in_correct_order() {
        //given
        User owner = transactionTemplate.execute(status ->
                (User) new TestDataBuilder(getCurrentSession())
                        .user("owner1").save().children(b -> b
                        .paragraph(ROOT_NAME).children(b2 -> b2
                                .paragraph("P2")
                                .paragraph("P1")
                                .paragraph("P4")
                                .paragraph("P3")
                        )
                ).getResults().get(0)
        );

        //when
        Paragraph paragraph = dao.loadParagraphById(Optional.empty(), owner);

        //then
        assertEquals(4, paragraph.getChildParagraphs().size());
        assertEquals("P2", paragraph.getChildParagraphs().get(0).getName());
        assertEquals("P1", paragraph.getChildParagraphs().get(1).getName());
        assertEquals("P4", paragraph.getChildParagraphs().get(2).getName());
        assertEquals("P3", paragraph.getChildParagraphs().get(3).getName());
    }

    @Test
    public void loadParagraphById_should_initialize_topics() {
        //given
        List<Object> saved = transactionTemplate.execute(status ->
                new TestDataBuilder(getCurrentSession())
                        .user("owner1").save().children(b -> b
                                .paragraph(ROOT_NAME).children(b2 -> b2
                                        .paragraph("P1")
                                        .paragraph("P2").save().children(b3->b3
                                                .topic("T1")
                                                .topic("T3")
                                                .topic("T2")
                                        )
                                        .paragraph("P3")
                                )
                        ).getResults()
        );
        User owner = (User) saved.get(0);
        Paragraph savedParagraph = (Paragraph) saved.get(1);

        //when
        Paragraph paragraph = dao.loadParagraphById(Optional.of(savedParagraph.getId()), owner);

        //then
        assertEquals(3, paragraph.getTopics().size());
        assertEquals("T1", paragraph.getTopics().get(0).getName());
        assertEquals("T3", paragraph.getTopics().get(1).getName());
        assertEquals("T2", paragraph.getTopics().get(2).getName());
    }

    @Test(expected = NoResultException.class)
    public void loadParagraphById_should_fail_when_requested_paragraph_has_different_owner() {
        //given
        List<Object> saved = transactionTemplate.execute(status ->
                new TestDataBuilder(getCurrentSession())
                .user("owner1").save().children(b -> b
                        .paragraph(ROOT_NAME).children(b2 -> b2
                                .paragraph("P1")
                        )
                ).user("owner2").children(b -> b
                        .paragraph(ROOT_NAME).children(b2 -> b2
                                .paragraph("P2").save()
                        )
                ).getResults()
        );
        User owner = (User) saved.get(0);
        Paragraph savedParagraph = (Paragraph) saved.get(1);

        //when
        Paragraph paragraph = dao.loadParagraphById(Optional.of(savedParagraph.getId()), owner);

        //then an exception should be thrown
    }

    @Test
    public void loadTopicById_should_load_topic_belonging_to_the_specified_user() {
        //given
        List<Object> saved = transactionTemplate.execute(status ->
                new TestDataBuilder(getCurrentSession())
                        .user("owner1").children(b -> b
                                .paragraph(ROOT_NAME).children(b2 -> b2
                                        .paragraph("P1")
                                        .paragraph("P2").children(b3->b3
                                                .topic("T1")
                                                .topic("T3")
                                                .topic("T2")
                                        )
                                        .paragraph("P3")
                                )
                        ).user("owner2").save().children(b -> b
                                .paragraph(ROOT_NAME).children(b2 -> b2
                                        .paragraph("P1")
                                        .paragraph("P2").children(b3->b3
                                                .topic("T10")
                                                .topic("T30").save()
                                                .topic("T20")
                                        )
                                        .paragraph("P3")
                                )
                        ).getResults()
        );
        User owner = (User) saved.get(0);
        Topic savedTopic = (Topic) saved.get(1);

        //when
        Topic topic = dao.loadTopicById(savedTopic.getId(), owner);

        //then
        assertEquals("T30", topic.getName());
    }

    @Test
    public void loadTopicById_should_load_all_parent_paragraphs() {
        //given
        List<Object> saved = transactionTemplate.execute(status ->
                new TestDataBuilder(getCurrentSession())
                        .user("owner1").save().children(b -> b
                                .paragraph(ROOT_NAME).children(b2 -> b2
                                        .paragraph("P1").children(b3->b3
                                            .paragraph("P2").children(b4->b4
                                                .paragraph("P3").children(b5->b5
                                                        .topic("T1").save()
                                                )
                                            )
                                        )
                                )
                        ).getResults()
        );
        User owner = (User) saved.get(0);
        Topic savedTopic = (Topic) saved.get(1);

        //when
        Topic topic = dao.loadTopicById(savedTopic.getId(), owner);

        //then
        assertEquals("P3", topic.getParagraph().getName());
        assertEquals("P2", topic.getParagraph().getParentParagraph().getName());
        assertEquals("P1", topic.getParagraph().getParentParagraph().getParentParagraph().getName());
    }

    @Test(expected = NoResultException.class)
    public void loadTopicById_should_fail_when_requested_topic_has_different_owner() {
        //given
        List<Object> saved = transactionTemplate.execute(status ->
                new TestDataBuilder(getCurrentSession())
                        .user("owner1").save().children(b -> b
                                .paragraph(ROOT_NAME).children(b2 -> b2
                                        .paragraph("P2").children(b3->b3
                                                .topic("T2")
                                        )
                                )
                        ).user("owner2").children(b -> b
                                .paragraph(ROOT_NAME).children(b2 -> b2
                                        .paragraph("P3").children(b3->b3
                                                .topic("T30").save()
                                        )
                                )
                        ).getResults()
        );
        User owner = (User) saved.get(0);
        Topic savedTopic = (Topic) saved.get(1);

        //when
        Topic topic = dao.loadTopicById(savedTopic.getId(), owner);

        //then an exception should be thrown
    }

    @Test
    public void nextTopic_should_load_correct_topic_in_the_same_paragraph() {
        //given
        List<Object> saved = transactionTemplate.execute(status ->
                new TestDataBuilder(getCurrentSession())
                        .user("owner").save().children(b -> b
                            .paragraph(ROOT_NAME).children(b2 -> b2
                                .paragraph("Book1").children(b3->b3
                                        .topic("T1")
                                        .topic("T2").save()
                                        .topic("T3")
                                )
                            )
                        ).getResults()
        );
        User owner = (User) saved.get(0);
        Topic savedTopic = (Topic) saved.get(1);

        //when
        Topic topic = dao.nextTopic(savedTopic.getId(), owner).get();

        //then
        assertEquals("T3", topic.getName());
    }

    @Test
    public void nextTopic_should_load_correct_topic_in_next_paragraph() {
        //given
        List<Object> saved = transactionTemplate.execute(status ->
                new TestDataBuilder(getCurrentSession())
                        .user("owner").save().children(b -> b
                            .paragraph(ROOT_NAME).children(b2 -> b2
                                .paragraph("Book1").children(b3->b3
                                        .paragraph("Book1").children(b4->b4
                                                .topic("T1")
                                                .topic("T2")
                                                .topic("T3").save()
                                        )
                                        .paragraph("Book1").children(b4->b4
                                                .topic("T4")
                                                .topic("T5")
                                                .topic("T6")
                                        )
                                )
                                .paragraph("Book2").children(b3->b3
                                        .topic("T7")
                                        .topic("T8")
                                        .topic("T9")
                                )
                            )
                        ).getResults()
        );
        User owner = (User) saved.get(0);
        Topic savedTopic = (Topic) saved.get(1);

        //when
        Topic topic = dao.nextTopic(savedTopic.getId(), owner).get();

        //then
        assertEquals("T4", topic.getName());
    }

    @Test
    public void nextTopic_should_return_none_for_the_last_topic_in_the_book_of_one_level() {
        //given
        List<Object> saved = transactionTemplate.execute(status ->
                new TestDataBuilder(getCurrentSession())
                        .user("owner").save().children(b -> b
                            .paragraph(ROOT_NAME).children(b2 -> b2
                                .paragraph("Book1").children(b3->b3
                                        .topic("T1")
                                        .topic("T2")
                                        .topic("T3").save()
                                )
                                .paragraph("Book2").children(b3->b3
                                        .topic("T4")
                                        .topic("T5")
                                        .topic("T6")
                                )
                            )
                        ).getResults()
        );
        User owner = (User) saved.get(0);
        Topic savedTopic = (Topic) saved.get(1);

        //when
        Optional<Topic> topicOpt = dao.nextTopic(savedTopic.getId(), owner);

        //then
        assertFalse(topicOpt.isPresent());
    }

    @Test
    public void nextTopic_should_return_none_for_the_last_topic_in_the_book_of_few_levels() {
        //given
        List<Object> saved = transactionTemplate.execute(status ->
                new TestDataBuilder(getCurrentSession())
                        .user("owner").save().children(b -> b
                            .paragraph(ROOT_NAME).children(b2 -> b2
                                .paragraph("Book1").children(b3->b3
                                        .paragraph("P1").children(b4->b4
                                                .topic("T1")
                                                .topic("T2")
                                                .topic("T3")
                                        )
                                        .paragraph("P2").children(b4->b4
                                                .topic("T4")
                                                .topic("T5")
                                                .topic("T6").save()
                                        )
                                )
                                .paragraph("Book2").children(b3->b3
                                        .topic("T4")
                                        .topic("T5")
                                        .topic("T6")
                                )
                            )
                        ).getResults()
        );
        User owner = (User) saved.get(0);
        Topic savedTopic = (Topic) saved.get(1);

        //when
        Optional<Topic> topicOpt = dao.nextTopic(savedTopic.getId(), owner);

        //then
        assertFalse(topicOpt.isPresent());
    }

    @Test
    public void nextTopic_it_should_be_possible_to_traverse_all_topics_in_correct_order() {
        //given
        List<Object> saved = transactionTemplate.execute(status ->
                new TestDataBuilder(getCurrentSession())
                        .user("owner").save().children(b -> b
                            .paragraph(ROOT_NAME).children(b2 -> b2
                                .paragraph("Book1").children(b3->b3
                                        .paragraph("P1").children(b4->b4
                                                .topic("T1").save()
                                                .topic("T2")
                                        )
                                        .paragraph("P2").children(b4->b4
                                                .topic("T3")
                                                .topic("T4")
                                        )
                                )
                                .paragraph("Book2").children(b3->b3
                                        .topic("T5")
                                        .topic("T6")
                                        .topic("T7")
                                )
                            )
                        ).getResults()
        );
        User owner = (User) saved.get(0);
        Topic savedTopic = (Topic) saved.get(1);

        //when
        List<Topic> allTopics = new ArrayList<>();
        Optional<Topic> nextTopicOpt = Optional.of(savedTopic);
        while (nextTopicOpt.isPresent()) {
            allTopics.add(nextTopicOpt.get());
            nextTopicOpt = dao.nextTopic(nextTopicOpt.get().getId(), owner);
        }

        //then
        assertEquals(4, allTopics.size());
        assertEquals("T1", allTopics.get(0).getName());
        assertEquals("T2", allTopics.get(1).getName());
        assertEquals("T3", allTopics.get(2).getName());
        assertEquals("T4", allTopics.get(3).getName());
    }

    @Test
    public void nextTopic_should_skip_empty_terminal_paragraphs() {
        //given
        List<Object> saved = transactionTemplate.execute(status ->
                new TestDataBuilder(getCurrentSession())
                        .user("owner").save().children(b -> b
                            .paragraph(ROOT_NAME).children(b2 -> b2
                                .paragraph("Book1").children(b3->b3
                                        .paragraph("P1").children(b4->b4
                                                .topic("T1")
                                                .topic("T2").save()
                                        )
                                        .paragraph("P2").children(b4->b4
                                                .paragraph("P3").children(b5->b5
                                                )
                                        )
                                        .paragraph("P4").children(b4->b4
                                                .topic("T3")
                                                .topic("T4")
                                        )
                                )
                            )
                        ).getResults()
        );
        User owner = (User) saved.get(0);
        Topic savedTopic = (Topic) saved.get(1);

        //when
        Topic nextTopic = dao.nextTopic(savedTopic.getId(), owner).get();

        //then
        assertEquals("T3", nextTopic.getName());
    }

    @Test
    public void prevTopic_should_load_correct_topic_in_the_same_paragraph() {
        //given
        List<Object> saved = transactionTemplate.execute(status ->
                new TestDataBuilder(getCurrentSession())
                        .user("owner").save().children(b -> b
                            .paragraph(ROOT_NAME).children(b2 -> b2
                                .paragraph("Book1").children(b3->b3
                                        .topic("T1")
                                        .topic("T2").save()
                                        .topic("T3")
                                )
                            )
                        ).getResults()
        );
        User owner = (User) saved.get(0);
        Topic savedTopic = (Topic) saved.get(1);

        //when
        Topic topic = dao.prevTopic(savedTopic.getId(), owner).get();

        //then
        assertEquals("T1", topic.getName());
    }

    @Test
    public void prevTopic_should_load_correct_topic_in_prev_paragraph() {
        //given
        List<Object> saved = transactionTemplate.execute(status ->
                new TestDataBuilder(getCurrentSession())
                        .user("owner").save().children(b -> b
                            .paragraph(ROOT_NAME).children(b2 -> b2
                                .paragraph("Book1").children(b3->b3
                                        .paragraph("P1").children(b4->b4
                                                .topic("T1")
                                                .topic("T2")
                                                .topic("T3")
                                        )
                                        .paragraph("P2").children(b4->b4
                                                .topic("T4").save()
                                                .topic("T5")
                                                .topic("T6")
                                        )
                                )
                                .paragraph("Book2").children(b3->b3
                                        .topic("T7")
                                        .topic("T8")
                                        .topic("T9")
                                )
                            )
                        ).getResults()
        );
        User owner = (User) saved.get(0);
        Topic savedTopic = (Topic) saved.get(1);

        //when
        Topic topic = dao.prevTopic(savedTopic.getId(), owner).get();

        //then
        assertEquals("T3", topic.getName());
    }

    @Test
    public void prevTopic_should_return_none_for_the_first_topic_in_the_book_of_one_level() {
        //given
        List<Object> saved = transactionTemplate.execute(status ->
                new TestDataBuilder(getCurrentSession())
                        .user("owner").save().children(b -> b
                            .paragraph(ROOT_NAME).children(b2 -> b2
                                .paragraph("Book1").children(b3->b3
                                        .topic("T1")
                                        .topic("T2")
                                        .topic("T3")
                                )
                                .paragraph("Book2").children(b3->b3
                                        .topic("T4").save()
                                        .topic("T5")
                                        .topic("T6")
                                )
                            )
                        ).getResults()
        );
        User owner = (User) saved.get(0);
        Topic savedTopic = (Topic) saved.get(1);

        //when
        Optional<Topic> topicOpt = dao.prevTopic(savedTopic.getId(), owner);

        //then
        assertFalse(topicOpt.isPresent());
    }

    @Test
    public void prevTopic_should_return_none_for_the_first_topic_in_the_book_of_few_levels() {
        //given
        List<Object> saved = transactionTemplate.execute(status ->
                new TestDataBuilder(getCurrentSession())
                        .user("owner").save().children(b -> b
                            .paragraph(ROOT_NAME).children(b2 -> b2
                                .paragraph("Book1").children(b3->b3
                                        .topic("T4")
                                        .topic("T5")
                                        .topic("T6")
                                )
                                .paragraph("Book2").children(b3->b3
                                        .paragraph("P1").children(b4->b4
                                                .topic("T1").save()
                                                .topic("T2")
                                                .topic("T3")
                                        )
                                        .paragraph("P2").children(b4->b4
                                                .topic("T4")
                                                .topic("T5")
                                                .topic("T6")
                                        )
                                )

                            )
                        ).getResults()
        );
        User owner = (User) saved.get(0);
        Topic savedTopic = (Topic) saved.get(1);

        //when
        Optional<Topic> topicOpt = dao.prevTopic(savedTopic.getId(), owner);

        //then
        assertFalse(topicOpt.isPresent());
    }

    @Test
    public void prevTopic_it_should_be_possible_to_traverse_all_topics_in_reverse_order() {
        //given
        List<Object> saved = transactionTemplate.execute(status ->
                new TestDataBuilder(getCurrentSession())
                        .user("owner").save().children(b -> b
                            .paragraph(ROOT_NAME).children(b2 -> b2
                                .paragraph("Book2").children(b3->b3
                                        .topic("T5")
                                        .topic("T6")
                                        .topic("T7")
                                )
                                .paragraph("Book1").children(b3->b3
                                        .paragraph("P1").children(b4->b4
                                                .topic("T1")
                                                .topic("T2")
                                        )
                                        .paragraph("P2").children(b4->b4
                                                .topic("T3")
                                                .topic("T4").save()
                                        )
                                )
                            )
                        ).getResults()
        );
        User owner = (User) saved.get(0);
        Topic savedTopic = (Topic) saved.get(1);

        //when
        List<Topic> allTopics = new ArrayList<>();
        Optional<Topic> prevTopicOpt = Optional.of(savedTopic);
        while (prevTopicOpt.isPresent()) {
            allTopics.add(prevTopicOpt.get());
            prevTopicOpt = dao.prevTopic(prevTopicOpt.get().getId(), owner);
        }

        //then
        assertEquals(4, allTopics.size());
        assertEquals("T4", allTopics.get(0).getName());
        assertEquals("T3", allTopics.get(1).getName());
        assertEquals("T2", allTopics.get(2).getName());
        assertEquals("T1", allTopics.get(3).getName());
    }

    @Test
    public void prevTopic_should_skip_empty_terminal_paragraphs() {
        //given
        List<Object> saved = transactionTemplate.execute(status ->
                new TestDataBuilder(getCurrentSession())
                        .user("owner").save().children(b -> b
                            .paragraph(ROOT_NAME).children(b2 -> b2
                                .paragraph("Book1").children(b3->b3
                                        .paragraph("P1").children(b4->b4
                                                .topic("T1")
                                                .topic("T2")
                                        )
                                        .paragraph("P2").children(b4->b4
                                                .paragraph("P3").children(b5->b5
                                                )
                                        )
                                        .paragraph("P4").children(b4->b4
                                                .topic("T3").save()
                                                .topic("T4")
                                        )
                                )
                            )
                        ).getResults()
        );
        User owner = (User) saved.get(0);
        Topic savedTopic = (Topic) saved.get(1);

        //when
        Topic nextTopic = dao.prevTopic(savedTopic.getId(), owner).get();

        //then
        assertEquals("T2", nextTopic.getName());
    }

    @Test
    public void createParagraph_should_create_paragraph() {
        //given
        List<Object> saved = transactionTemplate.execute(status ->
                new TestDataBuilder(getCurrentSession())
                        .user("owner").save().children(b -> b
                            .paragraph(ROOT_NAME)
                        ).getResults()
        );
        User owner = (User) saved.get(0);
        Paragraph rootParagraph = dao.loadRootParagraph(owner);

        //when
        dao.createParagraph(rootParagraph.getId(), "new-par");

        //then
        transactionTemplate.execute(status -> {
            Paragraph root = dao.loadRootParagraph(owner);
            assertEquals(1, root.getChildParagraphs().size());
            Paragraph newPar = root.getChildParagraphs().get(0);
            assertEquals("new-par", newPar.getName());
            assertEquals(owner.getId(), newPar.getOwner().getId());
            return null;
        });
    }

    @Test
    public void updateParagraph_should_update_paragraph() {
        //given
        List<Object> saved = transactionTemplate.execute(status ->
                new TestDataBuilder(getCurrentSession())
                        .user("owner").save().children(b -> b
                            .paragraph(ROOT_NAME).children(b1 -> b1
                                        .paragraph("P2").save()
                                )
                        ).getResults()
        );
        User owner = (User) saved.get(0);
        Paragraph paragraph = (Paragraph) saved.get(1);

        //when
        dao.updateParagraph(owner, paragraph.getId(), p -> p.setName("WSX"));

        //then
        transactionTemplate.execute(status -> {
            Assert.assertEquals(
                    "WSX",
                    getCurrentSession().load(Paragraph.class, paragraph.getId()).getName()
            );
            return null;
        });
    }

    @Test
    public void reorderParagraphChildren_should_reorder_paragraphs_and_topics() {
        //given
        List<Object> saved = prepareDataForReordering();
        User owner = (User) saved.get(0);
        Paragraph p1c = (Paragraph) saved.get(1);
        Paragraph p2a = (Paragraph) saved.get(2);
        Paragraph p3d = (Paragraph) saved.get(3);
        Paragraph p4b = (Paragraph) saved.get(4);
        Topic t1c = (Topic) saved.get(5);
        Topic t2a = (Topic) saved.get(6);
        Topic t3d = (Topic) saved.get(7);
        Topic t4b = (Topic) saved.get(8);
        ReorderParagraphChildren request = new ReorderParagraphChildren();
        request.setParentId(dao.loadRootParagraph(owner).getId());
        request.setParagraphs(Arrays.asList(p2a.getId(), p4b.getId(), p1c.getId(), p3d.getId()));
        request.setTopics(Arrays.asList(t2a.getId(), t4b.getId(), t1c.getId(), t3d.getId()));

        //when
        dao.reorderParagraphChildren(owner, request);

        //then
        transactionTemplate.execute(status -> {
            Paragraph root = dao.loadRootParagraph(owner);
            assertEquals(p2a.getId(), root.getChildParagraphs().get(0).getId());
            assertEquals(p4b.getId(), root.getChildParagraphs().get(1).getId());
            assertEquals(p1c.getId(), root.getChildParagraphs().get(2).getId());
            assertEquals(p3d.getId(), root.getChildParagraphs().get(3).getId());
            assertEquals(t2a.getId(), root.getTopics().get(0).getId());
            assertEquals(t4b.getId(), root.getTopics().get(1).getId());
            assertEquals(t1c.getId(), root.getTopics().get(2).getId());
            assertEquals(t3d.getId(), root.getTopics().get(3).getId());
            return null;
        });
    }

    @Test(expected = OutlineException.class)
    public void reorderParagraphChildren_should_fail_on_mismatching_paragraph_ids() {
        //given
        List<Object> saved = prepareDataForReordering();
        User owner = (User) saved.get(0);
        Paragraph p1c = (Paragraph) saved.get(1);
        Paragraph p3d = (Paragraph) saved.get(3);
        Paragraph p4b = (Paragraph) saved.get(4);
        ReorderParagraphChildren request = new ReorderParagraphChildren();
        request.setParentId(dao.loadRootParagraph(owner).getId());
        request.setParagraphs(Arrays.asList(p4b.getId(), p1c.getId(), p3d.getId()));

        //when
        dao.reorderParagraphChildren(owner, request);

        //then
        //an exception should be thrown
    }

    @Test(expected = OutlineException.class)
    public void reorderParagraphChildren_should_fail_on_mismatching_topic_ids() {
        //given
        List<Object> saved = prepareDataForReordering();
        User owner = (User) saved.get(0);
        Topic t1c = (Topic) saved.get(5);
        Topic t2a = (Topic) saved.get(6);
        Topic t4b = (Topic) saved.get(8);
        ReorderParagraphChildren request = new ReorderParagraphChildren();
        request.setParentId(dao.loadRootParagraph(owner).getId());
        request.setTopics(Arrays.asList(t2a.getId(), t4b.getId(), t1c.getId()));

        //when
        dao.reorderParagraphChildren(owner, request);

        //then
        //an exception should be thrown
    }

    @Test
    public void moveParagraph_should_move_paragraph_when_destination_paragraph_contains_more_children_than_source_paragraph() {
        //given
        List<Object> saved = transactionTemplate.execute(status ->
                new TestDataBuilder(getCurrentSession())
                        .user("owner").save().children(b -> b
                            .paragraph(ROOT_NAME).children(b1 -> b1
                                    .paragraph("P1").save().children(b2 -> b2
                                        .paragraph("P2")
                                        .paragraph("P3").save()
                                        .paragraph("P4")
                                    )
                                    .paragraph("P5").save().children(b2 -> b2
                                        .paragraph("P6")
                                        .paragraph("P7")
                                        .paragraph("P8")
                                        .paragraph("P9")
                                        .paragraph("P10")
                                    )
                            )
                ).getResults()
        );
        User owner = (User) saved.get(0);
        Paragraph p1 = (Paragraph) saved.get(1);
        Paragraph p3 = (Paragraph) saved.get(2);
        Paragraph p5 = (Paragraph) saved.get(3);
        transactionTemplate.execute(status -> {
            List<Paragraph> p1Children = dao.loadParagraphByNotNullId(p1.getId(), owner).getChildParagraphs();
            assertEquals(3, p1Children.size());
            assertEquals("P2", p1Children.get(0).getName());
            assertEquals("P3", p1Children.get(1).getName());
            assertEquals("P4", p1Children.get(2).getName());
            List<Paragraph> p5Children = dao.loadParagraphByNotNullId(p5.getId(), owner).getChildParagraphs();
            assertEquals(5, p5Children.size());
            assertEquals("P6", p5Children.get(0).getName());
            assertEquals("P7", p5Children.get(1).getName());
            assertEquals("P8", p5Children.get(2).getName());
            assertEquals("P9", p5Children.get(3).getName());
            assertEquals("P10", p5Children.get(4).getName());
            return null;
        });

        //when
        dao.moveParagraph(owner, p3.getId(), p5.getId());

        //then
        transactionTemplate.execute(status -> {
            List<Paragraph> p1Children = dao.loadParagraphByNotNullId(p1.getId(), owner).getChildParagraphs();
            assertEquals(2, p1Children.size());
            assertEquals("P2", p1Children.get(0).getName());
            assertEquals("P4", p1Children.get(1).getName());
            List<Paragraph> p5Children = dao.loadParagraphByNotNullId(p5.getId(), owner).getChildParagraphs();
            assertEquals(6, p5Children.size());
            assertEquals("P6", p5Children.get(0).getName());
            assertEquals("P7", p5Children.get(1).getName());
            assertEquals("P8", p5Children.get(2).getName());
            assertEquals("P9", p5Children.get(3).getName());
            assertEquals("P10", p5Children.get(4).getName());
            assertEquals("P3", p5Children.get(5).getName());
            return null;
        });

    }

    @Test
    public void moveParagraph_should_move_paragraph_when_destination_paragraph_contains_less_children_than_source_paragraph() {
        //given
        List<Object> saved = transactionTemplate.execute(status ->
                new TestDataBuilder(getCurrentSession())
                        .user("owner").save().children(b -> b
                            .paragraph(ROOT_NAME).children(b1 -> b1
                                    .paragraph("P1").save().children(b2 -> b2
                                        .paragraph("P2")
                                        .paragraph("P3").save()
                                        .paragraph("P4")
                                    )
                                    .paragraph("P5").save().children(b2 -> b2
                                        .paragraph("P6")
                                    )
                            )
                ).getResults()
        );
        User owner = (User) saved.get(0);
        Paragraph p1 = (Paragraph) saved.get(1);
        Paragraph p3 = (Paragraph) saved.get(2);
        Paragraph p5 = (Paragraph) saved.get(3);
        transactionTemplate.execute(status -> {
            List<Paragraph> p1Children = dao.loadParagraphByNotNullId(p1.getId(), owner).getChildParagraphs();
            assertEquals(3, p1Children.size());
            assertEquals("P2", p1Children.get(0).getName());
            assertEquals("P3", p1Children.get(1).getName());
            assertEquals("P4", p1Children.get(2).getName());
            List<Paragraph> p5Children = dao.loadParagraphByNotNullId(p5.getId(), owner).getChildParagraphs();
            assertEquals(1, p5Children.size());
            assertEquals("P6", p5Children.get(0).getName());
            return null;
        });

        //when
        dao.moveParagraph(owner, p3.getId(), p5.getId());

        //then
        transactionTemplate.execute(status -> {
            List<Paragraph> p1Children = dao.loadParagraphByNotNullId(p1.getId(), owner).getChildParagraphs();
            assertEquals(2, p1Children.size());
            assertEquals("P2", p1Children.get(0).getName());
            assertEquals("P4", p1Children.get(1).getName());
            List<Paragraph> p5Children = dao.loadParagraphByNotNullId(p5.getId(), owner).getChildParagraphs();
            assertEquals(2, p5Children.size());
            assertEquals("P6", p5Children.get(0).getName());
            assertEquals("P3", p5Children.get(1).getName());
            return null;
        });

    }

    @Test
    public void moveParagraph_should_move_sibling_paragraphs_one_into_another() {
        //given
        List<Object> saved = transactionTemplate.execute(status ->
                new TestDataBuilder(getCurrentSession())
                        .user("owner").save().children(b -> b
                            .paragraph(ROOT_NAME).children(b1 -> b1
                                    .paragraph("P1").save()
                                    .paragraph("P2").save()
                            )
                ).getResults()
        );
        User owner = (User) saved.get(0);
        Paragraph p1 = (Paragraph) saved.get(1);
        Paragraph p2 = (Paragraph) saved.get(2);
        transactionTemplate.execute(status -> {
            List<Paragraph> rootChildren = dao.loadRootParagraph(owner).getChildParagraphs();
            assertEquals(2, rootChildren.size());
            assertEquals("P1", rootChildren.get(0).getName());
            assertEquals("P2", rootChildren.get(1).getName());
            List<Paragraph> p1Children = dao.loadParagraphByNotNullId(p1.getId(), owner).getChildParagraphs();
            assertEquals(0, p1Children.size());
            List<Paragraph> p2Children = dao.loadParagraphByNotNullId(p2.getId(), owner).getChildParagraphs();
            assertEquals(0, p2Children.size());
            return null;
        });

        //when
        dao.moveParagraph(owner, p2.getId(), p1.getId());

        //then
        transactionTemplate.execute(status -> {
            List<Paragraph> rootChildren = dao.loadRootParagraph(owner).getChildParagraphs();
            assertEquals(1, rootChildren.size());
            assertEquals("P1", rootChildren.get(0).getName());
            List<Paragraph> p1Children = dao.loadParagraphByNotNullId(p1.getId(), owner).getChildParagraphs();
            assertEquals(1, p1Children.size());
            assertEquals("P2", p1Children.get(0).getName());
            List<Paragraph> p2Children = dao.loadParagraphByNotNullId(p2.getId(), owner).getChildParagraphs();
            assertEquals(0, p2Children.size());
            return null;
        });

    }

    @Test(expected = OutlineException.class)
    public void moveParagraph_should_deny_moving_paragraph_under_itself() {
        //given
        List<Object> saved = transactionTemplate.execute(status ->
                new TestDataBuilder(getCurrentSession())
                        .user("owner").save().children(b -> b
                        .paragraph(ROOT_NAME).children(b1 -> b1
                                .paragraph("P1").save().children(b2 -> b2
                                        .paragraph("P2")
                                        .paragraph("P3").save()
                                        .paragraph("P4")
                                )
                                .paragraph("P5").save().children(b2 -> b2
                                        .paragraph("P6")
                                )
                        )
                ).getResults()
        );
        User owner = (User) saved.get(0);
        Paragraph p1 = (Paragraph) saved.get(1);
        Paragraph p3 = (Paragraph) saved.get(2);

        //when
        dao.moveParagraph(owner, p1.getId(), p3.getId());

        //then
        //an exception should be thrown
    }

    @Test
    public void moveTopic_should_move_topic_when_destination_paragraph_contains_more_children_than_source_paragraph() {
        //given
        List<Object> saved = transactionTemplate.execute(status ->
                new TestDataBuilder(getCurrentSession())
                        .user("owner").save().children(b -> b
                        .paragraph(ROOT_NAME).children(b1 -> b1
                                .paragraph("P1").save().children(b2 -> b2
                                        .topic("T2")
                                        .topic("T3").save()
                                        .topic("T4")
                                )
                                .paragraph("P5").save().children(b2 -> b2
                                        .topic("T6")
                                        .topic("T7")
                                        .topic("T8")
                                        .topic("T9")
                                        .topic("T10")
                                )
                        )
                ).getResults()
        );
        User owner = (User) saved.get(0);
        Paragraph p1 = (Paragraph) saved.get(1);
        Topic t3 = (Topic) saved.get(2);
        Paragraph p5 = (Paragraph) saved.get(3);
        transactionTemplate.execute(status -> {
            List<Topic> p1Children = dao.loadParagraphByNotNullId(p1.getId(), owner).getTopics();
            assertEquals(3, p1Children.size());
            assertEquals("T2", p1Children.get(0).getName());
            assertEquals("T3", p1Children.get(1).getName());
            assertEquals("T4", p1Children.get(2).getName());
            List<Topic> p5Children = dao.loadParagraphByNotNullId(p5.getId(), owner).getTopics();
            assertEquals(5, p5Children.size());
            assertEquals("T6", p5Children.get(0).getName());
            assertEquals("T7", p5Children.get(1).getName());
            assertEquals("T8", p5Children.get(2).getName());
            assertEquals("T9", p5Children.get(3).getName());
            assertEquals("T10", p5Children.get(4).getName());
            return null;
        });

        //when
        dao.moveTopic(owner, t3.getId(), p5.getId());

        //then
        transactionTemplate.execute(status -> {
            List<Topic> p1Children = dao.loadParagraphByNotNullId(p1.getId(), owner).getTopics();
            assertEquals(2, p1Children.size());
            assertEquals("T2", p1Children.get(0).getName());
            assertEquals("T4", p1Children.get(1).getName());
            List<Topic> p5Children = dao.loadParagraphByNotNullId(p5.getId(), owner).getTopics();
            assertEquals(6, p5Children.size());
            assertEquals("T6", p5Children.get(0).getName());
            assertEquals("T7", p5Children.get(1).getName());
            assertEquals("T8", p5Children.get(2).getName());
            assertEquals("T9", p5Children.get(3).getName());
            assertEquals("T10", p5Children.get(4).getName());
            assertEquals("T3", p5Children.get(5).getName());
            return null;
        });

    }

    @Test
    public void moveTopic_should_move_topic_when_destination_paragraph_contains_less_children_than_source_paragraph() {
        //given
        List<Object> saved = transactionTemplate.execute(status ->
                new TestDataBuilder(getCurrentSession())
                        .user("owner").save().children(b -> b
                        .paragraph(ROOT_NAME).children(b1 -> b1
                                .paragraph("P1").save().children(b2 -> b2
                                        .topic("T2")
                                        .topic("T3").save()
                                        .topic("T4")
                                )
                                .paragraph("P5").save().children(b2 -> b2
                                        .topic("T6")
                                )
                        )
                ).getResults()
        );
        User owner = (User) saved.get(0);
        Paragraph p1 = (Paragraph) saved.get(1);
        Topic t3 = (Topic) saved.get(2);
        Paragraph p5 = (Paragraph) saved.get(3);
        transactionTemplate.execute(status -> {
            List<Topic> p1Children = dao.loadParagraphByNotNullId(p1.getId(), owner).getTopics();
            assertEquals(3, p1Children.size());
            assertEquals("T2", p1Children.get(0).getName());
            assertEquals("T3", p1Children.get(1).getName());
            assertEquals("T4", p1Children.get(2).getName());
            List<Topic> p5Children = dao.loadParagraphByNotNullId(p5.getId(), owner).getTopics();
            assertEquals(1, p5Children.size());
            assertEquals("T6", p5Children.get(0).getName());
            return null;
        });

        //when
        dao.moveTopic(owner, t3.getId(), p5.getId());

        //then
        transactionTemplate.execute(status -> {
            List<Topic> p1Children = dao.loadParagraphByNotNullId(p1.getId(), owner).getTopics();
            assertEquals(2, p1Children.size());
            assertEquals("T2", p1Children.get(0).getName());
            assertEquals("T4", p1Children.get(1).getName());
            List<Topic> p5Children = dao.loadParagraphByNotNullId(p5.getId(), owner).getTopics();
            assertEquals(2, p5Children.size());
            assertEquals("T6", p5Children.get(0).getName());
            assertEquals("T3", p5Children.get(1).getName());
            return null;
        });

    }

    @Test
    public void createSynopsisTopic_should_create_new_topic() {
        //given
        List<Object> saved = transactionTemplate.execute(status ->
                new TestDataBuilder(getCurrentSession())
                        .user("owner").save().children(b -> b
                        .paragraph(ROOT_NAME).children(b1 -> b1
                                .paragraph("P1").save().children(b2 -> b2
                                        .topic("T2")
                                )
                        )
                ).getResults()
        );
        User owner = (User) saved.get(0);
        Paragraph p1 = (Paragraph) saved.get(1);
        final UUID[] imgId = new UUID[1];
        transactionTemplate.execute(status -> {
            Session session = getCurrentSession();
            Image img = new Image();
            img.setOwner(session.load(User.class, owner.getId()));
            imgId[0] = (UUID) session.save(img);
            return null;
        });
        transactionTemplate.execute(status -> {
            List<Topic> p1Children = dao.loadParagraphByNotNullId(p1.getId(), owner).getTopics();
            assertEquals(1, p1Children.size());
            assertEquals("T2", p1Children.get(0).getName());
            return null;
        });
        EditSynopsisTopicForm form = new EditSynopsisTopicForm();
        form.setParentId(p1.getId());
        form.setName("ST");
        form.setContent(Arrays.asList(
                ContentForForm.builder().type(TEXT).text("ttt1").build(),
                ContentForForm.builder().type(IMAGE).id(imgId[0]).build()
        ));

        //when
        UUID newTopicId = dao.createSynopsisTopic(owner, form);

        //then
        transactionTemplate.execute(status -> {
            List<Topic> p1Children = dao.loadParagraphByNotNullId(p1.getId(), owner).getTopics();
            assertEquals(2, p1Children.size());
            assertEquals("T2", p1Children.get(0).getName());

            SynopsisTopic st = (SynopsisTopic) p1Children.get(1);
            assertEquals(newTopicId, st.getId());
            assertEquals("ST", st.getName());
            assertEquals(2, st.getContents().size());

            assertEquals("ttt1", ((Text)st.getContents().get(0)).getText());
            assertEquals(owner.getId(), st.getContents().get(0).getOwner().getId());

            assertTrue(st.getContents().get(1) instanceof Image);
            assertEquals(imgId[0], st.getContents().get(1).getId());
            assertEquals(owner.getId(), st.getContents().get(1).getOwner().getId());

            return null;
        });

    }

    @Test
    public void createImage_should_create_new_image() {
        //given
        User user = transactionTemplate.execute(status -> {
            User usr = new User();
            usr.setName("uuu");
            usr.setPassword("ddddd");
            getCurrentSession().save(usr);
            return usr;
        });

        //when
        UUID imgId = dao.createImage(user);

        //then
        Image img = transactionTemplate.execute(status -> {
            Image im = getCurrentSession().load(Image.class, imgId);
            im.getOwner();
            return im;
        });
        Assert.assertEquals(user.getId(), img.getOwner().getId());
    }

    @Test
    public void updateSynopsisTopic_should_change_topic_name() {
        //given
        List<Object> objs = transactionTemplate.execute(status -> {
            Session session = getCurrentSession();
            User usr = createUser(session);

            Paragraph par = new Paragraph();
            par.setName("par");
            par.setOwner(usr);
            session.persist(par);

            SynopsisTopic topic = new SynopsisTopic();
            topic.setName("top");
            par.addTopic(topic);

            return Arrays.asList(usr, topic);
        });
        User user = (User) objs.get(0);
        SynopsisTopic topic = (SynopsisTopic) objs.get(1);

        Assert.assertEquals("top", topic.getName());
        Assert.assertEquals(0, topic.getContents().size());

        EditSynopsisTopicForm form = new EditSynopsisTopicForm();
        form.setId(topic.getId());
        form.setName("T-o-P");
        form.setContent(Collections.emptyList());

        //when
        dao.updateSynopsisTopic(user, form);

        //then
        SynopsisTopic updatedTopic = dao.loadSynopsisTopicByIdWithContent(form.getId(), user);
        Assert.assertEquals("T-o-P", updatedTopic.getName());
        Assert.assertEquals(0, updatedTopic.getContents().size());
    }

    @Test
    public void updateSynopsisTopic_should_not_change_unchanged_items() {
        //given
        List<Object> objs = transactionTemplate.execute(status -> {
            Session session = getCurrentSession();
            User usr = createUser(session);

            Paragraph par = new Paragraph();
            par.setName("par");
            par.setOwner(usr);
            session.persist(par);

            SynopsisTopic topic = new SynopsisTopic();
            topic.setName("top");
            par.addTopic(topic);

            topic.addContent(new Image());
            topic.addContent(Text.builder().text("t1").build());

            return Arrays.asList(usr, topic);
        });
        User user = (User) objs.get(0);
        SynopsisTopic topic = (SynopsisTopic) objs.get(1);
        Image i1 = (Image) topic.getContents().get(0);
        Text t1 = (Text) topic.getContents().get(1);

        Assert.assertEquals(2, topic.getContents().size());
        Assert.assertEquals("t1", t1.getText());


        EditSynopsisTopicForm form = new EditSynopsisTopicForm();
        form.setId(topic.getId());
        form.setName("T-o-P");
        form.setContent(Arrays.asList(
                ContentForForm.builder().type(IMAGE).id(i1.getId()).build(),
                ContentForForm.builder().type(TEXT).id(t1.getId()).text(t1.getText()).build()
        ));

        //when
        dao.updateSynopsisTopic(user, form);

        //then
        SynopsisTopic updatedTopic = dao.loadSynopsisTopicByIdWithContent(form.getId(), user);
        List<Content> contents = updatedTopic.getContents();
        Assert.assertEquals(2, contents.size());
        Assert.assertEquals(i1.getId(), contents.get(0).getId());
        Assert.assertEquals(t1.getId(), contents.get(1).getId());
        Assert.assertEquals("t1", ((Text)contents.get(1)).getText());
    }

    @Test
    public void updateSynopsisTopic_should_change_order_of_content() {
        //given
        List<Object> objs = transactionTemplate.execute(status -> {
            Session session = getCurrentSession();
            User usr = createUser(session);

            Paragraph par = new Paragraph();
            par.setName("par");
            par.setOwner(usr);
            session.persist(par);

            SynopsisTopic topic = new SynopsisTopic();
            topic.setName("top");
            par.addTopic(topic);

            topic.addContent(new Image());
            topic.addContent(Text.builder().text("t1").build());
            topic.addContent(Text.builder().text("t2").build());
            topic.addContent(new Image());

            return Arrays.asList(usr, topic);
        });
        User user = (User) objs.get(0);
        SynopsisTopic topic = (SynopsisTopic) objs.get(1);
        Image i1 = (Image) topic.getContents().get(0);
        Text t1 = (Text) topic.getContents().get(1);
        Text t2 = (Text) topic.getContents().get(2);
        Image i2 = (Image) topic.getContents().get(3);

        Assert.assertEquals(4, topic.getContents().size());
        Assert.assertEquals("t1", t1.getText());
        Assert.assertEquals("t2", t2.getText());


        EditSynopsisTopicForm form = new EditSynopsisTopicForm();
        form.setId(topic.getId());
        form.setName("T-o-P");
        form.setContent(Arrays.asList(
                ContentForForm.builder().type(TEXT).id(t2.getId()).text(t2.getText()).build(),
                ContentForForm.builder().type(IMAGE).id(i2.getId()).build(),
                ContentForForm.builder().type(IMAGE).id(i1.getId()).build(),
                ContentForForm.builder().type(TEXT).id(t1.getId()).text(t1.getText()).build()
        ));

        //when
        dao.updateSynopsisTopic(user, form);

        //then
        SynopsisTopic updatedTopic = dao.loadSynopsisTopicByIdWithContent(form.getId(), user);
        List<Content> contents = updatedTopic.getContents();
        Assert.assertEquals(4, contents.size());
        Assert.assertEquals(t2.getId(), contents.get(0).getId());
        Assert.assertEquals("t2", ((Text)contents.get(0)).getText());
        Assert.assertEquals(i2.getId(), contents.get(1).getId());
        Assert.assertEquals(i1.getId(), contents.get(2).getId());
        Assert.assertEquals(t1.getId(), contents.get(3).getId());
        Assert.assertEquals("t1", ((Text)contents.get(3)).getText());
    }

    @Test
    public void updateSynopsisTopic_should_update_text_of_text_content() {
        //given
        List<Object> objs = transactionTemplate.execute(status -> {
            Session session = getCurrentSession();
            User usr = createUser(session);

            Paragraph par = new Paragraph();
            par.setName("par");
            par.setOwner(usr);
            session.persist(par);

            SynopsisTopic topic = new SynopsisTopic();
            topic.setName("top");
            par.addTopic(topic);

            topic.addContent(Text.builder().text("txt1").build());

            UUID newImgId = dao.createImage(usr);

            return Arrays.asList(usr, topic, newImgId);
        });
        User user = (User) objs.get(0);
        SynopsisTopic topic = (SynopsisTopic) objs.get(1);
        Text text1 = (Text) topic.getContents().get(0);

        Assert.assertEquals("txt1", text1.getText());

        EditSynopsisTopicForm form = new EditSynopsisTopicForm();
        form.setId(topic.getId());
        form.setName("T-o-P");
        form.setContent(Arrays.asList(
                ContentForForm.builder().type(TEXT).id(text1.getId()).text("new-text").build()
        ));

        //when
        dao.updateSynopsisTopic(user, form);

        //then
        SynopsisTopic updatedTopic = dao.loadSynopsisTopicByIdWithContent(form.getId(), user);
        List<Content> contents = updatedTopic.getContents();
        Assert.assertEquals(1, contents.size());
        Assert.assertEquals(text1.getId(), contents.get(0).getId());
        Assert.assertEquals("new-text", ((Text)contents.get(0)).getText());
    }

    @Test
    public void updateSynopsisTopic_should_remove_released_content_items_from_database() {
        //given
        List<Object> objs = transactionTemplate.execute(status -> {
            Session session = getCurrentSession();
            User usr = createUser(session);

            Paragraph par = new Paragraph();
            par.setName("par");
            par.setOwner(usr);
            session.persist(par);

            SynopsisTopic topic = new SynopsisTopic();
            topic.setName("top");
            par.addTopic(topic);

            topic.addContent(new Image());
            topic.addContent(Text.builder().text("txt1").build());
            topic.addContent(Text.builder().text("txt2").build());

            UUID newImgId = dao.createImage(usr);

            return Arrays.asList(usr, topic, newImgId);
        });
        User user = (User) objs.get(0);
        SynopsisTopic topic = (SynopsisTopic) objs.get(1);
        Image img1 = (Image) topic.getContents().get(0);
        Text text1 = (Text) topic.getContents().get(1);
        Text text2 = (Text) topic.getContents().get(2);

        Assert.assertEquals(3, topic.getContents().size());
        Assert.assertEquals("txt1", text1.getText());
        Assert.assertEquals("txt2", text2.getText());


        EditSynopsisTopicForm form = new EditSynopsisTopicForm();
        form.setId(topic.getId());
        form.setName("T-o-P");
        form.setContent(Arrays.asList(
                ContentForForm.builder().type(TEXT).id(text1.getId()).text(text1.getText()).build()
        ));

        //when
        dao.updateSynopsisTopic(user, form);

        //then
        SynopsisTopic updatedTopic = dao.loadSynopsisTopicByIdWithContent(form.getId(), user);
        List<Content> contents = updatedTopic.getContents();
        Assert.assertEquals(1, contents.size());
        Assert.assertEquals(text1.getId(), contents.get(0).getId());
        Assert.assertEquals("txt1", ((Text)contents.get(0)).getText());

        transactionTemplate.execute(status -> {
           Assert.assertNull(getCurrentSession().get(Image.class, img1.getId()));
           Assert.assertNull(getCurrentSession().get(Text.class, text2.getId()));
           return null;
        });
    }

    @Test
    public void updateSynopsisTopic_should_create_new_items_in_content() {
        //given
        List<Object> objs = transactionTemplate.execute(status -> {
            Session session = getCurrentSession();
            User usr = createUser(session);

            Paragraph par = new Paragraph();
            par.setName("par");
            par.setOwner(usr);
            session.persist(par);

            SynopsisTopic topic = new SynopsisTopic();
            topic.setName("top");
            par.addTopic(topic);

            topic.addContent(new Image());
            topic.addContent(Text.builder().text("txt-no-change").build());

            UUID newImgId = dao.createImage(usr);

            return Arrays.asList(usr, topic, newImgId);
        });
        User user = (User) objs.get(0);
        SynopsisTopic topic = (SynopsisTopic) objs.get(1);
        UUID newImageId = (UUID) objs.get(2);
        Image img1 = (Image) topic.getContents().get(0);
        Text text1 = (Text) topic.getContents().get(1);

        Assert.assertEquals("top", topic.getName());
        Assert.assertEquals(2, topic.getContents().size());
        Assert.assertEquals("txt-no-change", text1.getText());


        EditSynopsisTopicForm form = new EditSynopsisTopicForm();
        form.setId(topic.getId());
        form.setName("T-o-P");
        form.setContent(Arrays.asList(
                ContentForForm.builder().type(IMAGE).id(img1.getId()).build(),
                ContentForForm.builder().type(TEXT).id(text1.getId()).text(text1.getText()).build(),
                ContentForForm.builder().type(TEXT).text("text2").build(),
                ContentForForm.builder().type(IMAGE).id(newImageId).build()
        ));

        //when
        dao.updateSynopsisTopic(user, form);

        //then
        SynopsisTopic updatedTopic = dao.loadSynopsisTopicByIdWithContent(form.getId(), user);
        Assert.assertEquals("T-o-P", updatedTopic.getName());
        List<Content> contents = updatedTopic.getContents();
        Assert.assertEquals(4, contents.size());
        Assert.assertEquals(img1.getId(), contents.get(0).getId());
        Assert.assertEquals(text1.getId(), contents.get(1).getId());
        Assert.assertEquals("txt-no-change", ((Text)contents.get(1)).getText());
        Assert.assertEquals("text2", ((Text)contents.get(2)).getText());
        Assert.assertTrue(contents.get(3) instanceof Image);
        Assert.assertEquals(newImageId, contents.get(3).getId());
    }

    private User createUser(Session session) {
        User usr = new User();
        usr.setName("uuu");
        usr.setPassword("ddddd");
        session.persist(usr);
        return usr;
    }

    private List<Object> prepareDataForReordering() {
        return transactionTemplate.execute(status ->
                new TestDataBuilder(getCurrentSession())
                        .user("owner").save().children(b -> b
                        .paragraph(ROOT_NAME).children(b1 -> b1
                                .paragraph("1C").save()
                                .paragraph("2A").save()
                                .paragraph("3D").save()
                                .paragraph("4B").save()
                                .topic("1C").save()
                                .topic("2A").save()
                                .topic("3D").save()
                                .topic("4B").save()
                        )
                ).getResults()
        );
    }
}