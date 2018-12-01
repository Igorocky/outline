package org.igye.outline.htmlforms;

import org.junit.Test;

import static org.junit.Assert.*;

public class LearnTextDataTest {
    @Test
    public void getIndicesToHide_should_produce_result_of_correct_length() {
        LearnTextData rnd = new LearnTextData();
        for (int i = 0; i<100; i++) {
            assertEquals(3, rnd.getIndicesToHide(9, 30, 1).size());
            assertEquals(2, rnd.getIndicesToHide(8, 30, 1).size());
            assertEquals(1, rnd.getIndicesToHide(15, 0, 1).size());
            assertEquals(73, rnd.getIndicesToHide(73, 100, 1).size());
        }
    }



}