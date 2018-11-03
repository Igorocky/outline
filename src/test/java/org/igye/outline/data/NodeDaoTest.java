package org.igye.outline.data;

import com.google.common.collect.ImmutableSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.igye.outline.AbstractHibernateTest;
import org.igye.outline.common.OutlineUtils;
import org.igye.outline.exceptions.OutlineException;
import org.igye.outline.htmlforms.ContentForForm;
import org.igye.outline.htmlforms.EditTopicForm;
import org.igye.outline.htmlforms.ReorderNodeChildren;
import org.igye.outline.model.Content;
import org.igye.outline.model.Image;
import org.igye.outline.model.Node;
import org.igye.outline.model.Paragraph;
import org.igye.outline.model.Text;
import org.igye.outline.model.Topic;
import org.igye.outline.model.User;
import org.igye.outline.selection.ActionType;
import org.igye.outline.selection.ObjectType;
import org.igye.outline.selection.Selection;
import org.igye.outline.selection.SelectionPart;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.igye.outline.common.OutlineUtils.SQL_DEBUG_LOGGER_NAME;
import static org.igye.outline.common.OutlineUtils.map;
import static org.igye.outline.htmlforms.ContentForForm.ContentTypeForForm.IMAGE;
import static org.igye.outline.htmlforms.ContentForForm.ContentTypeForForm.TEXT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = NodeDao.class)
public class NodeDaoTest extends AbstractHibernateTest {
    private static final Logger DEBUG_LOG = LogManager.getLogger(SQL_DEBUG_LOGGER_NAME);
    public static final String OWNER = "owner";
    public static final String SAVED_ID = "savedId";
    public static final String SAVED_PARAGRAPH = "savedParagraph";
    public static final String SAVED_PARAGRAPH_1 = "savedParagraph-1";
    public static final String SAVED_PARAGRAPH_2 = "savedParagraph-2";
    public static final String SAVED_PARAGRAPH_3 = "savedParagraph-3";
    public static final String SAVED_TOPIC = "savedTopic";
    public static final String SAVED_TOPIC1 = "savedTopic1";
    public static final String SAVED_TOPIC2 = "savedTopic2";
    public static final String SAVED_IMAGE = "savedImage";
    public static final String SAVED_IMAGE1 = "savedImage1";
    public static final String SAVED_IMAGE2 = "savedImage2";
    public static final String SAVED_IMAGE3 = "savedImage3";
    public static final String SAVED_IMAGE4 = "savedImage4";
    public static final String SAVED_IMAGE5 = "savedImage5";
    public static final String SAVED_IMAGE6 = "savedImage6";
    public static final String SAVED_TEXT = "savedText";
    public static final String SAVED_TEXT1 = "savedText1";
    public static final String SAVED_TEXT2 = "savedText2";

    @Autowired
    private NodeDao dao;

    @Test
    public void getRootNodes_should_select_only_root_nodes() {
        //given
        prepareTestData(b -> b
                .user("user1").currentUser().children(b1 -> b1
                        .paragraph("P1").children(b2 -> b2
                                .paragraph("C1")
                                .topic("C2")
                        )
                        .paragraph("P2").children(b2 -> b2
                                .topic("C3")
                                .paragraph("C4")
                        )
                        .topic("T1")
                )

        );

        //when
        Set<String> rootNodes = OutlineUtils.mapToSet(dao.getRootNodes(), Node::getName);

        //then
        assertEquals(ImmutableSet.of("P1", "P2", "T1"), rootNodes);
    }

    @Test
    public void getRootNodes_should_select_root_nodes_belonging_only_to_current_user() {
        //given
        prepareTestData(b -> b
                .user("user1").children(b1 -> b1
                        .paragraph("P1")
                        .topic("T1")
                )
                .user("user2").currentUser().children(b1 -> b1
                        .paragraph("P2")
                        .topic("T2")
                )

        );

        //when
        Set<String> rootNodes = OutlineUtils.mapToSet(dao.getRootNodes(), Node::getName);

        //then
        assertEquals(ImmutableSet.of("P2", "T2"), rootNodes);
    }

    @Test
    public void getParagraphById_should_return_paragraph_by_id() {
        //given
        Map<String, Object> testData = prepareTestData(b -> b
                .user("user1").currentUser().children(b1 -> b1
                        .paragraph("P1").children(b2 -> b2
                                .paragraph("P2").saveId(SAVED_PARAGRAPH)
                                .topic("T1")
                        )
                        .topic("T2")
                )

        );

        //when
        Paragraph paragraph = dao.getParagraphById((UUID) testData.get(SAVED_PARAGRAPH));

        //then
        assertEquals("P2", paragraph.getName());
    }

    @Test(expected = OutlineException.class)
    public void getParagraphById_should_not_return_paragraph_if_it_belongs_to_another_user() {
        //given
        Map<String, Object> testData = prepareTestData(b -> b
                .user("user1").children(b1 -> b1
                        .paragraph("P1").children(b2 -> b2
                                .paragraph("P2").saveId(SAVED_PARAGRAPH)
                                .topic("T1")
                        )
                        .topic("T2")
                )
                .user("user2").currentUser().children(b1 -> b1
                        .topic("T2")
                )
        );

        //when
        dao.getParagraphById((UUID) testData.get(SAVED_PARAGRAPH));

        //then
        Assert.fail("an exception should be thrown");
    }

