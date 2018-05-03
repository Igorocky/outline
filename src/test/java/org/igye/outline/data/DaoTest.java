package org.igye.outline.data;

import org.igye.outline.AbstractHibernateTest;
import org.igye.outline.model.Paragraph;
import org.igye.outline.model.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;

import static org.igye.outline.model.Paragraph.ROOT_NAME;
import static org.junit.Assert.assertEquals;

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
    public void loadParagraphById_should_select_paragraph_by_parent_id() {
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
}