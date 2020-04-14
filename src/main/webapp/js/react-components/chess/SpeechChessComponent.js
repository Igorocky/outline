"use strict";

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
            onMoveSelected: (move, onDone) => {
                console.log("Selected move: " + move)
                onDone()
            },
            userInputFallback: symbols => {
                if (symbols.length) {
                    if (symbols[symbols.length - 1].symbol == ":") {
                        say("Enter control command")
                        setStage(STAGE_CONTROL_COMMAND)
                    }
                }
            }
        })
    }

    function onSymbolsChanged(symbols) {
        if (!symbols.length) {
            return symbols
        }
        if (stage == STAGE_MOVE) {
            return moveSelectorOnSymbolsChanged(symbols)
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