    @Test
    public void getParagraphById_should_return_child_nodes_in_correct_order() {
        //given
        Map<String, Object> testData = prepareTestData(b -> b
                .user("user1").currentUser().children(b1 -> b1
                        .paragraph("P").saveId(SAVED_PARAGRAPH).children(b2 -> b2
                                .paragraph("P1")
                                .topic("T2")
                                .paragraph("P3")
                                .paragraph("P4")
                                .topic("T5")
                        )
                        .topic("T6")
                )

        );

        //when
        Paragraph paragraph = dao.getParagraphById((UUID) testData.get(SAVED_PARAGRAPH));

        //then
        assertEquals(5, paragraph.getChildNodes().size());
        assertEquals("P1", paragraph.getChildNodes().get(0).getName());
        assertEquals("T2", paragraph.getChildNodes().get(1).getName());
        assertEquals("P3", paragraph.getChildNodes().get(2).getName());
        assertEquals("P4", paragraph.getChildNodes().get(3).getName());
        assertEquals("T5", paragraph.getChildNodes().get(4).getName());
    }

    @Test
    public void getTopicById_should_return_topic_by_id() {
        //given
        Map<String, Object> testData = prepareTestData(b -> b
                .user("user1").currentUser().children(b1 -> b1
                        .paragraph("P1").children(b2 -> b2
                                .paragraph("P2")
                                .topic("T1").saveId(SAVED_TOPIC)
                        )
                        .topic("T2")
                )

        );

        //when
        Topic topic = dao.getTopicById((UUID) testData.get(SAVED_TOPIC));

        //then
        assertEquals("T1", topic.getName());
    }

    @Test(expected = OutlineException.class)
    public void getTopicById_should_not_return_topic_if_it_belongs_to_another_user() {
        //given
        Map<String, Object> testData = prepareTestData(b -> b
                .user("user1").children(b1 -> b1
                        .paragraph("P1").children(b2 -> b2
                                .paragraph("P2")
                                .topic("T1").saveId(SAVED_TOPIC)
                        )
                        .topic("T2")
                )
                .user("user2").currentUser().children(b1 -> b1
                        .topic("T2")
                )
        );

        //when
        dao.getTopicById((UUID) testData.get(SAVED_TOPIC));

        //then
        Assert.fail("an exception should be thrown");
    }


    @Test
    public void getTopicById_should_load_all_parent_paragraphs() {
        //given
        Map<String, Object> testData = prepareTestData(b -> b
                .user("user1").currentUser().children(b1 -> b1
                        .paragraph("P1").children(b2 -> b2
                                .paragraph("P2").children(b3 -> b3
                                        .paragraph("P3").children(b4 -> b4
                                                .topic("T1").saveId(SAVED_TOPIC)
                                        )
                                )
                        )
                )

        );

        //when
        Topic topic = dao.getTopicById((UUID) testData.get(SAVED_TOPIC));

        //then
        assertEquals("P3", topic.getParentNode().getName());
        assertEquals("P2", topic.getParentNode().getParentNode().getName());
        assertEquals("P1", topic.getParentNode().getParentNode().getParentNode().getName());
    }


    @Test
    public void nextSibling_should_load_correct_sibling_to_the_right() {
        //given
        Map<String, Object> testData = prepareTestData(b -> b
                .user("user1").currentUser().children(b1 -> b1
                        .paragraph("P1").children(b2 -> b2
                                .paragraph("P2")
                                .topic("T1").saveId(SAVED_TOPIC)
                                .paragraph("P3")
                        )
                        .topic("T2")
                )

        );

        //when
        Optional<Paragraph> sibling = (Optional<Paragraph>) dao.nextSibling(
                (UUID) testData.get(SAVED_TOPIC),
                true
        );

        //then
        assertEquals("P3", sibling.get().getName());
    }

    @Test
    public void nextSibling_should_load_correct_sibling_to_the_left() {
        //given
        Map<String, Object> testData = prepareTestData(b -> b
                .user("user1").currentUser().children(b1 -> b1
                        .paragraph("P1").children(b2 -> b2
                                .paragraph("P2")
                                .topic("T1").saveId(SAVED_TOPIC)
                                .paragraph("P3")
                        )
                        .topic("T2")
                )

        );

        //when
        Optional<Paragraph> sibling = (Optional<Paragraph>) dao.nextSibling(
                (UUID) testData.get(SAVED_TOPIC),
                false
        );

        //then
        assertEquals("P2", sibling.get().getName());
    }

    @Test
    public void nextSibling_should_return_empty_if_there_is_no_sibling_to_the_right() {
        //given
        Map<String, Object> testData = prepareTestData(b -> b
                .user("user1").currentUser().children(b1 -> b1
                        .paragraph("P1").children(b2 -> b2
                                .paragraph("P2")
                                .topic("T1").saveId(SAVED_TOPIC)
                        )
                        .topic("T2")
                )

        );

        //when
        Optional<?> sibling = dao.nextSibling(
                (UUID) testData.get(SAVED_TOPIC),
                true
        );

        //then
        assertFalse(sibling.isPresent());
    }

    @Test
    public void nextSibling_should_return_empty_if_there_is_no_sibling_to_the_left() {
        //given
        Map<String, Object> testData = prepareTestData(b -> b
                .user("user1").currentUser().children(b1 -> b1
                        .paragraph("P1").children(b2 -> b2
                                .topic("T1").saveId(SAVED_TOPIC)
                                .paragraph("P2")
                        )
                        .topic("T2")
                )

        );

        //when
        Optional<?> sibling = dao.nextSibling(
                (UUID) testData.get(SAVED_TOPIC),
                false
        );

        //then
        assertFalse(sibling.isPresent());
    }

