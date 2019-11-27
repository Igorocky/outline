const CHESS_COMPONENT_STAGE = {
    initialPosition: "INITIAL_POSITION",
    moves: "MOVES",
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
            renderCommandInputField()
        )
    }

    function renderRightPanel() {
        return reTabs({
            selectedTab:state.tab,
            onTabSelected:handleTabChange,
            tabs: {
                [CHESS_COMPONENT_STAGE.initialPosition]: {
                    label:"Initial position",
                    render: () => re(InitialPosition,{backend:backend, ...state.availableChessmanTypes})
                },
                [CHESS_COMPONENT_STAGE.moves]: {
                    label:"Moves",
                    render: () => re(History,{backend:backend, ...state.history})
                },
            },
        })
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