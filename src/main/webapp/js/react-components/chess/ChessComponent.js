const CHESS_COMPONENT_STAGE = {
    initialPosition: "INITIAL_POSITION",
    moves: "MOVES",
    exercise: "EXERCISE",
}

const ChessComponent = () => {

    const [state, setChessComponentState] = useState(null)

    function setRootComponentState(newState) {
        setChessComponentState(newState)
    }

    useEffect(() => {
        doRpcCall("initialState", {}, setRootComponentState)
    }, [])

    function renderChessBoard() {
        return re(ChessBoard,{key:"ChessBoard", setRootComponentState:setRootComponentState,
            ...state.chessBoard})
    }

    function renderCurrentTabContent() {
        if (state.availableChessmanTypes) {
            return re(InitialPosition,{key:"InitialPosition", setRootComponentState:setRootComponentState,
                ...state.availableChessmanTypes})
        } else if (state.history) {
            return re(History,{key:"History", setRootComponentState:setRootComponentState,
                ...state.history})
        } else {
            return null;
        }
    }

    function handleTabChange(event, newValue) {
        doRpcCall("chessTabSelected", {tab:newValue}, setRootComponentState)
    }

    function renderCommandInputField() {
        return re(CommandInput, {onExecCommand: ({commandStr, onDone}) =>
                doRpcCall("execChessCommand", {command:commandStr},
                    resp => {
                        onDone({errorMsg:resp.commandErrorMsg})
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