    @Test
    public void nextSibling_it_should_be_possible_to_traverse_all_children_in_correct_order_to_the_right() {
        //given
        Map<String, Object> testData = prepareTestData(b -> b
                .user("user1").currentUser().children(b1 -> b1
                        .paragraph("P0").children(b2 -> b2
                                .paragraph("P1").saveId(SAVED_ID)
                                .topic("T2")
                                .paragraph("P3")
                                .topic("T4")
                        )
                        .topic("T2")
                )

        );

        //when
        List<Node> children = new LinkedList<>();
        Optional<?> sibling = dao.nextSibling((UUID) testData.get(SAVED_ID), true);
        while (sibling.isPresent()) {
            Node node = (Node) sibling.get();
            children.add(node);
            sibling = dao.nextSibling(node.getId(), true);
        }

        //then
        List<String> childrenNames = map(children, Node::getName);
        assertEquals(3, childrenNames.size());
        assertEquals("T2", childrenNames.get(0));
        assertEquals("P3", childrenNames.get(1));
        assertEquals("T4", childrenNames.get(2));
    }

    @Test
    public void nextSibling_it_should_be_possible_to_traverse_all_children_in_correct_order_to_the_left() {
        //given
        Map<String, Object> testData = prepareTestData(b -> b
                .user("user1").currentUser().children(b1 -> b1
                        .paragraph("P0").children(b2 -> b2
                                .paragraph("P1")
                                .topic("T2")
                                .paragraph("P3")
                                .topic("T4").saveId(SAVED_ID)
                        )
                        .topic("T2")
                )

        );

        //when
        List<Node> children = new LinkedList<>();
        Optional<?> sibling = dao.nextSibling((UUID) testData.get(SAVED_ID), false);
        while (sibling.isPresent()) {
            Node node = (Node) sibling.get();
            children.add(node);
            sibling = dao.nextSibling(node.getId(), false);
        }

        //then
        List<String> childrenNames = map(children, Node::getName);
        assertEquals(3, childrenNames.size());
        assertEquals("P3", childrenNames.get(0));
        assertEquals("T2", childrenNames.get(1));
        assertEquals("P1", childrenNames.get(2));
    }

    @Test
    public void createParagraph_should_create_root_paragraph() {
        //given
        Map<String, Object> testData = prepareTestData(b -> b
                .user("user1").currentUser().children(b1 -> b1
                        .topic("T1")
                        .paragraph("P2")
                )

        );

        //when
        dao.createParagraph(null, "P3");

        //then
        List<Node> children = dao.getRootNodes();
        Assert.assertEquals(3, children.size());
        Assert.assertEquals("P2", children.get(0).getName());
        Assert.assertEquals("P3", children.get(1).getName());
        Assert.assertEquals("T1", children.get(2).getName());
    }

    @Test
    public void createParagraph_should_create_child_paragraph_and_place_it_to_the_end_of_children_list() {
        //given
        Map<String, Object> testData = prepareTestData(b -> b
                .user("user1").currentUser().children(b1 -> b1
                        .paragraph("PP").saveId(SAVED_ID).children(b2 -> b2
                                .topic("T1")
                                .paragraph("P1")
                        )
                )

        );

        //when
        dao.createParagraph((UUID) testData.get(SAVED_ID), "new-par");

        //then
        List<Node> children = dao.getParagraphById((UUID) testData.get(SAVED_ID)).getChildNodes();
        Assert.assertEquals(3, children.size());
        Assert.assertEquals("T1", children.get(0).getName());
        Assert.assertEquals("P1", children.get(1).getName());
        Assert.assertEquals("new-par", children.get(2).getName());
    }

    @Test
    public void updateParagraph_should_update_paragraph() {
        //given
        Map<String, Object> testData = prepareTestData(b -> b
                .user("user1").currentUser().children(b1 -> b1
                        .topic("T1")
                        .paragraph("P2").saveId(SAVED_ID)
                )

        );

        //when
        dao.updateParagraph((UUID) testData.get(SAVED_ID), par -> par.setName("NewName"));

        //then
        Paragraph paragraph = dao.getParagraphById((UUID) testData.get(SAVED_ID));
        Assert.assertEquals("NewName", paragraph.getName());
    }

    @Test
    public void reorderNodeChildren_should_reorder_paragraphs_and_topics() {
        //given
        Map<String, Object> saved = prepareDataForReordering();
        UUID a4 = (UUID) saved.get("A4");
        UUID b3 = (UUID) saved.get("B3");
        UUID c2 = (UUID) saved.get("C2");
        UUID d1 = (UUID) saved.get("D1");
        UUID e5 = (UUID) saved.get("E5");
        ReorderNodeChildren request = new ReorderNodeChildren();
        request.setParentId((UUID) saved.get(SAVED_ID));
        request.setChildren(Arrays.asList(d1, c2, b3, a4, e5));

        //when
        dao.reorderNodeChildren(request);

        //then
        Paragraph paragraph = dao.getParagraphById(request.getParentId());
        assertEquals(5, paragraph.getChildNodes().size());
        assertEquals("D1", paragraph.getChildNodes().get(0).getName());
        assertEquals("C2", paragraph.getChildNodes().get(1).getName());
        assertEquals("B3", paragraph.getChildNodes().get(2).getName());
        assertEquals("A4", paragraph.getChildNodes().get(3).getName());
        assertEquals("E5", paragraph.getChildNodes().get(4).getName());
    }

    @Test(expected = OutlineException.class)
    public void reorderNodeChildren_should_fail_on_mismatching_ids() {
        Map<String, Object> saved = prepareDataForReordering();
        UUID a4 = (UUID) saved.get("A4");
        UUID b3 = (UUID) saved.get("B3");
        UUID c2 = (UUID) saved.get("C2");
        UUID d1 = (UUID) saved.get("D1");
        UUID e5 = (UUID) saved.get("E5");
        ReorderNodeChildren request = new ReorderNodeChildren();
        request.setParentId((UUID) saved.get(SAVED_ID));
        request.setChildren(Arrays.asList(d1, c2, b3, e5));

        //when
        dao.reorderNodeChildren(request);

        //then
        Assert.fail("an exception should be thrown");
    }

