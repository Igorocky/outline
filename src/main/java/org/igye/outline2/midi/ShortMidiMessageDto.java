package org.igye.outline2.midi;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShortMidiMessageDto {
    private long tick;
    private int track;
    private int channel;
    private int key;
    private boolean isNoteOn;
}
