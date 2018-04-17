package org.igye.outline.oldmodel;

import org.igye.outline.typeconverters.ListConverter;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ListConverterTest {
    ListConverter converter = new ListConverter();

    @Test
    public void convertToDatabaseColumn() {
        Assert.assertEquals("a;b;c", converter.convertToDatabaseColumn(Arrays.asList("a","b","c")));
        Assert.assertEquals("a", converter.convertToDatabaseColumn(Arrays.asList("a")));
        Assert.assertEquals("", converter.convertToDatabaseColumn(Collections.emptyList()));
    }

    @Test
    public void convertToEntityAttribute() {
        Assert.assertEquals(Arrays.asList("a","b","c"), converter.convertToEntityAttribute("a;b;c"));
        Assert.assertEquals(Arrays.asList("a"), converter.convertToEntityAttribute("a"));
        Assert.assertEquals(Collections.emptyList(), converter.convertToEntityAttribute(""));
        Assert.assertEquals(Collections.emptyList(), converter.convertToEntityAttribute(null));
    }

    private boolean listsAreEqual(List l1, List l2) {
        if (l1.size() != l2.size()) {
            return false;
        }
        for (int i = 0; i < l1.size(); i++) {
            if (!l1.get(i).equals(l2.get(i))) {
                return false;
            }
        }
        return true;
    }
}