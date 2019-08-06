package org.igye.outline2.controllers;

import org.hibernate.Session;
import org.igye.outline2.manager.NodeRepository;
import org.igye.outline2.pm.Node;
import org.igye.outline2.pm.NodeClass;
import org.igye.outline2.pm.Tag;
import org.igye.outline2.pm.TagId;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.persistence.EntityManager;
import java.time.Instant;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import static org.igye.outline2.OutlineUtils.nullSafeGetter;
import static org.junit.Assert.assertEquals;

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
    public static void assertNodeInDatabase(JdbcTemplate jdbcTemplate, Node node, int ord) {
        String message = "node.getId() = " + node.getId();
        assertEquals(
                message,
                nullSafeGetter(node.getParentNode(), p->p.getId()),
                getUuidFromNodeTable(jdbcTemplate, node.getId(), PARENT_NODE_ID)
        );
        assertEquals(
                message,
                node.getClazz(),
                getNodeClassFromNodeTable(jdbcTemplate, node.getId(), CLAZZ)
        );
        assertEquals(
                message,
                node.getCreatedWhen(),
                getInstantFromNodeTable(jdbcTemplate, node.getId(), CREATED_WHEN)
        );


        List<Tag> tagsOfNodeInDatabase = getTagsOfNode(jdbcTemplate, node.getId());
        if (node.getTags()!=null) {
            List<Tag> tagsOfNode = node.getTags();
            assertEquals(message, tagsOfNode.size(), tagsOfNodeInDatabase.size());
            assertEquals(message, new HashSet<>(tagsOfNode), new HashSet<>(tagsOfNodeInDatabase));
        } else {
            assertEquals(message, 0, tagsOfNodeInDatabase.size());
        }

        if (node.getParentNode()!=null) {
            assertEquals(
                    message,
                    Integer.valueOf(ord),
                    getIntFromNodeTable(jdbcTemplate, node.getId(), ORD)
            );
        }

        List<Node> childNodes = node.getChildNodes();
        assertEquals(message,childNodes.size(), countRowsByParentIdInNodeTable(jdbcTemplate, node.getId()));
        for (int i = 0; i < childNodes.size(); i++) {
            assertNodeInDatabase(jdbcTemplate, childNodes.get(i), i);
        }
    }

    public static <T> T getValueFromTable(JdbcTemplate jdbcTemplate, String tableName, UUID id, String colName, Class<T> clazz) {
        return jdbcTemplate.queryForObject("select " + colName + " from " + tableName + " where id = ?", new Object[]{id}, clazz);
    }

    public static Instant getInstantFromTable(JdbcTemplate jdbcTemplate, String tableName, UUID id, String colName) {
        return jdbcTemplate.queryForObject(
                "select " + colName + " from " + tableName + " where id = ?",
                new Object[]{id},
                (resultSet, i) -> resultSet.getTimestamp(colName, UTC_CALENDAR).toInstant()
        );
    }

    public static <T> T getValueFromNodeTable(JdbcTemplate jdbcTemplate, UUID id, String colName, Class<T> clazz) {
        return getValueFromTable(jdbcTemplate, NODE, id, colName, clazz);
    }

    public static String getStringFromNodeTable(JdbcTemplate jdbcTemplate, UUID id, String colName) {
        return getValueFromNodeTable(jdbcTemplate, id, colName, String.class);
    }

    public static NodeClass getNodeClassFromNodeTable(JdbcTemplate jdbcTemplate, UUID id, String colName) {
        return getValueFromNodeTable(jdbcTemplate, id, colName, NodeClass.class);
    }

    public static Integer getIntFromNodeTable(JdbcTemplate jdbcTemplate, UUID id, String colName) {
        return getValueFromNodeTable(jdbcTemplate, id, colName, Integer.class);
    }

    public static UUID getUuidFromNodeTable(JdbcTemplate jdbcTemplate, UUID id, String colName) {
        return getValueFromNodeTable(jdbcTemplate, id, colName, UUID.class);
    }

    public static Instant getInstantFromNodeTable(JdbcTemplate jdbcTemplate, UUID id, String colName) {
        return getInstantFromTable(jdbcTemplate, NODE, id, colName);
    }

    public static int countRowsByParentIdInNodeTable(JdbcTemplate jdbcTemplate, UUID parentId) {
        return jdbcTemplate.queryForObject("select count(1) from NODE n where n.PARENT_NODE_ID = ?", new Object[]{parentId}, Integer.class);
    }

    public static List<Tag> getTagsOfNode(JdbcTemplate jdbcTemplate, UUID nodeId) {
        return jdbcTemplate.query(
                "select * from TAG where NODE_ID = ?",
                new Object[]{nodeId},
                (rs, idx) -> Tag.builder()
                        .tagId(TagId.fromString(rs.getString("TAG_ID")))
                        .ref(nullSafeGetter(
                                rs.getString("REF"),
                                str -> UUID.fromString(str),
                                id -> Node.builder().id(id).build()
                        ))
                        .value(rs.getString("VALUE"))
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
}
