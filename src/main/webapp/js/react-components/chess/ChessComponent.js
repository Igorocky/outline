const CHESS_COMPONENT_STAGE = {
    initialPosition: "INITIAL_POSITION",
    moves: "MOVES",
    exercise: "EXERCISE",
}

const ChessComponent = () => {

    const [state, setChessComponentState] = useState(null)

    const backend = useBackend({
        stateType: "chessboard",
        onBackendStateCreated: backend => backend.call("getCurrentState", {}),
        onMessageFromBackend: newState => setChessComponentState(newState)
    })

    function renderChessBoard() {
        if (state.chessBoard) {
            return re(ChessBoard,{key:"ChessBoard", backend:backend,
                ...state.chessBoard})
        } else {
            return null
        }
    }

    function renderCurrentTabContent() {
        if (state.availableChessmanTypes) {
            return re(InitialPosition,{key:"InitialPosition", backend:backend,
                ...state.availableChessmanTypes})
        } else if (state.history) {
            return re(History,{key:"History", backend:backend,
                ...state.history})
        } else {
            return null;
        }
    }

    function handleTabChange(event, newValue) {
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
            renderCommandInputField()
        )
    }

    function renderRightPanel() {
        return RE.Container.col.top.left({}, {style:{marginBottom:"5px"}},
            re(Paper, {square:true},
                re(Tabs,{value:state.tab,
                        indicatorColor:"primary",
                        textColor:"primary",
                        onChange:handleTabChange},
                    re(Tab,{label:"Initial position", value:CHESS_COMPONENT_STAGE.initialPosition}),
                    re(Tab,{label:"Moves", value:CHESS_COMPONENT_STAGE.moves}),
                    re(Tab,{label:"Practice", value:CHESS_COMPONENT_STAGE.exercise}),
                )
            ),
            renderCurrentTabContent()
        )
    }

    function renderPageContent() {
        if (state) {
            return RE.Container.row.left.top({},{style:{marginLeft:"5px",marginTop:"5px"}},
                renderLeftPanel(),
                renderRightPanel()
            )
        } else {
            return RE.LinearProgress({key:"LinearProgress",color:"secondary"})
        }
    }

    return renderPageContent()
}