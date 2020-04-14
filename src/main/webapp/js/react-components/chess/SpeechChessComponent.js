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

function useSpeechMoveSelector() {
    const SAY = "SAY"
    const TITLE = "TITLE"
    const ON_MOVE_SELECTED = "ON_MOVE_SELECTED"
    const ON_UNEXPECTED_SYMBOL = "ON_UNEXPECTED_SYMBOL"

    const CHESSMAN_TYPE = "CHESSMAN_TYPE"
    const ADDITIONAL_COORD_TYPE = "ADDITIONAL_COORD_TYPE"
    const ADDITIONAL_COORD = "ADDITIONAL_COORD"
    const X_COORD = "X_COORD"
    const Y_COORD = "Y_COORD"
    const PROMOTION = "PROMOTION"

    const [state, setState] = useState(() => createState({}))

    function createState({prevState, newState}) {
        return {
            [SAY]: firstDefined(SAY, newState, prevState, str => console.log("SAY: " + str)),
            [TITLE]: firstDefined(TITLE, newState, prevState, "No title was set."),
            [ON_MOVE_SELECTED]: firstDefined(ON_MOVE_SELECTED, newState, prevState, () => null),
            [ON_UNEXPECTED_SYMBOL]: firstDefined(ON_UNEXPECTED_SYMBOL, newState, prevState, () => null),
            [CHESSMAN_TYPE]: firstDefined(CHESSMAN_TYPE, newState, prevState),
            [ADDITIONAL_COORD_TYPE]: firstDefined(ADDITIONAL_COORD_TYPE, newState, prevState),
            [ADDITIONAL_COORD]: firstDefined(ADDITIONAL_COORD, newState, prevState),
            [X_COORD]: firstDefined(X_COORD, newState, prevState),
            [Y_COORD]: firstDefined(Y_COORD, newState, prevState),
            [PROMOTION]: firstDefined(PROMOTION, newState, prevState),
        }
    }

    function init({say, title, onMoveSelected, onUnexpectedSymbol}) {
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
        if (onMoveSelected !== undefined) {
            setState(old => createState({prevState:old, newState:{[ON_MOVE_SELECTED]:onMoveSelected}}))
        }
        if (onUnexpectedSymbol !== undefined) {
            setState(old => createState({prevState:old, newState:{[ON_UNEXPECTED_SYMBOL]:onUnexpectedSymbol}}))
        }
    }

    const promotionIsNeeded = state[CHESSMAN_TYPE] == "P"
        && (state[Y_COORD] == "1" || state[Y_COORD] == "8")
        && !state[PROMOTION]
    const CHESSMAN_TYPES = {"P": "Pawn", "N": "Knight", "B": "Bishop", "R": "Rook", "Q": "Queen", "K": "King",}
    function onSymbolsChanged(symbols) {
        if (symbols.length) {
            const last = symbols[symbols.length-1]
            if (!state[CHESSMAN_TYPE] && last.symbol == "start") {
                go()
            } else if (!state[CHESSMAN_TYPE]) {
                const chessmanTypeToRead = CHESSMAN_TYPES[last.symbol]
                if (chessmanTypeToRead !== undefined) {
                    state[SAY](chessmanTypeToRead)
                    setState(old => set(old, CHESSMAN_TYPE, last.symbol))
                } else {
                    state[ON_UNEXPECTED_SYMBOL](symbols)
                }
            } else if (!state[X_COORD] && !state[ADDITIONAL_COORD] && (last.symbol == "X" || last.symbol == "Y")) {
                setState(old => set(old, ADDITIONAL_COORD_TYPE, last.symbol))
                state[SAY]("Enter additional " + last.symbol + " coordinate.")
            } else if (state[ADDITIONAL_COORD_TYPE]) {
                setState(old => set(old, ADDITIONAL_COORD, last.symbol))
                setState(old => set(old, ADDITIONAL_COORD_TYPE, null))
                state[SAY](last.codeInfo.word)
            } else if (!state[X_COORD]) {
                setState(old => set(old, X_COORD, last.symbol))
                state[SAY](last.symbol)
            } else if (!state[Y_COORD]) {
                setState(old => set(old, Y_COORD, last.symbol))
                state[SAY](last.symbol, () => {
                    if (promotionIsNeeded) {
                        state[SAY]("Enter promotion")
                    }
                })
            } else if (promotionIsNeeded) {
                const chessmanTypeToRead = CHESSMAN_TYPES[last.symbol]
                if (chessmanTypeToRead !== undefined) {
                    state[SAY](chessmanTypeToRead)
                    setState(old => set(old, PROMOTION, last.symbol))
                } else {
                    state[ON_UNEXPECTED_SYMBOL](symbols)
                }
            } else if (last.symbol == "start") {
                go()
            } else {
                state[ON_UNEXPECTED_SYMBOL](symbols)
            }
            return [last]
        }
        return symbols
    }

    function go() {
        state[ON_MOVE_SELECTED](getSelectedMove(), () => clearSelection())
    }

    function getSelectedMove() {
        if (!state[CHESSMAN_TYPE]) {
            return ""
        } else {
            const ct = state[CHESSMAN_TYPE] == "P" ? "" : state[CHESSMAN_TYPE]
            const ac = emptyStrIfNull(state[ADDITIONAL_COORD])
            const toX = emptyStrIfNull(state[X_COORD]).toLowerCase()
            const toY = emptyStrIfNull(state[Y_COORD]).toLowerCase()
            const pr = emptyStrIfNull(state[PROMOTION])
            return ct + ac + toX + toY + pr
        }
    }

    function saySelectedMove() {
        if (!state[CHESSMAN_TYPE]) {
            return "Nothing was entered."
        } else {
            const wordsToSay = [

            ]
            const ct = state[CHESSMAN_TYPE] == "P" ? "" : state[CHESSMAN_TYPE]
            const ac = emptyStrIfNull(state[ADDITIONAL_COORD])
            const toX = emptyStrIfNull(state[X_COORD]).toLowerCase()
            const toY = emptyStrIfNull(state[Y_COORD]).toLowerCase()
            const pr = emptyStrIfNull(state[PROMOTION])
            return ct + ac + toX + toY + pr
        }
    }

    function clearSelection() {
        setState(old => set(old, CHESSMAN_TYPE, null))
        setState(old => set(old, ADDITIONAL_COORD_TYPE, null))
        setState(old => set(old, ADDITIONAL_COORD, null))
        setState(old => set(old, X_COORD, null))
        setState(old => set(old, Y_COORD, null))
        setState(old => set(old, PROMOTION, null))
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
    const {init: initMoveSelector, onSymbolsChanged: moveSelectorOnSymbolsChanged} = useSpeechMoveSelector()

    const STAGE_MOVE = "STAGE_MOVE"
    const STAGE_CONTROL_COMMAND = "STAGE_CONTROL_COMMAND"
    const STAGE_READ_INITIAL_POSITION = "STAGE_READ_INITIAL_POSITION"
    const [stage, setStage] = useState(STAGE_MOVE)

    useEffect(() => initMoveSelectorInner(), [])

    function initMoveSelectorInner() {
        initMoveSelector({
            say,
            title: "Enter next move.",
            onUnexpectedSymbol: onSymbolsChanged,
            onMoveSelected: (move, onDone) => {
                console.log("Selected move: " + move)
                onDone()
            }
        })
    }

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
                            initMoveSelectorInner()
                        }
                    })
                }
                return [last]
            } else if (stage == STAGE_READ_INITIAL_POSITION) {
                return listReaderOnSymbolsChanged(symbols)
            } else {
                say("Unexpected symbol: " + last.word)
                return [last]
            }
        }
        return symbols
    }

    const textColor = "black"

    return RE.Fragment({},
        re(MorseTouchDiv, {
            dotDuration,
            symbolDelay,
            onSymbolsChange: symbols => {
                if (stage == STAGE_MOVE) {
                    return moveSelectorOnSymbolsChanged(symbols)
                } else {
                    return onSymbolsChanged(symbols)
                }
            },
            bgColor:"white",
            textColor,
            controls: RE.Container.row.left.center({},{},
                RE.Button({style:{color:textColor}, onClick: openSpeechSettings}, "Settings")
            )
        }),
        renderSpeechSettings()
    )
}