    @Test
    public void performActionOnSelectedObjects_should_move_paragraph_when_destination_paragraph_contains_more_children_than_source_paragraph() {
        //given
        Map<String, Object> saved = prepareTestData(b -> b
                .user("user1").currentUser().children(b1 -> b1
                        .paragraph("P-src").children(b2 -> b2
                                .paragraph("P1").saveId(SAVED_ID)
                                .topic("T1")
                        )
                        .paragraph("P-dst").saveId(SAVED_PARAGRAPH_1).children(b2 -> b2
                                .paragraph("P2")
                                .paragraph("P3")
                                .topic("T2")
                                .topic("T3")
                                .topic("T4")
                        )
                )

        );
        Selection selection = new Selection();
        selection.setActionType(ActionType.MOVE);
        selection.setSelections(Arrays.asList(
                SelectionPart.builder()
                        .objectType(ObjectType.PARAGRAPH)
                        .selectedId((UUID) saved.get(SAVED_ID))
                        .build()
        ));

        //when
        dao.performActionOnSelectedObjects(selection, (UUID) saved.get(SAVED_PARAGRAPH_1));

        //then
        Paragraph paragraph = dao.getParagraphById((UUID) saved.get(SAVED_PARAGRAPH_1));
        assertEquals(6, paragraph.getChildNodes().size());
        assertEquals("P2", paragraph.getChildNodes().get(0).getName());
        assertEquals("P3", paragraph.getChildNodes().get(1).getName());
        assertEquals("T2", paragraph.getChildNodes().get(2).getName());
        assertEquals("T3", paragraph.getChildNodes().get(3).getName());
        assertEquals("T4", paragraph.getChildNodes().get(4).getName());
        assertEquals("P1", paragraph.getChildNodes().get(5).getName());
    }

    @Test
    public void performActionOnSelectedObjects_should_move_paragraph_when_destination_paragraph_contains_less_children_than_source_paragraph() {
        //given
        Map<String, Object> saved = prepareTestData(b -> b
                .user("user1").currentUser().children(b1 -> b1
                        .paragraph("P-src").children(b2 -> b2
                                .paragraph("P2")
                                .topic("T1")
                                .paragraph("P1").saveId(SAVED_ID)
                                .topic("T2")
                                .topic("T3")

                        )
                        .paragraph("P-dst").saveId(SAVED_PARAGRAPH_1).children(b2 -> b2
                                .paragraph("P3")
                                .topic("T4")
                        )
                )

        );
        Selection selection = new Selection();
        selection.setActionType(ActionType.MOVE);
        selection.setSelections(Arrays.asList(
                SelectionPart.builder()
                        .objectType(ObjectType.PARAGRAPH)
                        .selectedId((UUID) saved.get(SAVED_ID))
                        .build()
        ));

        //when
        dao.performActionOnSelectedObjects(selection, (UUID) saved.get(SAVED_PARAGRAPH_1));

        //then
        Paragraph paragraph = dao.getParagraphById((UUID) saved.get(SAVED_PARAGRAPH_1));
        assertEquals(3, paragraph.getChildNodes().size());
        assertEquals("P3", paragraph.getChildNodes().get(0).getName());
        assertEquals("T4", paragraph.getChildNodes().get(1).getName());
        assertEquals("P1", paragraph.getChildNodes().get(2).getName());
    }

    @Test
    public void performActionOnSelectedObjects_should_move_sibling_paragraphs_one_into_another() {
        //given
        Map<String, Object> saved = prepareTestData(b -> b
                .user("user1").currentUser().children(b1 -> b1
                        .paragraph("PP").saveId(SAVED_PARAGRAPH).children(b2 -> b2
                                .paragraph("P1").saveId(SAVED_PARAGRAPH_1)
                                .paragraph("P2").saveId(SAVED_PARAGRAPH_2)
                        )
                )

        );
        Selection selection = new Selection();
        selection.setActionType(ActionType.MOVE);
        selection.setSelections(Arrays.asList(
                SelectionPart.builder()
                        .objectType(ObjectType.PARAGRAPH)
                        .selectedId((UUID) saved.get(SAVED_PARAGRAPH_1))
                        .build()
        ));

        //when
        dao.performActionOnSelectedObjects(selection, (UUID) saved.get(SAVED_PARAGRAPH_2));

        //then
        Paragraph paragraph = dao.getParagraphById((UUID) saved.get(SAVED_PARAGRAPH_2));
        assertEquals(1, paragraph.getChildNodes().size());
        assertEquals("P1", paragraph.getChildNodes().get(0).getName());

        Paragraph parentParagraph = dao.getParagraphById((UUID) saved.get(SAVED_PARAGRAPH));
        assertEquals(1, parentParagraph.getChildNodes().size());
        assertEquals("P2", parentParagraph.getChildNodes().get(0).getName());
    }

    @Test(expected = OutlineException.class)
    public void performActionOnSelectedObjects_should_deny_moving_paragraph_under_itself() {
        //given
        Map<String, Object> saved = prepareTestData(b -> b
                .user("user1").currentUser().children(b1 -> b1
                        .paragraph("PP").saveId(SAVED_PARAGRAPH_1).children(b2 -> b2
                                .paragraph("P1").saveId(SAVED_PARAGRAPH_2)
                                .paragraph("P2")
                        )
                )

        );
        Selection selection = new Selection();
        selection.setActionType(ActionType.MOVE);
        selection.setSelections(Arrays.asList(
                SelectionPart.builder()
                        .objectType(ObjectType.PARAGRAPH)
                        .selectedId((UUID) saved.get(SAVED_PARAGRAPH_1))
                        .build()
        ));

        //when
        dao.performActionOnSelectedObjects(selection, (UUID) saved.get(SAVED_PARAGRAPH_2));

        //then
        Assert.fail("an exception should be thrown");
    }

