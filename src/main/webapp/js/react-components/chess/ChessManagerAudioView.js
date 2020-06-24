"use strict";

const ChessManagerAudioView = ({}) => {
    const PHASE_PUZZLE_MENU = "PHASE_PUZZLE_MENU"
    const PHASE_READ_START_POSITION = "PHASE_READ_START_POSITION"
    const PHASE_READ_HISTORY = "PHASE_READ_HISTORY"
    const PHASE_ENTER_USER_COMMAND = "PHASE_ENTER_USER_COMMAND"
    const PHASE_ENTER_DELAY = "PHASE_ENTER_DELAY"

    usePageTitle({pageTitleProvider: () => "ChessManagerAudioView", listenFor:[]})

    const {say, symbolDelay, dotDuration, dashDuration,
        openSpeechSettings, renderSettings:renderSpeechSettings, refreshStateFromSettings:refreshSpeechStateFromSettings,
        printState:printSpeechComponentState} = useSpeechComponent()

    const {init:initListReader, onSymbolsChanged:onSymbolsChangedInListReader} = useListReader()
    const {init:initTextInput, onSymbolsChanged:onSymbolsChangedInTextInput} = useMorseTextInput()

    const [beState, setBeState] = useState(null)
    const STARTED = "STARTED"
    const PHASE = "PHASE"
    const [feState, setFeState] = useState({[STARTED]: false})

    useEffect(() => {
        if (feState[STARTED] && beState.puzzleId != null) {
            setFeState(old => startNewPuzzle({feState:old, beState}))
        }
    }, [feState[STARTED]])

    const backendState = useBackendState({
        stateType: "ChessManagerAudio",
        onBackendStateCreated: backend => backend.call("getCurrentState"),
        onMessageFromBackend: chessManagerAudioDto => {
            if (chessManagerAudioDto.type == "state") {
                setBeState(chessManagerAudioDto)
                if (feState[STARTED]
                    && chessManagerAudioDto.puzzleId != null
                    && (beState == null || chessManagerAudioDto.puzzleId != beState.puzzleId)) {
                    setFeState(old => startNewPuzzle({feState:old, beState:chessManagerAudioDto}))
                }
            } else if (chessManagerAudioDto.type == "msg") {
                say(chessManagerAudioDto.msg)
            }
        }
    })

    function startNewPuzzle({feState, beState}) {
        feState = set(feState, PHASE, PHASE_PUZZLE_MENU)
        reInitListReaderForPuzzle({beState})
        return feState
    }

    function reInitListReaderForStartPosition({beState, cardIdx:cardIdxParam, readAnswer:readAnswerParam}) {
        const cardIdx = hasValue(cardIdxParam) ? cardIdxParam : 0
        const readAnswer = hasValue(readAnswerParam) ? readAnswerParam : false
        const card = beState.startPosition[cardIdx]
        initListReader({
            say,
            title: {
                say: () => say(readAnswer ? card.question : "Start position"),
            },
            sayCurrentElem: true,
            currElemIdx: readAnswer ? 0 : cardIdx,
            elems: createElemsForStartPositionListReader({beState, cardIdx, readAnswer, card})
        })
    }

    function reInitListReaderForPuzzle({beState}) {
        initListReader({
            say,
            title: {
                say: () => say("Puzzle menu"),
            },
            elems: [
                {
                    say: () => "Start position",
                    onEnter: () => {
                        setFeState(old => set(old, PHASE, PHASE_READ_START_POSITION))
                        reInitListReaderForStartPosition({beState})
                    }
                },
                {
                    say: () => "History",
                },
                {
                    say: () => "Execute command",
                    onEnter: () => {
                        setFeState(old => set(old, PHASE, PHASE_ENTER_USER_COMMAND))
                        reInitTextInputForUserCommand()
                    }
                },
            ]
        })
    }

    function reInitTextInput({title, onEnter, onEscape}) {
        initTextInput({say, title, onEnter, onEscape})
    }

    function reInitTextInputForUserCommand() {
        reInitTextInput({
            title: "Enter command",
            onEscape: () => {
                setFeState(old => set(old, PHASE, PHASE_PUZZLE_MENU))
                reInitListReaderForStartPosition({beState})
            },
            onEnter: userInput => {
                backendState.call("execChessCommand", {command: userInput.map(({sym}) => sym).join("")})
                reInitTextInputForUserCommand()
            }
        })
    }

    function createElemsForStartPositionListReader({beState, cardIdx, readAnswer, card}) {
        if (!readAnswer) {
            return beState.startPosition.map((card, idx) => ({
                say: () => say(card.question),
                onEnter: () => reInitListReaderForStartPosition({beState, cardIdx:idx, readAnswer:true}),
                onBack: () => {
                    setFeState(old => set(old, PHASE, PHASE_PUZZLE_MENU))
                    reInitListReaderForPuzzle({beState})
                }
            }))
        } else {
            return [
                {
                    say: () => say("Reading " + card.question),
                    onBack: () => reInitListReaderForStartPosition({beState, cardIdx:cardIdx, readAnswer:false})
                },
                ...card.answer.map((ans, idx) => ({
                    say: () => say(ans),
                    onEnter: idx < card.answer.length-1 ? null :
                        cardIdx >= beState.startPosition.length-1 ? null :
                            () => reInitListReaderForStartPosition({beState, cardIdx:cardIdx+1, readAnswer:true}),
                    onBack: () => reInitListReaderForStartPosition({beState, cardIdx:cardIdx, readAnswer:false})
                }))
            ]
        }
    }

    function selectOnSymbolsChangeToUse() {
        if (feState[PHASE] == PHASE_PUZZLE_MENU) {
            return onSymbolsChangedInListReader
        } else if (feState[PHASE] == PHASE_READ_START_POSITION) {
            return onSymbolsChangedInListReader
        } else if (feState[PHASE] == PHASE_READ_HISTORY) {
            return onSymbolsChangedInListReader
        } else if (feState[PHASE] == PHASE_ENTER_USER_COMMAND) {
            return onSymbolsChangedInTextInput
        } else if (feState[PHASE] == PHASE_ENTER_DELAY) {
            return onSymbolsChangedInTextInput
        }
    }

    if (feState[STARTED]) {
        const textColor = "white"
        const bgColor = "black"
        return RE.Fragment({},
            re(MorseTouchDiv, {
                dotDuration,
                dashDuration,
                symbolDelay,
                onSymbolsChange: selectOnSymbolsChangeToUse(),
                bgColor,
                textColor,
                controls: RE.Container.row.left.center({},{},
                    RE.Button({style:{color:textColor}, onClick: openSpeechSettings}, "Settings"),
                    RE.Button({style:{color:textColor}, onClick: refreshSpeechStateFromSettings}, "Reload"),
                    RE.Button({style:{color:textColor}, onClick: printSpeechComponentState}, "State"),
                )
            }),
            renderSpeechSettings()
        )
    } else {
        return RE.Button({onClick: () => setFeState(old => set(old, STARTED, true))}, "START")
    }
}