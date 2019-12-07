package org.igye.outline2.midi;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KeyMidiEventDto {
    private int track;
    private int channel;
    private int key;
    private long tickStart;
    private long tickEnd;
}
