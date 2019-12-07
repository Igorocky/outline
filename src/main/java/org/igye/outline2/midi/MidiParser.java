package org.igye.outline2.midi;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static javax.sound.midi.ShortMessage.NOTE_OFF;
import static javax.sound.midi.ShortMessage.NOTE_ON;

public class MidiParser {
    public static void main(String[] args) throws InvalidMidiDataException, IOException {
        Map<Integer, Map<Integer, List<KeyMidiEventDto>>> trackMap = parseMidi("D:\\Music\\midi\\mod.mid");
        printDeltas(trackMap);
    }

    private static void printDeltas(Map<Integer, Map<Integer, List<KeyMidiEventDto>>> trackMap) {
        System.out.println("num of tracks = " + trackMap.size());
        for (Integer track : trackMap.keySet()) {
            System.out.print("Track " + track + ":");
            Map<Integer, List<KeyMidiEventDto>> channelMap = trackMap.get(track);
            System.out.println(" num of channels = " + channelMap.size());
            for (Integer channel : channelMap.keySet()) {
                System.out.print("    Channel " + channel + ":");
                List<KeyMidiEventDto> keyEvents = channelMap.get(channel);
                System.out.println(" num of key events = " + keyEvents.size());
                int prevKey = 0;
                for (int keyEventIdx = 0; keyEventIdx < keyEvents.size(); keyEventIdx++) {
                    int currKey = keyEvents.get(keyEventIdx).getKey();
                    if (keyEventIdx == 0) {
                        System.out.print("x");
                    } else {
                        final int delta = currKey - prevKey;
                        if (delta != 0) {
                            System.out.print(" " + (delta>0?"+":"") + delta);
                        }
                    }
                    prevKey = currKey;
                }
            }
        }
    }

    private static Map<Integer, Map<Integer, List<KeyMidiEventDto>>> parseMidi(String filePath) throws InvalidMidiDataException, IOException {
        return collectKeyEvents(readShortEvents(filePath));
    }

    private static Map<Integer,Map<Integer,List<KeyMidiEventDto>>> collectKeyEvents(
            List<ShortMidiMessageDto> simpleEvents) {
        Collections.sort(simpleEvents, Comparator.comparing(ShortMidiMessageDto::getTick));
        Map<Integer,Map<Integer,Map<Integer, ShortMidiMessageDto>>> trackMap = new HashMap<>();
        Map<Integer,Map<Integer,List<KeyMidiEventDto>>> soundEvents = new HashMap<>();
        for (ShortMidiMessageDto simpleEvent : simpleEvents) {
            ShortMidiMessageDto onEvent = remove(trackMap,
                    simpleEvent.getTrack(), simpleEvent.getChannel(), simpleEvent.getKey());
            if (onEvent == null) {
                put(trackMap, simpleEvent);
            } else {
                append(soundEvents, KeyMidiEventDto.builder()
                        .track(simpleEvent.getTrack())
                        .channel(simpleEvent.getChannel())
                        .key(simpleEvent.getKey())
                        .tickStart(onEvent.getTick())
                        .tickEnd(simpleEvent.getTick())
                        .build());
            }

        }
        return soundEvents;
    }

    private static ShortMidiMessageDto remove(Map<Integer,Map<Integer,Map<Integer, ShortMidiMessageDto>>> trackMap,
                                              Integer track,
                                              Integer channel,
                                              Integer key) {
        Map<Integer, Map<Integer, ShortMidiMessageDto>> channelMap = trackMap.get(track);
        if (channelMap != null) {
            Map<Integer, ShortMidiMessageDto> keyMap = channelMap.get(channel);
            if (keyMap != null) {
                return keyMap.remove(key);
            }
        }
        return null;
    }

    private static void put(Map<Integer,Map<Integer,Map<Integer, ShortMidiMessageDto>>> trackMap,
                                    ShortMidiMessageDto event) {
        Map<Integer, Map<Integer, ShortMidiMessageDto>> channelMap = trackMap.get(event.getTrack());
        if (channelMap == null) {
            channelMap = new HashMap<>();
            trackMap.put(event.getTrack(), channelMap);
        }

        Map<Integer, ShortMidiMessageDto> keyMap = channelMap.get(event.getChannel());
        if (keyMap == null) {
            keyMap = new HashMap<>();
            channelMap.put(event.getChannel(), keyMap);
        }

        keyMap.put(event.getKey(), event);
    }

    private static void append(Map<Integer,Map<Integer,List<KeyMidiEventDto>>> trackMap,
                        KeyMidiEventDto event) {
        Map<Integer, List<KeyMidiEventDto>> channelMap = trackMap.get(event.getTrack());
        if (channelMap == null) {
            channelMap = new HashMap<>();
            trackMap.put(event.getTrack(), channelMap);
        }

        List<KeyMidiEventDto> channel = channelMap.get(event.getChannel());
        if (channel == null) {
            channel = new ArrayList<>();
            channelMap.put(event.getChannel(), channel);
        }
        channel.add(event);
    }

    private static List<ShortMidiMessageDto> readShortEvents(String filePath) throws InvalidMidiDataException, IOException {
        List<ShortMidiMessageDto> allEvents = new ArrayList<>();
        Sequence sequence = MidiSystem.getSequence(new File(filePath));
        int trackNumber = 0;
        for (Track track : sequence.getTracks()) {
            trackNumber++;
            for (int i = 0; i < track.size(); i++) {
                MidiEvent event = track.get(i);
                MidiMessage msg = event.getMessage();
                if (msg instanceof ShortMessage) {
                    ShortMessage sMsg = (ShortMessage) msg;
                    if (sMsg.getCommand() == NOTE_ON || sMsg.getCommand() == NOTE_OFF) {
                        allEvents.add(ShortMidiMessageDto.builder()
                                .tick(event.getTick())
                                .track(trackNumber)
                                .channel(sMsg.getChannel())
                                .key(sMsg.getData1())
                                .isNoteOn(sMsg.getCommand() == NOTE_ON)
                                .build()
                        );
                    }
                }
            }
        }
        return allEvents;
    }
}
