"use strict";

function useListReader() {
    const SAY = "SAY"
    const TITLE = "TITLE"
    const ELEMS = "ELEMS"
    const CURR_ELEM_IDX = "CURR_ELEM_IDX"
    const ON_EXIT = "ON_EXIT"
    const [state, setState] = useState(() => createState({}))

    function createState({prevState, newState}) {
        return {
            [SAY]: firstDefined(SAY, newState, prevState, str => console.log("SAY: " + str)),
            [TITLE]: firstDefined(TITLE, newState, prevState, "No title was set."),
            [ELEMS]: firstDefined(ELEMS, newState, prevState, []),
            [CURR_ELEM_IDX]: firstDefined(CURR_ELEM_IDX, newState, prevState, 0),
            [ON_EXIT]: firstDefined(ON_EXIT, newState, prevState, () => null),
        }
    }

    function init({say, title, elems, onExit}) {
        if (say !== undefined) {
            setState(old => createState({prevState:old, newState:{[SAY]:say}}))
        }
        if (title !== undefined) {
            setState(old => {
                const newState = createState({prevState:old, newState:{[TITLE]:title}})
                old[SAY](title)
                return newState
            })
        }
        if (elems !== undefined) {
            setState(old => {
                const newState = createState({prevState:old, newState:{[ELEMS]:elems, [CURR_ELEM_IDX]:0}})
                elems[0]()
                return newState
            })
        }
        if (onExit !== undefined) {
            setState(old => createState({prevState:old, newState:{[ON_EXIT]:onExit}}))
        }
    }

    function onSymbolsChanged(symbols) {
        if (symbols.length) {
            const last = symbols[symbols.length-1]
            if (last.codeInfo.sym == "N") {
                const currElemIdx = state[CURR_ELEM_IDX]
                if (state[ELEMS].length-1 <= currElemIdx) {
                    state[SAY]("No more elements to read to the right.")
                } else {
                    const newCurrElemIdx = currElemIdx+1
                    setState(old => createState({prevState:old, newState:{[CURR_ELEM_IDX]: newCurrElemIdx}}))
                    state[ELEMS][newCurrElemIdx]()
                }
                return [last]
            } else if (last.codeInfo.sym == "P") {
                const currElemIdx = state[CURR_ELEM_IDX]
                if (currElemIdx <= 0) {
                    state[SAY]("No more elements to read to the left.")
                } else {
                    const newCurrElemIdx = currElemIdx-1
                    setState(old => createState({prevState:old, newState:{[CURR_ELEM_IDX]: newCurrElemIdx}}))
                    state[ELEMS][newCurrElemIdx]()
                }
                return [last]
            } else if (last.codeInfo.sym == "R") {
                state[ELEMS][state[CURR_ELEM_IDX]]()
                return [last]
            } else if (last.codeInfo.sym == "?") {
                state[SAY](state[TITLE])
                return [last]
            } else if (last.codeInfo.sym == "end") {
                state[ON_EXIT]()
                return [last]
            }
            state[SAY]("Unexpected command: " + last.codeInfo.word)
            return [last]
        }
        return symbols
    }

    return {init, onSymbolsChanged}
}

