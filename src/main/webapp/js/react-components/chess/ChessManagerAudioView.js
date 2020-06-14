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
    const QUIZ_CARD_IDX = "QUIZ_CARD_IDX"
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
        feState = set(feState, QUIZ_CARD_IDX, 0)
        return initStartPositionListReader({feState, beState})
    }

    function initStartPositionListReader({feState, beState}) {
        const card = beState.startPosition[feState[QUIZ_CARD_IDX]];
        initListReader({
            say,
            title: {
                say: () => say(card.question),
            },
            sayFirstElem: true,
            elems: card.answer.map(ans => ({say: () => say(ans)}))
        })
        return feState
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