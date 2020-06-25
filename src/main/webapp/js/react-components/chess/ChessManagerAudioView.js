"use strict";

const ChessManagerAudioView = ({}) => {
    const PHASES = {
        PUZZLE_MENU: "PUZZLE_MENU",
        READ_START_POSITION: "READ_START_POSITION",
        READ_HISTORY: "READ_HISTORY",
        ENTER_USER_COMMAND: "ENTER_USER_COMMAND",
        ENTER_DELAY: "ENTER_DELAY",
    }

    const FE_STATE = {
        STARTED: "STARTED",
        PHASE: "PHASE",
    }

    usePageTitle({pageTitleProvider: () => "ChessManagerAudioView", listenFor:[]})

    const {say, symbolDelay, dotDuration, dashDuration,
        openSpeechSettings, renderSettings:renderSpeechSettings, refreshStateFromSettings:refreshSpeechStateFromSettings,
        printState:printSpeechComponentState} = useSpeechComponent()
    const {init:initListReader, onSymbolsChanged:onSymbolsChangedInListReader} = useListReader()
    const {init:initTextInput, onSymbolsChanged:onSymbolsChangedInTextInput} = useMorseTextInput()

    const [beState, setBeState] = useState(null)
    const prevHistory = usePrevious(getHistory())
    const [feState, setFeState] = useState({[FE_STATE.STARTED]: false})

    function getHistory() {
        return historyToAudioList(beState?(beState.history?beState.history:[]):[])
    }

    // const history = [
    //     {
    //         "feMoveNumber": 1,
    //         "whitesMove": "...",
    //         "whitesMoveSelected": false,
    //         "blacksMove": "Ra3",
    //         "blacksMoveSelected": false
    //     },
    //     {
    //         "feMoveNumber": 2,
    //         "whitesMove": "Re1",
    //         "whitesMoveSelected": false,
    //         "blacksMove": "a1=Q",
    //         "blacksMoveSelected": false
    //     },
    //     {
    //         "feMoveNumber": 3,
    //         "whitesMove": "Rxa1",
    //         "whitesMoveSelected": false,
    //         "blacksMove": "Re3#",
    //         "blacksMoveSelected": false
    //     }
    // ]



    // console.log("historyToAudioList(history)")
    // console.log(historyToAudioList(history))

    useEffect(() => {
        if (feState[FE_STATE.STARTED] && beState.puzzleId != null) {
            setFeState(old => startNewPuzzle({feState:old, beState}))
        }
    }, [feState[FE_STATE.STARTED]])

    useEffect(() => {
        const history = getHistory()
        if (prevHistory.length < history.length) {
            say("Moves history changed.", () => {
                let sayFn = () => null
                for (let i = history.length-1; i >= history.length; i--) {
                    sayFn = () => say(history[i], sayFn)
                }
                sayFn()
            })
        }
    })

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
        feState = set(feState, FE_STATE.PHASE, PHASES.PUZZLE_MENU)
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
                say: () => say(readAnswer ? card.question : "Start position cards"),
            },
            sayCurrentElem: true,
            currElemIdx: readAnswer ? 0 : cardIdx,
            elems: createElemsForStartPositionListReader({beState, cardIdx, readAnswer, card})
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

    function strToPhonetic(str) {
        const strUp = str.toUpperCase()
        const res = strUp == "+" ? "check"
            : strUp == "#" ? "mate"
                : strUp == "x" ? "takes"
                    : strUp == "=" ? "turns to"
                        : null
        if (hasValue(res)) {
            return res
        } else {
            let res2 = MORSE_ARR.filter(({sym, word}) => sym == strUp).map(({sym, word}) => word)
            if (res2.length) {
                return res2[0]
            } else {
                return null
            }
        }
    }

    function moveToPhonetic(move) {
        if (move == "...") {
            return "black to move"
        } else {
            return move.split('').map(strToPhonetic).join(", ")
        }
    }

    function flattenHistory(history) {
        return flatMap(history, ({whitesMove, blacksMove}) => [whitesMove, ...[hasValue(blacksMove)?blacksMove:[]]])
    }

    function historyToAudioList(history) {
        return flattenHistory(history).map(moveToPhonetic)
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