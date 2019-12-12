const CHESS_COMPONENT_STAGE = {
    initialPosition: "INITIAL_POSITION",
    moves: "MOVES",
    practice: "PRACTICE_SEQUENCE",
}

const ChessComponent = ({match, showPracticeTab, showOnlyPracticeTab, onBackendCreated, onSave, onCancel}) => {
    const [state, setChessComponentState] = useState(null)
    const puzzleId = getByPath(match, ["params", "puzzleId"])

    function processBackendStateCreated(backend) {
        if (onBackendCreated) {
            onBackendCreated(backend)
        } else if (puzzleId) {
            getNode({id:puzzleId}, puzzle =>
                backend.call("loadFromPgn", {
                    pgn:getTagSingleValue(puzzle, TAG_ID.CHESS_PUZZLE_PGN),
                    tabToOpen:"PRACTICE_SEQUENCE"
                }))
        } else {
            backend.call("getCurrentState", {})
        }
    }

    const backend = useBackend({
        stateType: "chessboard",
        onBackendStateCreated: processBackendStateCreated,
        onMessageFromBackend: chessComponentResponse => {
            if (chessComponentResponse.chessComponentView) {
                setChessComponentState(chessComponentResponse.chessComponentView)
            } else if (chessComponentResponse.savePgn) {
                onSave(chessComponentResponse.savePgn)
            }
        }
    })

    function renderChessBoard() {
        if (state.chessBoard) {
            return re(ChessBoard,{key:"ChessBoard", backend:backend,
                ...state.chessBoard})
        } else {
            return null
        }
    }

    function handleTabChange(newValue) {
        backend.call("chessTabSelected", {tab:newValue})
    }

    function renderCommandInputField() {
        return re(CommandInput, {
            onExecCommand: commandStr => backend.call("execChessCommand", {command:commandStr}),
            errorMsg: state.commandErrorMsg, responseMsg: state.commandResponseMsg
        })
    }

    function renderLeftPanel() {
        return RE.Container.col.top.left({},{style:{marginBottom:"5px"}},
            renderChessBoard(),
            state.tab==CHESS_COMPONENT_STAGE.moves?renderCommandInputField():null
        )
    }

    function renderRightPanel() {
        const tabs = {}
        if (_.size(getByPath(state, ["history", "rows"], [])) == 0 && !showOnlyPracticeTab) {
            tabs[CHESS_COMPONENT_STAGE.initialPosition] = {
                label:"Initial position",
                render: () => re(InitialPosition,{backend:backend, ...state.availableChessmanTypes})
            }
        }
        if (!showOnlyPracticeTab) {
            tabs[CHESS_COMPONENT_STAGE.moves] = {
                label:"Moves",
                render: () => re(History,{backend:backend, ...state.history})
            }
        }
        if (showPracticeTab) {
            tabs[CHESS_COMPONENT_STAGE.practice] = {
                label:"Practice",
                render: () => re(SequencePractice,{backend:backend, ...state.practiseState})
            }
        }
        return reTabs({selectedTab:state.tab, onTabSelected:handleTabChange, tabs:tabs})
    }

    function renderSaveCancelButtons() {
        return RE.Container.row.right.top({},{style:{marginRight: "10px"}},
            onSave?RE.Button({color:"primary", variant:"contained",
                onClick: ()=>backend.call("getPgnToSave", {})}, "Save"):null,
            onCancel?RE.Button({variant:"contained", onClick:onCancel}, "Cancel"):null,
        )
    }

    function renderPageContent() {
        if (state) {
            return RE.Container.col.top.left({},{},
                RE.Container.row.left.top({},{style:{marginLeft:"5px",marginTop:"5px"}},
                    renderLeftPanel(),
                    renderRightPanel()
                ),
                renderSaveCancelButtons()
            )
        } else {
            return RE.CircularProgress({key:"LinearProgress",color:"primary"})
        }
    }

    return renderPageContent()
}