    @Test
    public void performActionOnSelectedObjects_should_move_topic_when_destination_paragraph_contains_more_children_than_source_paragraph() {
        //given
        Map<String, Object> saved = prepareTestData(b -> b
                .user("user1").currentUser().children(b1 -> b1
                        .paragraph("P-src").children(b2 -> b2
                                .paragraph("P1")
                                .topic("T1").saveId(SAVED_ID)
                        )
                        .paragraph("P-dst").saveId(SAVED_PARAGRAPH_1).children(b2 -> b2
                                .paragraph("P2")
                                .paragraph("P3")
                                .topic("T2")
                                .topic("T3")
                                .topic("T4")
                        )
                )

        );
        Selection selection = new Selection();
        selection.setActionType(ActionType.MOVE);
        selection.setSelections(Arrays.asList(
                SelectionPart.builder()
                        .objectType(ObjectType.TOPIC)
                        .selectedId((UUID) saved.get(SAVED_ID))
                        .build()
        ));

        //when
        dao.performActionOnSelectedObjects(selection, (UUID) saved.get(SAVED_PARAGRAPH_1));

        //then
        Paragraph paragraph = dao.getParagraphById((UUID) saved.get(SAVED_PARAGRAPH_1));
        assertEquals(6, paragraph.getChildNodes().size());
        assertEquals("P2", paragraph.getChildNodes().get(0).getName());
        assertEquals("P3", paragraph.getChildNodes().get(1).getName());
        assertEquals("T2", paragraph.getChildNodes().get(2).getName());
        assertEquals("T3", paragraph.getChildNodes().get(3).getName());
        assertEquals("T4", paragraph.getChildNodes().get(4).getName());
        assertEquals("T1", paragraph.getChildNodes().get(5).getName());
    }

    @Test
    public void performActionOnSelectedObjects_should_move_topic_when_destination_paragraph_contains_less_children_than_source_paragraph() {
        //given
        Map<String, Object> saved = prepareTestData(b -> b
                .user("user1").currentUser().children(b1 -> b1
                        .paragraph("P-src").children(b2 -> b2
                                .paragraph("P2")
                                .topic("T1")
                                .paragraph("P1")
                                .topic("T2").saveId(SAVED_ID)
                                .topic("T3")

                        )
                        .paragraph("P-dst").saveId(SAVED_PARAGRAPH_1).children(b2 -> b2
                                .paragraph("P3")
                                .topic("T4")
                        )
                )

        );
        Selection selection = new Selection();
        selection.setActionType(ActionType.MOVE);
        selection.setSelections(Arrays.asList(
                SelectionPart.builder()
                        .objectType(ObjectType.TOPIC)
                        .selectedId((UUID) saved.get(SAVED_ID))
                        .build()
        ));

        //when
        dao.performActionOnSelectedObjects(selection, (UUID) saved.get(SAVED_PARAGRAPH_1));

        //then
        Paragraph paragraph = dao.getParagraphById((UUID) saved.get(SAVED_PARAGRAPH_1));
        assertEquals(3, paragraph.getChildNodes().size());
        assertEquals("P3", paragraph.getChildNodes().get(0).getName());
        assertEquals("T4", paragraph.getChildNodes().get(1).getName());
        assertEquals("T2", paragraph.getChildNodes().get(2).getName());
    }



    @Test
    public void performActionOnSelectedObjects_should_move_image_from_source_topic_with_only_one_image_to_destination_topic_with_no_images() {
        //given
        Map<String, Object> saved = prepareTestData(b -> b
                .user("user1").currentUser().children(b1 -> b1
                        .paragraph("PP").children(b2 -> b2
                                .topic("T1").saveId(SAVED_TOPIC1).children(b3 -> b3
                                        .image().saveId(SAVED_IMAGE)
                                )
                                .topic("T2").saveId(SAVED_TOPIC2)
                        )
                )

        );
        Selection selection = new Selection();
        selection.setActionType(ActionType.MOVE);
        selection.setSelections(Arrays.asList(
                SelectionPart.builder()
                        .objectType(ObjectType.IMAGE)
                        .selectedId((UUID) saved.get(SAVED_IMAGE))
                        .build()
        ));

        //when
        dao.performActionOnSelectedObjects(selection, (UUID) saved.get(SAVED_TOPIC2));

        //then
        Topic topic1 = dao.getTopicById((UUID) saved.get(SAVED_TOPIC1));
        assertEquals(0, topic1.getContents().size());

        Topic topic2 = dao.getTopicById((UUID) saved.get(SAVED_TOPIC2));
        assertEquals(1, topic2.getContents().size());
        assertEquals(saved.get(SAVED_IMAGE), topic2.getContents().get(0).getId());
    }

