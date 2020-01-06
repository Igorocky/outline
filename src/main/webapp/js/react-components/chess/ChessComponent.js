const CHESS_COMPONENT_STAGE = {
    initialPosition: "INITIAL_POSITION",
    moves: "MOVES",
    practice: "PRACTICE_SEQUENCE",
}

const ChessComponent = ({match, showPracticeTab, showOnlyPracticeTab, onBackendCreated, onSave, onCancel,
                            setPageTitle}) => {
    const [state, setChessComponentState] = useState(null)
    const puzzleId = getByPath(match, ["params", "puzzleId"])
    const [puzzleName, setPuzzleName] = useState(null)
    const [initialPgnHashCode, setInitialPgnHashCode] = useState(null)

    useEffect(() => {
        if (setPageTitle) {
            if (puzzleId) {
                if (puzzleName) {
                    document.title = "Solve puzzle: " + puzzleName
                } else {
                    document.title = "Solve Puzzle"
                }
            } else {
                document.title = "Chessboard editor"
            }
        }
    }, [puzzleId,puzzleName])

    function processBackendStateCreated(backend) {
        if (onBackendCreated) {
            onBackendCreated(backend)
        } else if (puzzleId) {
            loadPuzzle(puzzleId)
        } else {
            backend.call("getCurrentState", {})
        }
    }

    useEffect(() => {
        if (puzzleId) {
            loadPuzzle(puzzleId)
        }
    }, [puzzleId])

    function loadPuzzle(puzzleId) {
        getNode({id:puzzleId}, puzzle => {
            setPuzzleName(getTagSingleValue(puzzle, TAG_ID.name))
            backend.call("loadFromPgn", {
                pgn:getTagSingleValue(puzzle, TAG_ID.CHESS_PUZZLE_PGN),
                tabToOpen:"PRACTICE_SEQUENCE",
                autoResponse:getTagSingleValue(puzzle, TAG_ID.CHESS_PUZZLE_AUTO_RESPONSE),
            })
        })
    }

    const backend = useBackend({
        stateType: "chessboard",
        onBackendStateCreated: processBackendStateCreated,
        onMessageFromBackend: chessComponentResponse => {
            if (chessComponentResponse.chessComponentView) {
                setChessComponentState(chessComponentResponse.chessComponentView)
                setInitialPgnHashCode(currPgnHashCode => {
                    if (currPgnHashCode == null) {
                        return chessComponentResponse.chessComponentView.pgnHashCode
                    } else {
                        return currPgnHashCode
                    }
                })
            } else if (chessComponentResponse.savePgn) {
                onSave(chessComponentResponse.savePgn)
            }
        }
    })

    function renderChessBoard() {
        if (state.chessBoard) {
            return re(ChessBoard,{key:"ChessBoard", backend:backend,
                ...state.chessBoard})
        } else if (state.chessBoardText) {
            return RE.TextField({
                className: "black-text",
                multiline: true,
                rowsMax: 3000,
                value: state.chessBoardText,
                disabled: true,
                variant: "standard",
                style:{width:"300px", margin:"0px 0px 10px 10px"},
            })
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
        const tabs = {}
        if (state.noMovesRecorded && !showOnlyPracticeTab) {
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
                render: () => re(SequencePractice,{backend:backend, ...state.practiseState, history:state.history})
            }
        }
        return reTabs({selectedTab:state.tab, onTabSelected:handleTabChange, tabs:tabs})
    }

    function renderSaveCancelButtons() {
        return RE.Container.row.right.top({},{style:{marginRight: "10px"}},
            onSave?RE.Button({
                color:"primary",
                variant:"contained",
                onClick: ()=>backend.call("getPgnToSave", {}),
                disabled: initialPgnHashCode == state.pgnHashCode
            }, "Save"):null,
            onCancel?RE.Button({variant:"contained", onClick:onCancel}, "Cancel"):null,
        )
    }

    function renderTitle() {
        if (puzzleName) {
            return RE.span({style:{fontWeight:"bold", fontSize:"30px"}},puzzleName)
        } else {
            return null
        }
    }

    function renderPageContent() {
        if (state) {
            return RE.Container.col.top.left({},{},
                renderTitle(),
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