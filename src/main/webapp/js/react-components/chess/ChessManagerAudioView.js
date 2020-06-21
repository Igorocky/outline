"use strict";

const ChessManagerAudioView = ({}) => {
    const PHASE_READ_START_POSITION = "read_start_position"

    usePageTitle({pageTitleProvider: () => "ChessManagerAudioView", listenFor:[]})

    const {say, symbolDelay, dotDuration, dashDuration,
        openSpeechSettings, renderSettings:renderSpeechSettings, refreshStateFromSettings:refreshSpeechStateFromSettings,
        printState:printSpeechComponentState} = useSpeechComponent()

    const {init:initListReader, onSymbolsChanged:onSymbolsChangedInListReader} = useListReader()

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
        feState = set(feState, PHASE, PHASE_READ_START_POSITION)
        reInitStartPositionListReader({beState, cardIdx:0, readAnswer:true})
        return feState
    }

    function reInitStartPositionListReader({beState, cardIdx, readAnswer}) {
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

    function createElemsForStartPositionListReader({beState, cardIdx, readAnswer, card}) {
        if (!readAnswer) {
            return beState.startPosition.map((card, idx) => ({
                say: () => say(card.question),
                onEnter: () => reInitStartPositionListReader({beState, cardIdx:idx, readAnswer:true})
            }))
        } else {
            return [
                {
                    say: () => say("Reading " + card.question),
                    onBack: () => reInitStartPositionListReader({beState, cardIdx:cardIdx, readAnswer:false})
                },
                ...card.answer.map((ans, idx) => ({
                    say: () => say(ans),
                    onEnter: idx < card.answer.length-1 ? null :
                        cardIdx >= beState.startPosition.length-1 ? null :
                            () => reInitStartPositionListReader({beState, cardIdx:cardIdx+1, readAnswer:true}),
                    onBack: () => reInitStartPositionListReader({beState, cardIdx:cardIdx, readAnswer:false})
                }))
            ]
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
                onSymbolsChange: onSymbolsChangedInListReader,
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