package org.igye.outline2.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.Session;
import org.igye.outline2.exceptions.OutlineException;
import org.igye.outline2.manager.NodeRepository;
import org.igye.outline2.pm.Node;
import org.igye.outline2.pm.Tag;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.CollectionUtils;

import javax.persistence.EntityManager;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.UUID;

import static org.igye.outline2.OutlineUtils.nullSafeGetter;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class OutlineTestUtils {

    public static final String PARENT_NODE_ID = "PARENT_NODE_ID";
    public static final String CLAZZ = "CLAZZ";
    public static final String ORD = "ORD";
    public static final String NODE = "NODE";
    public static final String CREATED_WHEN = "CREATED_WHEN";
    public static final Calendar UTC_CALENDAR = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

    public static void saveNodeTreeToDatabase(NodeRepository nodeRepository, ObjectHolder<List<Node>> rootNodes) {
        nodeRepository.findByParentNodeId(null).forEach(nodeRepository::delete);
        rootNodes.get().forEach(nodeRepository::save);
    }

    public static void assertNodeInDatabase(JdbcTemplate jdbcTemplate, ObjectHolder<List<Node>> rootNodes) {
        rootNodes.get().forEach(rootNode -> assertNodeInDatabase(jdbcTemplate, rootNode));
    }

    public static void assertNodeInDatabase(JdbcTemplate jdbcTemplate, Node node) {
        assertNodeInDatabase(jdbcTemplate, node, 0);
    }
    public static void assertNodeInDatabase(JdbcTemplate jdbcTemplate, Node expectedNode, int ord) {
        String message = "node.getId() = " + expectedNode.getId();
        Node nodeInDatabase = loadNodeFromDb(jdbcTemplate, expectedNode.getId());
        assertEquals(message, expectedNode.getClazz(), nodeInDatabase.getClazz());
        assertEquals(message, expectedNode.getCreatedWhen(), nodeInDatabase.getCreatedWhen());

        List<Tag> expectedTags = expectedNode.getTags();
        compareTags(message, expectedTags, nodeInDatabase.getTags());

        assertEquals(
                message,
                (UUID)nullSafeGetter(expectedNode.getParentNode(), p->p.getId()),
                nullSafeGetter(nodeInDatabase.getParentNode(), p->p.getId())
        );
        if (expectedNode.getParentNode()!=null) {
            assertEquals(
                    message,
                    Integer.valueOf(ord),
                    getIntFromNodeTable(jdbcTemplate, expectedNode.getId(), ORD)
            );
        }
        List<Node> childNodes = expectedNode.getChildNodes();
        assertEquals(message, childNodes.size(), countRowsByParentIdInNodeTable(jdbcTemplate, expectedNode.getId()));
        for (int i = 0; i < childNodes.size(); i++) {
            assertNodeInDatabase(jdbcTemplate, childNodes.get(i), i);
        }
    }

    public static <T> T getValueFromTable(JdbcTemplate jdbcTemplate, String tableName, UUID id, String colName, Class<T> clazz) {
        return jdbcTemplate.queryForObject("select " + colName + " from " + tableName + " where id = ?", new Object[]{id}, clazz);
    }

    public static <T> T getValueFromNodeTable(JdbcTemplate jdbcTemplate, UUID id, String colName, Class<T> clazz) {
        return getValueFromTable(jdbcTemplate, NODE, id, colName, clazz);
    }

    public static Integer getIntFromNodeTable(JdbcTemplate jdbcTemplate, UUID id, String colName) {
        return getValueFromNodeTable(jdbcTemplate, id, colName, Integer.class);
    }

    public static int countRowsByParentIdInNodeTable(JdbcTemplate jdbcTemplate, UUID parentId) {
        return jdbcTemplate.queryForObject(
                "select count(1) from NODE n where n.PARENT_NODE_ID = ?",
                new Object[]{parentId},
                Integer.class
        );
    }

    public static List<Tag> getTagsOfNode(JdbcTemplate jdbcTemplate, UUID nodeId) {
        return jdbcTemplate.query(
                "select * from TAG where NODE_ID = ?",
                new Object[]{nodeId},
                (rs, idx) -> Tag.builder()
                        .id((UUID) rs.getObject("ID"))
                        .node(Node.builder().id((UUID) rs.getObject("NODE_ID")).build())
                        .tagId(rs.getString("TAG_ID"))
                        .value(rs.getString("VALUE"))
                        .build()
        );
    }

    public static Node loadNodeFromDb(JdbcTemplate jdbcTemplate, UUID nodeId) {
        return jdbcTemplate.queryForObject(
                "select * from NODE where ID = ?",
                new Object[]{nodeId},
                (rs, idx) -> Node.builder()
                        .id((UUID) rs.getObject("ID"))
                        .clazz(rs.getString(CLAZZ))
                        .createdWhen(rs.getTimestamp(CREATED_WHEN, UTC_CALENDAR).toInstant())
                        .tags(getTagsOfNode(jdbcTemplate, nodeId))
                        .parentNode(nullSafeGetter(
                                (UUID) rs.getObject(PARENT_NODE_ID),
                                parentId -> Node.builder().id(parentId).build()
                        ))
                        .build()
        );
    }

    public static <T> T exploreDB(EntityManager entityManager) {
        getCurrentSession(entityManager).doWork(connection -> org.h2.tools.Server.startWebServer(connection));
        return null;
    }

    private static Session getCurrentSession(EntityManager entityManager) {
        return entityManager.unwrap(Session.class);
    }

    public static String writeValueAsString(ObjectMapper objectMapper, Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new OutlineException(e);
        }
    }

    public static void assertMapsEqual(Map<String, Object> map1, Map<String, Object> map2) {
        assertEquals(map1.keySet(), map2.keySet());
        for (String key : map1.keySet()) {
            assertEquals(map1.get(key), map2.get(key));
        }
    }

    public static DoNotSerialize doNotSerialize(Object value) {
        return new DoNotSerialize(value);
    }

    public static class DoNotSerialize {
        private Object value;

        public DoNotSerialize(Object value) {
            this.value = value;
        }

        public Object getValue() {
            return value;
        }
    }

    private static void compareTags(String message, List<Tag> expectedTags, List<Tag> actualTags) {
        assertEquals(message, expectedTags.size(), actualTags.size());
        if (!CollectionUtils.isEmpty(expectedTags)) {
            expectedTags.forEach(expectedTag -> assertTrue(message, findCorrespondingTag(actualTags, expectedTag)));
            actualTags.forEach(actualTag -> assertTrue(message, findCorrespondingTag(expectedTags, actualTag)));
        }
    }

    private static boolean findCorrespondingTag(List<Tag> tags, Tag tag) {
        for (Tag tag1 : tags) {
            if (
                    Objects.equals(tag1.getNode().getId(), tag.getNode().getId())
                            && Objects.equals(tag1.getTagId(), tag.getTagId())
                            && Objects.equals(tag1.getValue(), tag.getValue())
            ) {
                return true;
            }
        }
        return false;
    }
}