    @Test
    public void performActionOnSelectedObjects_should_move_image_from_source_topic_with_only_one_image_to_destination_topic_with_few_images() {
        //given
        Map<String, Object> saved = prepareTestData(b -> b
                .user("user1").currentUser().children(b1 -> b1
                        .paragraph("PP").children(b2 -> b2
                                .topic("T1").saveId(SAVED_TOPIC1).children(b3 -> b3
                                        .image().saveId(SAVED_IMAGE)
                                )
                                .topic("T2").saveId(SAVED_TOPIC2).children(b3 -> b3
                                        .image().saveId(SAVED_IMAGE1)
                                        .image().saveId(SAVED_IMAGE2)
                                        .image().saveId(SAVED_IMAGE3)
                                )
                        )
                )

        );
        Selection selection = new Selection();
        selection.setActionType(ActionType.MOVE);
        selection.setSelections(Arrays.asList(
                SelectionPart.builder()
                        .objectType(ObjectType.IMAGE)
                        .selectedId((UUID) saved.get(SAVED_IMAGE))
                        .build()
        ));

        //when
        dao.performActionOnSelectedObjects(selection, (UUID) saved.get(SAVED_TOPIC2));

        //then
        Topic topic1 = dao.getTopicById((UUID) saved.get(SAVED_TOPIC1));
        assertEquals(0, topic1.getContents().size());

        Topic topic2 = dao.getTopicById((UUID) saved.get(SAVED_TOPIC2));
        assertEquals(4, topic2.getContents().size());
        assertEquals(saved.get(SAVED_IMAGE1), topic2.getContents().get(0).getId());
        assertEquals(saved.get(SAVED_IMAGE2), topic2.getContents().get(1).getId());
        assertEquals(saved.get(SAVED_IMAGE3), topic2.getContents().get(2).getId());
        assertEquals(saved.get(SAVED_IMAGE), topic2.getContents().get(3).getId());
    }

    @Test
    public void performActionOnSelectedObjects_should_move_image_from_source_topic_with_few_images_to_destination_topic_with_no_images() {
        //given
        Map<String, Object> saved = prepareTestData(b -> b
                .user("user1").currentUser().children(b1 -> b1
                        .paragraph("PP").children(b2 -> b2
                                .topic("T1").saveId(SAVED_TOPIC1).children(b3 -> b3
                                        .image().saveId(SAVED_IMAGE1)
                                        .image().saveId(SAVED_IMAGE2)
                                        .image().saveId(SAVED_IMAGE3)
                                )
                                .topic("T2").saveId(SAVED_TOPIC2).children(b3 -> b3
                                )
                        )
                )

        );
        Selection selection = new Selection();
        selection.setActionType(ActionType.MOVE);
        selection.setSelections(Arrays.asList(
                SelectionPart.builder()
                        .objectType(ObjectType.IMAGE)
                        .selectedId((UUID) saved.get(SAVED_IMAGE2))
                        .build()
        ));

        //when
        dao.performActionOnSelectedObjects(selection, (UUID) saved.get(SAVED_TOPIC2));

        //then
        Topic topic1 = dao.getTopicById((UUID) saved.get(SAVED_TOPIC1));
        assertEquals(2, topic1.getContents().size());
        assertEquals(saved.get(SAVED_IMAGE1), topic1.getContents().get(0).getId());
        assertEquals(saved.get(SAVED_IMAGE3), topic1.getContents().get(1).getId());

        Topic topic2 = dao.getTopicById((UUID) saved.get(SAVED_TOPIC2));
        assertEquals(1, topic2.getContents().size());
        assertEquals(saved.get(SAVED_IMAGE2), topic2.getContents().get(0).getId());
    }

    @Test
    public void performActionOnSelectedObjects_should_move_image_from_source_topic_with_few_images_to_destination_topic_with_few_images() {
        //given
        Map<String, Object> saved = prepareTestData(b -> b
                .user("user1").currentUser().children(b1 -> b1
                        .paragraph("PP").children(b2 -> b2
                                .topic("T1").saveId(SAVED_TOPIC1).children(b3 -> b3
                                        .image().saveId(SAVED_IMAGE1)
                                        .image().saveId(SAVED_IMAGE2)
                                        .image().saveId(SAVED_IMAGE3)
                                )
                                .topic("T2").saveId(SAVED_TOPIC2).children(b3 -> b3
                                        .image().saveId(SAVED_IMAGE4)
                                        .image().saveId(SAVED_IMAGE5)
                                        .image().saveId(SAVED_IMAGE6)
                                )
                        )
                )

        );
        Selection selection = new Selection();
        selection.setActionType(ActionType.MOVE);
        selection.setSelections(Arrays.asList(
                SelectionPart.builder()
                        .objectType(ObjectType.IMAGE)
                        .selectedId((UUID) saved.get(SAVED_IMAGE1))
                        .build()
        ));

        //when
        dao.performActionOnSelectedObjects(selection, (UUID) saved.get(SAVED_TOPIC2));

        //then
        Topic topic1 = dao.getTopicById((UUID) saved.get(SAVED_TOPIC1));
        assertEquals(2, topic1.getContents().size());
        assertEquals(saved.get(SAVED_IMAGE2), topic1.getContents().get(0).getId());
        assertEquals(saved.get(SAVED_IMAGE3), topic1.getContents().get(1).getId());

        Topic topic2 = dao.getTopicById((UUID) saved.get(SAVED_TOPIC2));
        assertEquals(4, topic2.getContents().size());
        assertEquals(saved.get(SAVED_IMAGE4), topic2.getContents().get(0).getId());
        assertEquals(saved.get(SAVED_IMAGE5), topic2.getContents().get(1).getId());
        assertEquals(saved.get(SAVED_IMAGE6), topic2.getContents().get(2).getId());
        assertEquals(saved.get(SAVED_IMAGE1), topic2.getContents().get(3).getId());
    }

