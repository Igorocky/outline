package org.igye.outline.common;

import com.google.common.collect.ImmutableList;
import org.igye.outline.htmlforms.CellType;
import org.igye.outline.htmlforms.IconInfo;
import org.igye.outline.model.Icon;
import org.igye.outline.model.Node;
import org.igye.outline.model.Paragraph;
import org.igye.outline.model.Topic;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

public class OutlineUtilsTest {
    @Test
    public void getIconsInfo_should_produce_correct_result() {
        //given
        UUID idT1 = UUID.randomUUID();
        UUID idTI1 = UUID.randomUUID();
        Topic t1 = new Topic();
        t1.setId(idT1);
        t1.setIcon(Icon.builder().id(idTI1).build());

        UUID idP1 = UUID.randomUUID();
        Paragraph p1 = new Paragraph();
        p1.setId(idP1);
        p1.setIcon(null);
        p1.setEol(true);

        UUID idP2 = UUID.randomUUID();
        UUID idPI2 = UUID.randomUUID();
        Paragraph p2 = new Paragraph();
        p2.setId(idP2);
        p2.setIcon(Icon.builder().id(idPI2).build());

        UUID idT2 = UUID.randomUUID();
        Topic t2 = new Topic();
        t2.setId(idT2);
        t2.setIcon(null);

        UUID idT3 = UUID.randomUUID();
        UUID idTI3 = UUID.randomUUID();
        Topic t3 = new Topic();
        t3.setId(idT3);
        t3.setIcon(Icon.builder().id(idTI3).build());

        ImmutableList<Node> nodes = ImmutableList.of(
                t1, p1,
                p2, t2, t3
        );

        //when
        List<List<IconInfo>> iconsInfo = OutlineUtils.getIconsInfo(nodes);

        //then
        IconInfo c00 = iconsInfo.get(0).get(0);
        IconInfo c01 = iconsInfo.get(0).get(1);
        IconInfo c02 = iconsInfo.get(0).get(2);
        IconInfo c03 = iconsInfo.get(0).get(3);
        IconInfo c10 = iconsInfo.get(1).get(0);
        IconInfo c11 = iconsInfo.get(1).get(1);
        IconInfo c12 = iconsInfo.get(1).get(2);
        IconInfo c13 = iconsInfo.get(1).get(3);
        IconInfo c20 = iconsInfo.get(2).get(0);
        IconInfo c21 = iconsInfo.get(2).get(1);
        IconInfo c22 = iconsInfo.get(2).get(2);
        IconInfo c23 = iconsInfo.get(2).get(3);

        assertEquals(CellType.EMPTY, c00.getCellType());

        assertEquals(CellType.NUMBER, c01.getCellType());
        assertEquals(1, c01.getNumber().intValue());

        assertEquals(CellType.NUMBER, c02.getCellType());
        assertEquals(2, c02.getNumber().intValue());

        assertEquals(CellType.NUMBER, c03.getCellType());
        assertEquals(3, c03.getNumber().intValue());

        assertEquals(CellType.NUMBER, c10.getCellType());
        assertEquals(1, c10.getNumber().intValue());

        assertEquals(CellType.TOPIC, c11.getCellType());
        assertEquals(idT1, c11.getNodeId());
        assertEquals(idTI1, c11.getIconId());

        assertEquals(CellType.PARAGRAPH, c12.getCellType());
        assertEquals(idP1, c12.getNodeId());
        assertNull(c12.getIconId());

        assertEquals(CellType.EMPTY, c13.getCellType());

        assertEquals(CellType.NUMBER, c20.getCellType());
        assertEquals(2, c20.getNumber().intValue());

        assertEquals(CellType.PARAGRAPH, c21.getCellType());
        assertEquals(idP2, c21.getNodeId());
        assertEquals(idPI2, c21.getIconId());

        assertEquals(CellType.TOPIC, c22.getCellType());
        assertEquals(idT2, c22.getNodeId());
        assertNull(c22.getIconId());

        assertEquals(CellType.TOPIC, c23.getCellType());
        assertEquals(idT3, c23.getNodeId());
        assertEquals(idTI3, c23.getIconId());
    }

}