"use strict";

const ChessComponentM = ({actionsContainerRef}) => {
    const [openConfirmActionDialog, closeConfirmActionDialog, renderConfirmActionDialog] = useConfirmActionDialog()
    const [state, setChessComponentMState] = useState(null)
    const query = useQuery();
    const puzzleId = query.get("puzzleId")
    const fen = query.get("fen")
    const [puzzleName, setPuzzleName] = useState(null)
    const [historyIsShown, setHistoryIsShown] = useState(false)

    useEffect(() => {
        if (puzzleName) {
            document.title = puzzleName
        } else {
            document.title = "Solve puzzle"
        }
    }, [puzzleName])

    function processBackendStateCreated(backend) {
        if (puzzleId) {
            loadPuzzle(puzzleId)
        } else if (fen) {
            loadFen(fen)
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
                tabToOpen:null,
                autoResponse: autoResponseEnabled,
                commands: ["sm", "ci"]
            })
        })
    }

    function loadFen(fen) {
        backend.call("loadFromFen", {
            fen:fen,
            tabToOpen:"MOVES",
            autoResponse: false,
            commands: ["sm", "ci"]
        })
    }

    useEffect(() => {
        if (backend.isReady && puzzleId) {
            loadPuzzle(puzzleId)
        }
    }, [backend.isReady, puzzleId])

    function renderTitle() {
        return RE.span({}, puzzleName)
    }

    function renderPositionIterator() {
        const chessboardSequence = getByPath(state, ["chessBoardSequence"])
        if (chessboardSequence) {
            return re(CellIteratorM, {...chessboardSequence})
        } else {
            return null
        }
    }

    function renderMovesHistory() {
        const history = getByPath(state, ["history"])
        if (history) {
            return re(HistoryM, {backend: backend, ...history})
        } else {
            return null
        }
    }

    function renderControlButtons() {
        const currentPositionFen = getByPath(state, ["currPositionFen"])

        function goToStart() {backend.call("execChessCommand", {command:"s"})}
        function goToEnd() {backend.call("execChessCommand", {command:"e"})}
        function goToPrev() {backend.call("execChessCommand", {command:"p"})}
        function goToNext() {backend.call("execChessCommand", {command:"n"})}
        function analyzePosition() {window.open(PATH.createChessboardComponentM({fen:urlEncodeFen(currentPositionFen)}))}

        return RE.ButtonGroup({size:"small"},
            RE.Button({onClick: goToStart},RE.Icon({}, "fast_rewind")),
            RE.Button({onClick: goToPrev},RE.Icon({style:{transform: "scaleX(-1)"}}, "play_arrow")),
            RE.Button({onClick: goToNext},RE.Icon({}, "play_arrow")),
            RE.Button({onClick: goToEnd},RE.Icon({}, "fast_forward")),
            RE.Button({onClick: deleteAllMovesToTheRight},RE.Icon({}, "delete_forever")),
            RE.Button({onClick: analyzePosition, disabled:!currentPositionFen},
                RE.Icon({}, "equalizer")),
            RE.Button({onClick: () => setHistoryIsShown(true)},RE.Icon({}, "history")),
        )
    }

    function renderMoveSelector() {
        const chessboardSequence = getByPath(state, ["chessBoardSequence"])
        if (chessboardSequence) {
            return re(ChessMoveSelectorM, {
                onMoveSelected: ({move, onDone}) => {
                    backend.call("execChessCommand", {command:move==""?"nn":move})
                    onDone()
                }
            })
        } else {
            return null
        }
    }

    function renderPuzzleStatus() {
        const puzzleState = getByPath(state, ["practiseState"])
        if (puzzleState) {
            return re(PuzzleProgressM, {...puzzleState})
        } else {
            return null
        }
    }

    function renderCommandResponses() {
        const commandProgressMsg = getByPath(state, ["commandProgressMsg"])
        const commandErrorMsg = getByPath(state, ["commandErrorMsg"])
        return RE.Fragment({},
            commandProgressMsg?RE.span({},commandProgressMsg):null,
            commandErrorMsg?RE.span({style:{color:"red"}},commandErrorMsg):null,
        )
    }

    function renderPuzzleHistory() {
        if (puzzleId && historyIsShown) {
            return RE.Dialog({fullScreen:true, open:true},
                RE.Button({color:"primary", onClick: () => setHistoryIsShown(false)}, "Close"),
                re(ChessPuzzleHistory, {puzzleId:puzzleId})
            )
        } else {
            return null
        }
    }

    function deleteAllMovesToTheRight() {
        openConfirmActionDialog({
            pConfirmText: "Delete all moves to the right?",
            pOnCancel: closeConfirmActionDialog,
            pStartActionBtnText: "Delete",
            pStartAction: ({onDone}) => {
                backend.call("execChessCommand", {command:"rr"})
                closeConfirmActionDialog()
            },
            pActionDoneText: "not used",
            pActionDoneBtnText: "not used",
            pOnActionDoneBtnClick: closeConfirmActionDialog
        })
    }

    return RE.Container.col.top.right({},{style:{marginTop: "0.5em"}},
        re(Portal, {container: actionsContainerRef.current}, renderTitle()),
        renderPositionIterator(),
        renderMovesHistory(),
        renderControlButtons(),
        renderPuzzleStatus(),
        renderCommandResponses(),
        renderMoveSelector(),
        renderPuzzleHistory(),
        renderConfirmActionDialog()
    )
}