    @Test
    public void createTopic_should_create_new_topic() {
        //given
        Map<String, Object> saved = prepareTestData(b -> b
                .user("user1").currentUser().children(b1 -> b1
                        .paragraph("PP").saveId(SAVED_PARAGRAPH).children(b2 -> b2
                                .topic("T1")
                        )
                )

        );
        UUID imgId = dao.createImage();
        EditTopicForm form = new EditTopicForm();
        form.setParentId((UUID) saved.get(SAVED_PARAGRAPH));
        form.setName("new topic");
        form.setContent(Arrays.asList(
                ContentForForm.builder().type(TEXT).text("ttt1").build(),
                ContentForForm.builder().type(IMAGE).id(imgId).build()
        ));

        //when
        UUID newTopicId = dao.createTopic(form);

        //then
        Paragraph paragraph = dao.getParagraphById((UUID) saved.get(SAVED_PARAGRAPH));
        assertEquals(2, paragraph.getChildNodes().size());
        assertEquals("T1", paragraph.getChildNodes().get(0).getName());
        assertEquals("new topic", paragraph.getChildNodes().get(1).getName());
        Topic topic = dao.getTopicById(paragraph.getChildNodes().get(1).getId());
        assertEquals(2, topic.getContents().size());
        assertEquals("ttt1", ((Text)topic.getContents().get(0)).getText());
        assertEquals(imgId, topic.getContents().get(1).getId());
    }

    @Test
    public void createImage_should_create_new_image() {
        //given
        Map<String, Object> saved = prepareTestData(b -> b
                .user("user1").saveId(SAVED_ID).currentUser()

        );

        //when
        UUID imgId = dao.createImage();

        //then
        Image img = doInTransaction(ses -> {
            Image res = ses.load(Image.class, imgId);
            res.getOwner();
            return res;
        });
        Assert.assertEquals(saved.get(SAVED_ID), img.getOwner().getId());
    }

    @Test
    public void updateTopic_should_change_topic_name() {
        //given
        Map<String, Object> saved = prepareTestData(b -> b
                .user("user1").currentUser().children(b1 -> b1
                        .paragraph("PP").children(b2 -> b2
                                .topic("T1").saveId(SAVED_ID)
                        )
                )

        );

        EditTopicForm form = new EditTopicForm();
        form.setId((UUID) saved.get(SAVED_ID));
        form.setName("T-o-P");
        form.setContent(Collections.emptyList());

        //when
        dao.updateTopic(form);

        //then
        Topic updatedTopic = dao.getTopicById(form.getId());
        Assert.assertEquals("T-o-P", updatedTopic.getName());
        Assert.assertEquals(0, updatedTopic.getContents().size());
    }

    @Test
    public void updateTopic_should_not_change_unchanged_items() {
        //given
        Map<String, Object> saved = prepareTestData(b -> b
                .user("user1").currentUser().children(b1 -> b1
                        .paragraph("PP").children(b2 -> b2
                                .topic("T1").saveId(SAVED_TOPIC).children(b3 -> b3
                                        .image().saveId(SAVED_IMAGE)
                                        .text("tt1").saveId(SAVED_TEXT)
                                )
                        )
                )

        );
        EditTopicForm form = new EditTopicForm();
        form.setId((UUID) saved.get(SAVED_TOPIC));
        form.setName("T-o-P");
        form.setContent(Arrays.asList(
                ContentForForm.builder().type(IMAGE).id((UUID) saved.get(SAVED_IMAGE)).build(),
                ContentForForm.builder().type(TEXT).id((UUID) saved.get(SAVED_TEXT)).text("tt1").build()
        ));

        //when
        dao.updateTopic(form);

        //then
        Topic updatedTopic = dao.getTopicById((UUID) saved.get(SAVED_TOPIC));
        List<Content> contents = updatedTopic.getContents();
        Assert.assertEquals(2, contents.size());
        Assert.assertEquals(saved.get(SAVED_IMAGE), contents.get(0).getId());
        Assert.assertEquals(saved.get(SAVED_TEXT), contents.get(1).getId());
        Assert.assertEquals("tt1", ((Text)contents.get(1)).getText());
    }

    @Test
    public void updateTopic_should_change_order_of_content() {
        //given
        Map<String, Object> saved = prepareTestData(b -> b
                .user("user1").currentUser().children(b1 -> b1
                        .paragraph("PP").children(b2 -> b2
                                .topic("T1").saveId(SAVED_TOPIC).children(b3 -> b3
                                        .image().saveId(SAVED_IMAGE1)
                                        .text("tt1").saveId(SAVED_TEXT1)
                                        .text("tt2").saveId(SAVED_TEXT2)
                                        .image().saveId(SAVED_IMAGE2)
                                )
                        )
                )

        );
        EditTopicForm form = new EditTopicForm();
        form.setId((UUID) saved.get(SAVED_TOPIC));
        form.setName("T-o-P");
        form.setContent(Arrays.asList(
                ContentForForm.builder().type(TEXT).id((UUID) saved.get(SAVED_TEXT1)).text("tt1").build(),
                ContentForForm.builder().type(IMAGE).id((UUID) saved.get(SAVED_IMAGE2)).build(),
                ContentForForm.builder().type(IMAGE).id((UUID) saved.get(SAVED_IMAGE1)).build(),
                ContentForForm.builder().type(TEXT).id((UUID) saved.get(SAVED_TEXT2)).text("tt2").build()
        ));

        //when
        dao.updateTopic(form);

        //then
        Topic updatedTopic = dao.getTopicById((UUID) saved.get(SAVED_TOPIC));
        List<Content> contents = updatedTopic.getContents();
        Assert.assertEquals(4, contents.size());
        Assert.assertEquals(saved.get(SAVED_TEXT1), contents.get(0).getId());
        Assert.assertEquals("tt1", ((Text)contents.get(0)).getText());
        Assert.assertEquals(saved.get(SAVED_IMAGE2), contents.get(1).getId());
        Assert.assertEquals(saved.get(SAVED_IMAGE1), contents.get(2).getId());
        Assert.assertEquals(saved.get(SAVED_TEXT2), contents.get(3).getId());
        Assert.assertEquals("tt2", ((Text)contents.get(3)).getText());
    }

