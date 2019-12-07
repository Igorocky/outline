package org.igye.outline2.midi;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MidiTrack {
    List<MidiChannel> channels;
}
