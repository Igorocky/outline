package org.igye.outline.data;

import org.igye.outline.AbstractHibernateTest;
import org.igye.outline.model.Paragraph;
import org.igye.outline.model.Topic;
import org.igye.outline.model.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.util.UriComponentsBuilder;

import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.igye.outline.model.Paragraph.ROOT_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = Dao.class)
@PropertySource("test.properties")
public class DaoTest extends AbstractHibernateTest {
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
}