    @Test
    public void updateTopic_should_update_text_of_text_content() {
        //given
        Map<String, Object> saved = prepareTestData(b -> b
                .user("user1").currentUser().children(b1 -> b1
                        .paragraph("PP").children(b2 -> b2
                                .topic("T1").saveId(SAVED_TOPIC).children(b3 -> b3
                                        .image().saveId(SAVED_IMAGE)
                                        .text("tt1").saveId(SAVED_TEXT)
                                )
                        )
                )

        );
        EditTopicForm form = new EditTopicForm();
        form.setId((UUID) saved.get(SAVED_TOPIC));
        form.setName("T-o-P");
        form.setContent(Arrays.asList(
                ContentForForm.builder().type(IMAGE).id((UUID) saved.get(SAVED_IMAGE)).build(),
                ContentForForm.builder().type(TEXT).id((UUID) saved.get(SAVED_TEXT)).text("aa1").build()
        ));

        //when
        dao.updateTopic(form);

        //then
        Topic updatedTopic = dao.getTopicById((UUID) saved.get(SAVED_TOPIC));
        List<Content> contents = updatedTopic.getContents();
        Assert.assertEquals(2, contents.size());
        Assert.assertEquals(saved.get(SAVED_IMAGE), contents.get(0).getId());
        Assert.assertEquals(saved.get(SAVED_TEXT), contents.get(1).getId());
        Assert.assertEquals("aa1", ((Text)contents.get(1)).getText());
    }

    @Test
    public void updateTopic_should_remove_released_content_items_from_database() {
        //given
        Map<String, Object> saved = prepareTestData(b -> b
                .user("user1").currentUser().children(b1 -> b1
                        .paragraph("PP").children(b2 -> b2
                                .topic("T1").saveId(SAVED_TOPIC).children(b3 -> b3
                                        .image().saveId(SAVED_IMAGE1)
                                        .text("tt1").saveId(SAVED_TEXT1)
                                        .text("tt2").saveId(SAVED_TEXT2)
                                        .image().saveId(SAVED_IMAGE2)
                                )
                        )
                )

        );
        EditTopicForm form = new EditTopicForm();
        form.setId((UUID) saved.get(SAVED_TOPIC));
        form.setName("T-o-P");
        form.setContent(Arrays.asList(
                ContentForForm.builder().type(IMAGE).id((UUID) saved.get(SAVED_IMAGE2)).build(),
                ContentForForm.builder().type(TEXT).id((UUID) saved.get(SAVED_TEXT2)).text("tt2").build()
        ));

        //when
        dao.updateTopic(form);

        //then
        Topic updatedTopic = dao.getTopicById((UUID) saved.get(SAVED_TOPIC));
        List<Content> contents = updatedTopic.getContents();
        Assert.assertEquals(2, contents.size());
        Assert.assertEquals(saved.get(SAVED_IMAGE2), contents.get(0).getId());
        Assert.assertEquals(saved.get(SAVED_TEXT2), contents.get(1).getId());
        Assert.assertEquals("tt2", ((Text)contents.get(1)).getText());
        doInTransactionV(ses -> {
            assertNull(ses.get(Text.class, (UUID) saved.get(SAVED_TEXT1)));
            assertNull(ses.get(Text.class, (UUID) saved.get(SAVED_IMAGE1)));
        });
    }

    @Test
    public void updateTopic_should_create_new_items_in_content() {
        //given
        Map<String, Object> saved = prepareTestData(b -> b
                .user("user1").currentUser().children(b1 -> b1
                        .paragraph("PP").children(b2 -> b2
                                .topic("T1").saveId(SAVED_TOPIC).children(b3 -> b3
                                        .image().saveId(SAVED_IMAGE1)
                                        .text("tt1").saveId(SAVED_TEXT1)
                                )
                        )
                )

        );
        UUID img2Id = dao.createImage();
        EditTopicForm form = new EditTopicForm();
        form.setId((UUID) saved.get(SAVED_TOPIC));
        form.setName("T-o-P");
        form.setContent(Arrays.asList(
                ContentForForm.builder().type(TEXT).id((UUID) saved.get(SAVED_TEXT1)).text("tt1").build(),
                ContentForForm.builder().type(IMAGE).id(img2Id).build(),
                ContentForForm.builder().type(IMAGE).id((UUID) saved.get(SAVED_IMAGE1)).build(),
                ContentForForm.builder().type(TEXT).id((UUID) saved.get(SAVED_TEXT2)).text("tt2").build()
        ));

        //when
        dao.updateTopic(form);

        //then
        Topic updatedTopic = dao.getTopicById((UUID) saved.get(SAVED_TOPIC));
        List<Content> contents = updatedTopic.getContents();
        Assert.assertEquals(4, contents.size());
        Assert.assertEquals(saved.get(SAVED_TEXT1), contents.get(0).getId());
        Assert.assertEquals("tt1", ((Text)contents.get(0)).getText());
        Assert.assertEquals(img2Id, contents.get(1).getId());
        Assert.assertEquals(saved.get(SAVED_IMAGE1), contents.get(2).getId());
        Assert.assertEquals("tt2", ((Text)contents.get(3)).getText());
    }

    private Map<String, Object> prepareDataForReordering() {
        return prepareTestData(b -> b
                .user("user1").currentUser().children(b1 -> b1
                        .paragraph("PP").saveId(SAVED_ID).children(b2 -> b2
                                .paragraph("A4").saveId("A4")
                                .paragraph("B3").saveId("B3")
                                .topic("C2").saveId("C2")
                                .topic("D1").saveId("D1")
                                .topic("E5").saveId("E5")
                        )
                        .topic("T1")
                )

        );

    }
}