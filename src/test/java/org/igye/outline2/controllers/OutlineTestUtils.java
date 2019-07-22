package org.igye.outline2.controllers;

import org.igye.outline2.pm.Image;
import org.igye.outline2.pm.Node;
import org.igye.outline2.pm.Text;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class OutlineTestUtils {

    public static final String PARENT_NODE_ID = "PARENT_NODE_ID";
    public static final String NAME = "NAME";
    public static final String ORD = "ORD";
    public static final String NODE = "NODE";
    public static final String IMAGE = "IMAGE";
    public static final String TEXT_TABLE = "TEXT";
    public static final String TEXT_COLUMN = "TEXT";
    public static final String ICON_ID = "ICON_ID";
    public static final String IMG_ID = "IMG_ID";

    public static void assertNodeInDatabase(JdbcTemplate jdbcTemplate, Node node) {
        assertEquals(
                node.getParentNode() == null ? null : node.getParentNode().getId(),
                getUuidFromNodeTable(jdbcTemplate, node.getId(), PARENT_NODE_ID)
        );
        assertEquals(
                node.getName(),
                getStringFromNodeTable(jdbcTemplate, node.getId(), NAME)
        );
        assertEquals(
                Integer.valueOf(node.getOrd()),
                getIntFromNodeTable(jdbcTemplate, node.getId(), ORD)
        );
        assertEquals(
                node.getIconId(),
                getUuidFromNodeTable(jdbcTemplate, node.getId(), ICON_ID)
        );
        // TODO: 22.07.2019 check createdWhen and updatedWhen

        if (node instanceof Image) {
            assertEquals(
                    ((Image)node).getImgId(),
                    getUuidFromImageTable(jdbcTemplate, node.getId(), IMG_ID)
            );
        } else if (node instanceof Text) {
            assertEquals(
                    ((Text)node).getText(),
                    getStringFromTextTable(jdbcTemplate, node.getId(), TEXT_COLUMN)
            );
        }

        if (node.getChildNodes().isEmpty()) {
            assertEquals(0, countRowsInImageTable(jdbcTemplate, node.getId()));
            assertEquals(0, countRowsInTextTable(jdbcTemplate, node.getId()));
        } else {
            Map<Class<? extends Node>, Integer> counts = node.getChildNodes().stream().map(n -> n.getClass()).collect(Collectors.toMap(
                    cn -> cn, cn -> 1, (l,r) -> l+r
            ));
            HashSet<Class<? extends Node>> allKeys = new HashSet<>(counts.keySet());
            allKeys.remove(Node.class);
            allKeys.remove(Text.class);
            assertTrue(allKeys.isEmpty());
            assertEquals(
                    counts.get(Image.class) == null ? 0 : counts.get(Image.class).intValue(),
                    countRowsInImageTable(jdbcTemplate, node.getId())
            );
            assertEquals(
                    counts.get(Text.class) == null ? 0 : counts.get(Text.class).intValue(),
                    countRowsInTextTable(jdbcTemplate, node.getId())
            );
            node.getChildNodes().forEach(n -> assertNodeInDatabase(jdbcTemplate, n));
        }
    }

    public static int countRowsInTable(JdbcTemplate jdbcTemplate, String tableName, UUID id) {
        return jdbcTemplate.queryForObject("select count(1) from " + tableName + " where id = ?", new Object[]{id}, Integer.class);
    }

    public static <T> T getValueFromTable(JdbcTemplate jdbcTemplate, String tableName, UUID id, String colName, Class<T> clazz) {
        return jdbcTemplate.queryForObject("select " + colName + " from " + tableName + " where id = ?", new Object[]{id}, clazz);
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

    public static UUID getUuidFromImageTable(JdbcTemplate jdbcTemplate, UUID id, String colName) {
        return getValueFromTable(jdbcTemplate, IMAGE, id, colName, UUID.class);
    }

    public static int countRowsInImageTable(JdbcTemplate jdbcTemplate, UUID id) {
        return countRowsInTable(jdbcTemplate, IMAGE, id);
    }

    public static String getStringFromTextTable(JdbcTemplate jdbcTemplate, UUID id, String colName) {
        return getValueFromTable(jdbcTemplate, TEXT_TABLE, id, colName, String.class);
    }

    public static int countRowsInTextTable(JdbcTemplate jdbcTemplate, UUID id) {
        return countRowsInTable(jdbcTemplate, TEXT_TABLE, id);
    }

}
