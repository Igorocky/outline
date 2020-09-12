"use strict";

function useMorseComponent() {
    const LOCAL_STORAGE_KEY = "MorseComponent.settings"
    const s = {
        INPUT_DOT_DURATION: 'INPUT_DOT_DURATION',
        INPUT_DASH_DURATION: 'INPUT_DASH_DURATION',
        INPUT_SYMBOL_DELAY: 'INPUT_SYMBOL_DELAY',
        OUTPUT_DOT_DURATION: 'OUTPUT_DOT_DURATION',
        OUTPUT_DASH_DURATION: 'OUTPUT_DASH_DURATION',
        OUTPUT_SYMBOL_DELAY: 'OUTPUT_SYMBOL_DELAY',
    }
    const ATTRS_TO_SAVE_TO_LOC_STORAGE = [
        s.INPUT_DOT_DURATION,
        s.INPUT_DASH_DURATION,
        s.INPUT_SYMBOL_DELAY,
        s.OUTPUT_DOT_DURATION,
        s.OUTPUT_DASH_DURATION,
        s.OUTPUT_SYMBOL_DELAY,
    ]

    const [state, setState] = useState(() => createState({}))
    const [settings, setSettings] = useState(null)

    useEffect(refreshStateFromSettings, [])

    function createState({prevState, params}) {
        return {
            [s.INPUT_SYMBOL_DELAY]:firstDefined(s.INPUT_SYMBOL_DELAY, params, prevState, 350),
            [s.INPUT_DOT_DURATION]:firstDefined(s.INPUT_DOT_DURATION, params, prevState, 150),
            [s.INPUT_DASH_DURATION]:firstDefined(s.INPUT_DASH_DURATION, params, prevState, 700),
        }
    }

    function refreshStateFromSettings() {
        updateStateFromSettings(readSettingsFromLocalStorage(
            {localStorageKey: LOCAL_STORAGE_KEY, attrsToRead: ATTRS_TO_SAVE_TO_LOC_STORAGE}
        ))
    }

    function say(text, onEnd) {
        // console.log("say: " + text)
        const msg = new SpeechSynthesisUtterance()
        msg.voice = state[VOICE_OBJ]
        msg.rate = state[RATE]
        msg.pitch = state[PITCH]
        msg.volume = state[VOLUME]
        msg.text = text
        msg.lang = "en"
        msg.onend = onEnd
        speechSynthesis.speak(msg);
    }

    function getVoiceObj(voiceUri) {
        const voices = window.speechSynthesis.getVoices()
        if (voices.length) {
            return voices.find(v => voiceUri && v.voiceURI == voiceUri || !voiceUri && v.default)
        }
    }

    function updateStateFromSettings(settings) {
        setState(old => createState({prevState:old, params: settings}))
    }

    function saveSettings() {
        saveSettingsToLocalStorage(
            {settings:settings, attrsToSave:ATTRS_TO_SAVE_TO_LOC_STORAGE, localStorageKey: LOCAL_STORAGE_KEY}
        )
    }

    function openCloseSettingsDialog(opened) {
        if (opened) {
            setSettings({...state})
        } else {
            setSettings(null)
        }
    }

    function renderSettings() {
        if (settings) {
            return RE.Dialog({fullScreen:true, open:true},
                RE.AppBar({},
                    RE.Toolbar({},
                        RE.Button({
                                edge:"start",
                                variant:"contained",
                                onClick: () => openCloseSettingsDialog(false),
                                style: {marginRight: "20px"}},
                            "Close"
                        ),
                        RE.Button({
                                variant:"contained",
                                onClick: () => {
                                    updateStateFromSettings(settings)
                                    saveSettings()
                                    openCloseSettingsDialog(false)
                                },
                            },
                            "Save"
                        ),
                    )
                ),
                RE.table({style:{marginTop:"80px"}},
                    RE.tbody({},
                        RE.tr({},
                            RE.td({},"Dot duration"),
                            RE.td({},
                                RE.Container.col.top.left({},{},
                                    settings[DOT_DURATION],
                                    renderSlider({min:50, max:500, step: 25, value:settings[DOT_DURATION],
                                        setValue: newValue => setSettings(old => set(old, DOT_DURATION, newValue))})
                                )
                            ),
                        ),
                        RE.tr({},
                            RE.td({},"Dash duration"),
                            RE.td({},
                                RE.Container.col.top.left({},{},
                                    settings[DASH_DURATION],
                                    renderSlider({min:500, max:4000, step: 100, value:settings[DASH_DURATION],
                                        setValue: newValue => setSettings(old => set(old, DASH_DURATION, newValue))})
                                )
                            ),
                        ),
                        RE.tr({},
                            RE.td({},"Symbol delay"),
                            RE.td({},
                                RE.Container.col.top.left({},{},
                                    settings[SYMBOL_DELAY],
                                    renderSlider({min:50, max:2000, step: 50, value:settings[SYMBOL_DELAY],
                                        setValue: newValue => setSettings(old => set(old, SYMBOL_DELAY, newValue))})
                                )
                            ),
                        ),
                        RE.tr({},
                            RE.td({},"Voice"),
                            RE.td({},
                                RE.Select({
                                        value:settings[VOICE_URI]?settings[VOICE_URI]:"Undefined",
                                        onChange: event => {
                                            const newValue = event.target.value;
                                            setSettings(old => set(old, VOICE_URI, newValue))
                                        },
                                    },
                                    window.speechSynthesis.getVoices().map(voice => RE.MenuItem(
                                        {key: voice.voiceURI, value:voice.voiceURI, },
                                        voice.name
                                    ))
                                )
                            ),
                        ),
                        RE.tr({},
                            RE.td({},"Rate"),
                            RE.td({},
                                RE.Container.col.top.left({},{},
                                    settings[RATE],
                                    renderSlider({min:0.1, max:10, step: 0.1, value:settings[RATE],
                                        setValue: newValue => setSettings(old => set(old, RATE, newValue))})
                                )
                            ),
                        ),
                        RE.tr({},
                            RE.td({},"Pitch"),
                            RE.td({},
                                RE.Container.col.top.left({},{},
                                    settings[PITCH],
                                    renderSlider({min:0, max:2, step: 0.1, value:settings[PITCH],
                                        setValue: newValue => setSettings(old => set(old, PITCH, newValue))})
                                )
                            ),
                        ),
                        RE.tr({},
                            RE.td({},"Volume"),
                            RE.td({},
                                RE.Container.col.top.left({},{},
                                    settings[VOLUME],
                                    renderSlider({min:0, max:1, step: 0.1, value:settings[VOLUME],
                                        setValue: newValue => setSettings(old => set(old, VOLUME, newValue))})
                                )
                            ),
                        )
                    )
                )
            )
        } else {
            return null
        }
    }

    function renderSlider({min, max, step, value, setValue}) {
        return RE.div({style:{width:"280px"}},
            RE.Slider({
                value:value,
                onChange: (event, newValue) => setValue(newValue),
                step:step,
                min:min,
                max:max
            })
        )
    }

    return {say, renderSettings, refreshStateFromSettings,
        printState: () => {
            console.log("useSpeechComponent.state")
            console.log(state)
        },
        symbolDelay: state[SYMBOL_DELAY], dotDuration: state[DOT_DURATION], dashDuration: state[DASH_DURATION],
        openSpeechSettings: () => openCloseSettingsDialog(true),
    }
}