function useSpeechComponent() {
    const LOCAL_STORAGE_KEY = "MorseChessComponent.SpeechSettings"
    const VOICE_URI = "VOICE_URI"
    const VOICE_OBJ = "VOICE_OBJ"
    const RATE = "RATE"
    const PITCH = "PITCH"
    const VOLUME = "VOLUME"
    const SYMBOL_DELAY = "SYMBOL_DELAY"
    const DOT_DURATION = "DOT_DURATION"

    const ATTRS_TO_SAVE_TO_LOC_STORAGE = [VOICE_URI, RATE, PITCH, VOLUME, SYMBOL_DELAY, DOT_DURATION]

    const [state, setState] = useState(() => createState({}))
    const [settings, setSettings] = useState(null)

    useEffect(
        () => updateStateFromSettings(readSettingsFromLocalStorage(
                {localStorageKey: LOCAL_STORAGE_KEY, attrsToRead: ATTRS_TO_SAVE_TO_LOC_STORAGE}
        )), []
    )

    useEffect(() => {
        window.speechSynthesis.onvoiceschanged = () => setState(old => createState({prevState:old}))
    }, [])

    function createState({prevState, newState}) {
        const voiceUri = firstDefined(VOICE_URI, newState, prevState)
        return {
            [VOICE_URI]: voiceUri,
            [VOICE_OBJ]: getVoiceObj(voiceUri),
            [RATE]: firstDefined(RATE, newState, prevState, 1),
            [PITCH]: firstDefined(PITCH, newState, prevState, 1),
            [VOLUME]: firstDefined(VOLUME, newState, prevState, 1),
            [SYMBOL_DELAY]:firstDefined(SYMBOL_DELAY, newState, prevState, 350),
            [DOT_DURATION]:firstDefined(DOT_DURATION, newState, prevState, 150),
        }
    }

    function say(text, onend) {
        const msg = new SpeechSynthesisUtterance()
        msg.voice = state[VOICE_OBJ]
        msg.rate = state[RATE]
        msg.pitch = state[PITCH]
        msg.volume = state[VOLUME]
        msg.text = text
        msg.lang = "en"
        msg.onend = onend
        speechSynthesis.speak(msg);
    }

    function getVoiceObj(voiceUri) {
        const voices = window.speechSynthesis.getVoices()
        if (voices.length) {
            return voices.find(v => voiceUri && v.voiceURI == voiceUri || !voiceUri && v.default)
        }
    }

    function updateStateFromSettings(settings) {
        setState(old => createState({prevState:old, newState: settings}))
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

    return {say, renderSettings,
        symbolDelay: state[SYMBOL_DELAY], dotDuration: state[DOT_DURATION],
        openSpeechSettings: () => openCloseSettingsDialog(true)
}
}

const SpeechChessComponent = ({state}) => {
    const {say, renderSettings: renderSpeechSettings, symbolDelay, dotDuration, openSpeechSettings} = useSpeechComponent()
    const {init: initListReader, onSymbolsChanged: listReaderOnSymbolsChanged} = useListReader()

    const STAGE_MOVE = "STAGE_MOVE"
    const STAGE_CONTROL_COMMAND = "STAGE_CONTROL_COMMAND"
    const STAGE_READ_INITIAL_POSITION = "STAGE_READ_INITIAL_POSITION"
    const [stage, setStage] = useState(STAGE_MOVE)

    function onSymbolsChanged(symbols) {
        if (symbols.length) {
            if (stage == STAGE_MOVE) {
                const last = symbols[symbols.length-1]
                if (last.codeInfo.sym == ":") {
                    say("Enter control command")
                    setStage(STAGE_CONTROL_COMMAND)
                }
                return [last]
            } else if (stage == STAGE_CONTROL_COMMAND) {
                const last = symbols[symbols.length-1]
                if (last.codeInfo.sym == "P") {
                    setStage(STAGE_READ_INITIAL_POSITION)
                    initListReader({
                        say, title: "Reading initial position.", elems: [
                            () => say("The first elem"),
                            () => say("The second elem"),
                        ],
                        onExit: () => {
                            setStage(STAGE_MOVE)
                            say("Enter next move")
                        }
                    })
                }
                return [last]
            } else if (stage == STAGE_READ_INITIAL_POSITION) {
                return listReaderOnSymbolsChanged(symbols)
            }
        }
        return symbols
    }

    const textColor = "black"

    return RE.Fragment({},
        re(MorseTouchDiv, {
            dotDuration,
            symbolDelay,
            onSymbolsChange: onSymbolsChanged,
            bgColor:"white",
            textColor,
            controls: RE.Container.row.left.center({},{},
                RE.Button({style:{color:textColor}, onClick: openSpeechSettings}, "Settings")
            )
        }),
        renderSpeechSettings()
    )
}

