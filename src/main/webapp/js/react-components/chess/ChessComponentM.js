"use strict";

const ChessComponentM = ({}) => {
    const [state, setChessComponentMState] = useState(null)
    const query = useQuery();
    const puzzleId = query.get("puzzleId")
    const [puzzleName, setPuzzleName] = useState(null)

    useEffect(() => {
        if (puzzleName) {
            document.title = "Solve puzzle: " + puzzleName
        } else {
            document.title = "Solve Puzzle"
        }
    }, [puzzleName])

    function processBackendStateCreated(backend) {
        if (puzzleId) {
            loadPuzzle(puzzleId)
        } else {
            backend.call("getCurrentState", {})
        }
    }

    const backend = useBackend({
        stateType: "chessboard",
        onBackendStateCreated: processBackendStateCreated,
        onMessageFromBackend: chessComponentResponse => {
            if (chessComponentResponse.chessComponentView) {
                setChessComponentMState(chessComponentResponse.chessComponentView)
            }
        }
    })

    function loadPuzzle(puzzleId) {
        getNode({id:puzzleId}, puzzle => {
            setPuzzleName(getTagSingleValue(puzzle, TAG_ID.name))
            const autoResponseEnabled = "true" == getTagSingleValue(puzzle, TAG_ID.CHESS_PUZZLE_AUTO_RESPONSE);
            backend.call("loadFromPgn", {
                pgn:getTagSingleValue(puzzle, TAG_ID.CHESS_PUZZLE_PGN),
                tabToOpen:"PRACTICE_SEQUENCE",
                autoResponse: autoResponseEnabled,
                commands: ["sm", "ci"]
            })
        })
    }

    useEffect(() => {
        if (backend.isReady && puzzleId) {
            loadPuzzle(puzzleId)
        }
    }, [backend.isReady, puzzleId])

    function renderPositionIterator() {
        const chessboardSequence = getByPath(state, ["chessBoardSequence"])
        if (chessboardSequence) {
            return re(CellIteratorM, {...chessboardSequence})
        } else {
            return null
        }
    }

    function renderHistory() {
        const history = getByPath(state, ["history"])
        if (history) {
            return re(HistoryM, {backend: backend, ...history})
        } else {
            return null
        }
    }

    function renderMoveSelector() {
        const chessboardSequence = getByPath(state, ["chessBoardSequence"])
        if (chessboardSequence) {
            return re(ChessMoveSelectorM, {
                onMoveSelected: ({move, onDone}) => {
                    backend.call("execChessCommand", {command:move})
                    onDone()
                }
            })
        } else {
            return null
        }
    }

    return RE.Container.col.top.center({},{style:{marginTop: "0.5em"}},
        renderPositionIterator(),
        renderHistory(),
        renderMoveSelector(),
    )
}