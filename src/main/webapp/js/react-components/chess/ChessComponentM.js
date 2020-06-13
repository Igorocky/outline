"use strict";

const CHESSMAN_TYPE_CODES = {
    "WHITE" : {"P": 9817, "N": 9816, "B": 9815, "R": 9814, "Q": 9813, "K": 9812},
    "BLACK" : {"P": 9823, "N": 9822, "B": 9821, "R": 9820, "Q": 9819, "K": 9818}
}

const ChessComponentM = ({actionsContainerRef}) => {
    const [openConfirmActionDialog, closeConfirmActionDialog, renderConfirmActionDialog] = useConfirmActionDialog()
    const [state, setChessComponentMState] = useState(null)
    const query = useQuery()
    const puzzleId = query.get("puzzleId")
    const fen = query.get("fen")
    const [puzzleName, setPuzzleName] = useState(null)
    const [historyIsShown, setHistoryIsShown] = useState(false)
    const [settingsIsShown, setSettingsIsShown] = useState(false)
    const [showMoreControlButtons, setShowMoreControlButtons] = useState(false)
    const [speechComponentActive, setSpeechComponentActive] = useState(false)

    useEffect(() => {
        document.title = getPageTitle()
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

    const backendState = useBackendState({
        stateType: "chessboard",
        onBackendStateCreated: processBackendStateCreated,
        onMessageFromBackend: chessComponentResponse => {
            if (chessComponentResponse.chessComponentView) {
                setChessComponentMState(chessComponentResponse.chessComponentView)
            }
        }
    })

    function getPageTitle() {
        if (puzzleName) {
            return puzzleName
        } else if (puzzleId) {
            return "Solve puzzle"
        } else {
            return "Play Chess"
        }
    }

    function loadPuzzle(puzzleId) {
        getNode({id:puzzleId}, puzzle => {
            setPuzzleName(getTagSingleValue(puzzle, TAG_ID.name))
            const autoResponseEnabled = "true" == getTagSingleValue(puzzle, TAG_ID.CHESS_PUZZLE_AUTO_RESPONSE);
            backendState.call("loadFromPgn", {
                pgn:getTagSingleValue(puzzle, TAG_ID.CHESS_PUZZLE_PGN),
                tabToOpen:null,
                autoResponse: autoResponseEnabled,
                depth:getTagSingleValue(puzzle, TAG_ID.CHESS_PUZZLE_DEPTH),
                commands: ["sm", "ci"]
            })
        })
    }

    function loadFen(fen) {
        backendState.call("loadFromFen", {
            fen:fen,
            tabToOpen:"MOVES",
            autoResponse: false,
            commands: ["sm", "ci"]
        })
    }

    useEffect(() => {
        if (backendState.isReady && puzzleId) {
            loadPuzzle(puzzleId)
        }
    }, [backendState.isReady, puzzleId])

    function renderTitle() {
        return RE.span({}, getPageTitle())
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
            return re(HistoryM, {backend: backendState, ...history})
        } else {
            return null
        }
    }

    function renderControlButtons() {
        const currentPositionFen = getByPath(state, ["currPositionFen"])

        function goToStart() {backendState.call("execChessCommand", {command:"s"})}
        function goToEnd() {backendState.call("execChessCommand", {command:"e"})}
        function goToPrev() {backendState.call("execChessCommand", {command:"p"})}
        function goToNext() {backendState.call("execChessCommand", {command:"n"})}
        function analyzePosition() {window.open(PATH.createChessboardComponentM({fen:urlEncodeFen(currentPositionFen)}))}

        const buttons = [[
                {iconName:"fast_rewind", onClick: goToStart},
                {icon:RE.Icon({style:{transform: "scaleX(-1)"}}, "play_arrow"), onClick: goToPrev},
                {iconName:"play_arrow", onClick: goToNext},
                {iconName:"fast_forward", onClick: goToEnd},
                {iconName:"delete_forever", onClick: deleteAllMovesToTheRight},
                {iconName:"settings", onClick: () => setSettingsIsShown(true)},
                {iconName:"equalizer", onClick: analyzePosition},
                {iconName:"more_horiz", onClick: () => setShowMoreControlButtons(!showMoreControlButtons)},
        ]]
        if (showMoreControlButtons) {
            buttons.push([
                {iconName:"history", disabled: !puzzleId, onClick: () => setHistoryIsShown(true)},
                {iconName:"flip_to_back", disabled: !state.noMovesRecorded, onClick: () => backendState.call(
                    "chessTabSelected", {tab:CHESS_COMPONENT_STAGE.initialPosition}
                )},
                {iconName:"record_voice_over", onClick: () => setSpeechComponentActive(true)},
            ])
        }

        return re(KeyPad, {
            componentKey: "controlButtons",
            keys: buttons,
            variant: "outlined",
        })
    }

    function renderMoveSelector() {
        const chessboardSequence = getByPath(state, ["chessBoardSequence"])
        if (chessboardSequence) {
            return re(ChessMoveSelectorM, {
                onMoveSelected: ({move, onDone}) => {
                    backendState.call("execChessCommand", {command:move==""?"nn":move})
                    onDone()
                }
            })
        } else {
            return null
        }
    }

    function getSelectedChessmanType(move) {
        if (move.length == 2) {
            return "P"
        } else {
            return move.substr(0,1)
        }
    }

    function getSelectedCellName(move) {
        if (move.length == 2) {
            return move
        } else {
            return move.substr(1,2)
        }
    }

    function getCoordsOfSelectedCell(move) {
        const selectedCellName = getSelectedCellName(move)
        const x = selectedCellName.charCodeAt(0)-97
        const y = selectedCellName.charCodeAt(1)-49
        return {x:x, y:y}
    }

    function getCoordsOfSelectedPiece(move) {
        const codeOfSelectedChessmanType =
            CHESSMAN_TYPE_CODES[state.availableChessmanTypes.colorToMove][getSelectedChessmanType(move)]
        const arr = state.availableChessmanTypes.availableChessmanTypes
        for (let i = 0; i < arr.length; i++) {
            for (let j = 0; j < arr[i].length; j++) {
                if (codeOfSelectedChessmanType == arr[i][j].code) {
                    return arr[i][j].coords
                }
            }
        }
    }

    function renderMoveSelectorForPositionBuilder() {
        return re(ChessMoveSelectorM, {
            disablePromotion: true,
            onMoveSelected: ({move, onDone}) => {
                if (move) {
                    backendState.call("cellLeftClicked", {coords:getCoordsOfSelectedPiece(move)})
                    backendState.call("cellLeftClicked", {coords:getCoordsOfSelectedCell(move)})
                } else {
                    loadFen(state.availableChessmanTypes.fen)
                }
                onDone()
            }
        })
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
            commandProgressMsg
                ?RE.span({},commandProgressMsg)
                :RE.span({style:{color:"white"}},"."),
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

    function renderDepthSetting() {
        const depth = getByPath(state, ["depth"])
        return RE.Container.row.left.center({},{},
            "Depth",
            re(EditableTextField,{
                inlineActions: true,
                initialValue: depth,
                spanStyle: {margin:"0px 10px", fontSize:"18px"},
                textFieldStyle: {width:"100px", margin:"0px 10px"},
                onSave: ({newValue, onSaved}) => {
                    if (/^\d+$/.test(newValue)) {
                        backendState.call("execChessCommand", {command: "d " + newValue})
                        onSaved()
                    }
                }
            })
        )
    }

    function renderSettings() {
        if (settingsIsShown) {
            return RE.Dialog({fullScreen:true, open:true},
                RE.Button({color:"primary", onClick: () => setSettingsIsShown(false)}, "Close"),
                RE.Container.col.top.right({},{},
                    renderDepthSetting()
                )
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
                backendState.call("execChessCommand", {command:"rr"})
                closeConfirmActionDialog()
            },
            pActionDoneText: "not used",
            pActionDoneBtnText: "not used",
            pOnActionDoneBtnClick: closeConfirmActionDialog
        })
    }

    function renderTextChessboard() {
        return RE.TextField({
            className: "black-text",
            multiline: true,
            rowsMax: 3000,
            value: state.chessBoardText,
            disabled: true,
            variant: "standard",
            style:{width:"300px"},
        })
    }

    function renderColorToMoveSelector() {
        return RE.Container.col.top.center({}, {},
            RE.RadioGroup({
                    row: true,
                    value: state.availableChessmanTypes.colorToMove,
                    onChange: event => backendState.call("setColorToMove", {colorToMove:event.target.value})
                },
                RE.FormControlLabel({label: "White to move", value: "WHITE", control: RE.Radio({})}),
                RE.FormControlLabel({label: "Black to move", value: "BLACK", control: RE.Radio({})}),
            )
        )
    }

    function renderCastlingAvailability({
                                            blackLongCastlingIsAvailable,
                                            blackShortCastlingIsAvailable,
                                            whiteLongCastlingIsAvailable,
                                            whiteShortCastlingIsAvailable,
    }) {
        return RE.Container.col.top.left({},{},
            RE.Container.row.left.top({},{},
                RE.FormControlLabel({
                    labelPlacement:"start",
                    label:"Black O-O-O",
                    control: RE.Checkbox({
                        checked: blackLongCastlingIsAvailable,
                        onClick: () => backendState.call("changeCastlingAvailability", {color:"BLACK", isLong: true}),
                    }),
                    style:{marginRight:"20px"}
                }),
                RE.FormControlLabel({
                    labelPlacement:"end",
                    label:"O-O",
                    control: RE.Checkbox({
                        checked: blackShortCastlingIsAvailable,
                        onClick: () => backendState.call("changeCastlingAvailability", {color:"BLACK", isLong: false}),
                    })
                }),
            ),
            RE.Container.row.left.top({},{},
                RE.FormControlLabel({
                    labelPlacement:"start",
                    label:"White O-O-O",
                    control: RE.Checkbox({
                        checked: whiteLongCastlingIsAvailable,
                        onClick: () => backendState.call("changeCastlingAvailability", {color:"WHITE", isLong: true}),
                    }),
                    style:{marginRight:"20px"}
                }),
                RE.FormControlLabel({
                    labelPlacement:"end",
                    label:"O-O",
                    control: RE.Checkbox({
                        checked: whiteShortCastlingIsAvailable,
                        onClick: () => backendState.call("changeCastlingAvailability", {color:"WHITE", isLong: false}),
                    })
                }),
            )
        )
    }

    function renderPositionBuilderControlButtons() {
        return RE.Fragment({},
            renderColorToMoveSelector(),
            renderMoveSelectorForPositionBuilder(),
            renderCastlingAvailability({...state.availableChessmanTypes})
        )
    }

    function renderPositionBuilderTab() {
        return RE.Container.col.top.right({},{style:{}},
            re(Portal, {container: actionsContainerRef.current}, renderTitle()),
            renderTextChessboard(),
            renderPositionBuilderControlButtons(),
            renderMoveSelector(),
        )
    }

    function renderMovesTab() {
        return RE.Container.col.top.right({},{style:{marginTop: "0.5em"}},
            re(Portal, {container: actionsContainerRef.current}, renderTitle()),
            renderMovesHistory(),
            renderControlButtons(),
            renderPuzzleStatus(),
            renderCommandResponses(),
            renderPositionIterator(),
            renderMoveSelector(),
            renderPuzzleHistory(),
            renderSettings(),
            renderConfirmActionDialog()
        )
    }

    if (speechComponentActive) {
        return re(SpeechChessComponent, {state})
    } else {
        if (state && state.tab == "INITIAL_POSITION") {
            return renderPositionBuilderTab()
        } else {
            return renderMovesTab()
        }
    }
}