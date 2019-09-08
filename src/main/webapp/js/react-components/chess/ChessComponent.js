const CHESS_COMPONENT_STAGE = {
    initialPosition: "INITIAL_POSITION",
    moves: "MOVES",
    exercise: "EXERCISE",
}

const ChessComponent = () => {

    const [state, setChessComponentState] = useState(null)

    const backendInner = useBackend({
        stateType: "chessboard",
        onRegistered: backend =>
            backend.call("getCurrentState", {}, initialState => setChessComponentState(initialState))
    })

    const backend = {
        call: function (methodName, params) {
            backendInner.call(
                methodName,
                params,
                newState => setChessComponentState(newState)
            )
        }
    }

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
        return re(CommandInput, {onExecCommand: ({commandStr, onDone}) =>
                backendInner.call("execChessCommand", {command:commandStr},
                    resp => {
                        onDone({errorMsg:resp.commandErrorMsg, responseMsg: resp.commandResponseMsg})
                        setChessComponentState(resp)
                    }
                )
        })
    }

    function renderLeftPanel() {
        return re(Container.col.top.left, {childStyle:{marginBottom:"5px"}},
            renderChessBoard(),
            renderCommandInputField()
        )
    }

    function renderRightPanel() {
        return re(Container.col.top.left, {childStyle:{marginBottom:"5px"}},
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
            return re(Container.row.left.top, {childStyle:{marginLeft:"5px",marginTop:"5px"}},
                renderLeftPanel(),
                renderRightPanel()
            )
        } else {
            return re(LinearProgress, {key:"LinearProgress",color:"secondary"})
        }
    }

    return renderPageContent()
}