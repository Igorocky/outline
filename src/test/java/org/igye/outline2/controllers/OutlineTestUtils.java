package org.igye.outline2.controllers;

import org.hibernate.Session;
import org.igye.outline2.manager.NodeRepository;
import org.igye.outline2.pm.ImageRef;
import org.igye.outline2.pm.Node;
import org.igye.outline2.pm.Text;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.persistence.EntityManager;
import java.time.Instant;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.igye.outline2.OutlineUtils.nullSafeGetter;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class OutlineTestUtils {

    public static final String PARENT_NODE_ID = "PARENT_NODE_ID";
    public static final String NAME = "NAME";
    public static final String ORD = "ORD";
    public static final String NODE = "NODE";
    public static final String IMAGE_REF = "IMAGE_REF";
    public static final String TEXT_TABLE = "TEXT";
    public static final String TEXT_COLUMN = "TEXT";
    public static final String ICON_ID = "ICON_ID";
    public static final String CREATED_WHEN = "CREATED_WHEN";
    public static final String IMAGE_ID = "IMAGE_ID";
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
                node.getName(),
                getStringFromNodeTable(jdbcTemplate, node.getId(), NAME)
        );
        if (node.getParentNode()!=null) {
            assertEquals(
                    message,
                    Integer.valueOf(ord),
                    getIntFromNodeTable(jdbcTemplate, node.getId(), ORD)
            );
        }
        assertEquals(
                message,
                nullSafeGetter(node.getIcon(), i -> i.getId()),
                getUuidFromNodeTable(jdbcTemplate, node.getId(), ICON_ID)
        );
        assertEquals(
                message,
                node.getCreatedWhen(),
                getInstantFromNodeTable(jdbcTemplate, node.getId(), CREATED_WHEN)
        );

        if (node instanceof ImageRef) {
            assertEquals(
                    message,
                    nullSafeGetter(((ImageRef)node).getImage(), i->i.getId()),
                    getUuidFromImageRefTable(jdbcTemplate, node.getId(), IMAGE_ID)
            );
        } else if (node instanceof Text) {
            assertEquals(
                    message,
                    ((Text)node).getText(),
                    getStringFromTextTable(jdbcTemplate, node.getId(), TEXT_COLUMN)
            );
        }

        if (node.getChildNodes().isEmpty()) {
            assertEquals(message,0, countRowsByParentIdInImageTable(jdbcTemplate, node.getId()));
            assertEquals(message,0, countRowsByParentIdInTextTable(jdbcTemplate, node.getId()));
        } else {
            Map<Class<? extends Node>, Integer> counts = node.getChildNodes().stream().map(n -> n.getClass()).collect(Collectors.toMap(
                    cn -> cn, cn -> 1, (l,r) -> l+r
            ));
            HashSet<Class<? extends Node>> allKeys = new HashSet<>(counts.keySet());
            allKeys.remove(Node.class);
            allKeys.remove(Text.class);
            allKeys.remove(ImageRef.class);
            assertTrue(allKeys.isEmpty());
            int countRowsByParentIdInTextTable = countRowsByParentIdInTextTable(jdbcTemplate, node.getId());
            assertEquals(
                    message,
                    counts.get(Text.class) == null ? 0 : counts.get(Text.class).intValue(),
                    countRowsByParentIdInTextTable
            );
            int countRowsByParentIdInImageTable = countRowsByParentIdInImageTable(jdbcTemplate, node.getId());
            assertEquals(
                    message,
                    counts.get(ImageRef.class) == null ? 0 : counts.get(ImageRef.class).intValue(),
                    countRowsByParentIdInImageTable
            );
            assertEquals(
                    message,
                    counts.get(Node.class) == null ? 0 : counts.get(Node.class).intValue(),
                    countRowsByParentIdInNodeTable(jdbcTemplate, node.getId())
                                    - countRowsByParentIdInTextTable
                                    - countRowsByParentIdInImageTable
            );
            List<Node> childNodes = node.getChildNodes();
            for (int i = 0; i < childNodes.size(); i++) {
                assertNodeInDatabase(jdbcTemplate, childNodes.get(i), i);
            }
        }
    }

    public static int countRowsInTable(JdbcTemplate jdbcTemplate, String tableName, UUID id) {
        return jdbcTemplate.queryForObject("select count(1) from " + tableName + " where id = ?", new Object[]{id}, Integer.class);
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

    public static Integer getIntFromNodeTable(JdbcTemplate jdbcTemplate, UUID id, String colName) {
        return getValueFromNodeTable(jdbcTemplate, id, colName, Integer.class);
    }

    public static UUID getUuidFromNodeTable(JdbcTemplate jdbcTemplate, UUID id, String colName) {
        return getValueFromNodeTable(jdbcTemplate, id, colName, UUID.class);
    }

    public static Instant getInstantFromNodeTable(JdbcTemplate jdbcTemplate, UUID id, String colName) {
        return getInstantFromTable(jdbcTemplate, NODE, id, colName);
    }

    public static UUID getUuidFromImageRefTable(JdbcTemplate jdbcTemplate, UUID id, String colName) {
        return getValueFromTable(jdbcTemplate, IMAGE_REF, id, colName, UUID.class);
    }

    public static int countRowsByParentIdInNodeTable(JdbcTemplate jdbcTemplate, UUID parentId) {
        return jdbcTemplate.queryForObject("select count(1) from NODE n where n.PARENT_NODE_ID = ?", new Object[]{parentId}, Integer.class);
    }

    public static int countRowsByParentIdInImageTable(JdbcTemplate jdbcTemplate, UUID parentId) {
        return jdbcTemplate.queryForObject("select count(1) from IMAGE_REF i left join NODE n on i.ID = n.ID where n.PARENT_NODE_ID = ?", new Object[]{parentId}, Integer.class);
    }

    public static int countRowsByParentIdInTextTable(JdbcTemplate jdbcTemplate, UUID parentId) {
        return jdbcTemplate.queryForObject("select count(1) from TEXT i left join NODE n on i.ID = n.ID where n.PARENT_NODE_ID = ?", new Object[]{parentId}, Integer.class);
    }

    public static String getStringFromTextTable(JdbcTemplate jdbcTemplate, UUID id, String colName) {
        return getValueFromTable(jdbcTemplate, TEXT_TABLE, id, colName, String.class);
    }

    public static <T> T exploreDB(EntityManager entityManager) {
        getCurrentSession(entityManager).doWork(connection -> org.h2.tools.Server.startWebServer(connection));
        return null;
    }

    private static Session getCurrentSession(EntityManager entityManager) {
        return entityManager.unwrap(Session.class);
    }

    public static void notImplemented() {
        throw new NotImplementedException();
    }

}
