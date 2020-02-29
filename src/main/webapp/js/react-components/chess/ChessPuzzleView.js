'use strict'

const ChessPuzzleShortView = ({node, navigateToNodeId, reloadParentNode, createLink}) => {
    const [openConfirmActionDialog, closeConfirmActionDialog, renderConfirmActionDialog] = useConfirmActionDialog()
    const popupActions = []
    const chessPuzzleUrl = getTagSingleValue(node, TAG_ID.chessPuzzleUrl)

    function createDuplicate() {
        openConfirmActionDialog({
            pConfirmText: "Create a duplicate?",
            pOnCancel: closeConfirmActionDialog,
            pStartActionBtnText: "Duplicate",
            pStartAction: ({onDone}) => doRpcCall(
                "rpcDuplicateChessPuzzle",
                {basePuzzleId:node[NODE.id]},
                () => {
                    closeConfirmActionDialog()
                    reloadParentNode()
                }
            ),
            pActionDoneText: "not used",
            pActionDoneBtnText: "not used",
            pOnActionDoneBtnClick: closeConfirmActionDialog
        })
    }

    popupActions.push(
        iconButton({iconName: "play_arrow",
            onClick: e => {
                window.open(PATH.createChessboardWithPractice(node[NODE.id]))
                e.stopPropagation()
            }
        })
    )
    popupActions.push(
        iconButton({iconName: "tap_and_play",
            onClick: e => {
                console.log("opening '" + PATH.createChessboardComponentM({puzzleId: node[NODE.id]}) + "'");
                window.open(PATH.createChessboardComponentM({puzzleId: node[NODE.id]}))
                e.stopPropagation()
            }
        })
    )
    if (chessPuzzleUrl) {
        popupActions.push(
            iconButton({iconName: "open_in_new",
                onClick: e => {
                    window.open(chessPuzzleUrl)
                    e.stopPropagation()
                }
            })
        )
    }
    popupActions.push(
        iconButton({iconName: "control_point_duplicate",
            onClick: e => {
                createDuplicate()
                e.stopPropagation()
            }
        })
    )

    return RE.Fragment({},
        re(FolderComponent,{
            text:getTagSingleValue(node, TAG_ID.name, node[NODE.objectClass]),
            props: createLink(PATH.createNodeWithIdPath(node[NODE.id])),
            icon: RE.img({
                src:"/img/chess/chess_puzzle.png",
                style: {width:"24px", height:"24px", marginTop: "5px", marginLeft: "5px"}
            }),
            popupActions: _.size(popupActions)>0?RE.Fragment({},popupActions):null
        }),
        renderConfirmActionDialog()
    )
}

const RedGreenSwitch = withStyles({
    switchBase: {
        color: "lightgrey",
        '&$checked': {
            color: "limegreen",
        },
        '&$checked + $track': {
            backgroundColor: "limegreen",
        },
    },
    checked: {},
    track: {},
})(RE.Switch)

const ChessPuzzleFullView = ({curNode, actionsContainerRef, navigateToNodeId}) => {
    const [newCommentText, setNewCommentText] = useState(null)
    const [openConfirmActionDialog, closeConfirmActionDialog, renderConfirmActionDialog] = useConfirmActionDialog()
    const fenRef = useRef(null)

    function getCurrPuzzleId() {
        return curNode[NODE.id]
    }

    function reloadCurrNode() {
        navigateToNodeId(getCurrPuzzleId())
    }

    function isPaused() {
        return "true" == getTagSingleValue(curNode, TAG_ID.CHESS_PUZZLE_PAUSED)
    }

    function isAutoResponseEnabled() {
        return "true" == getTagSingleValue(curNode, TAG_ID.CHESS_PUZZLE_AUTO_RESPONSE)
    }

    function isTextModeEnabled() {
        return "true" == getTagSingleValue(curNode, TAG_ID.CHESS_PUZZLE_TEXT_MODE)
    }

    function renderUrl() {
        const puzzleUrl = getTagSingleValue(curNode, TAG_ID.chessPuzzleUrl);
        return RE.Container.row.left.center({style:{marginBottom:"10px"}},{},
            renderPaused(),
            RE.Paper({style:{paddingLeft:"10px"}},
                "URL",
                re(EditableTextField,{
                    key:"puzzle-url-" + getCurrPuzzleId(),
                    inlineActions: true,
                    initialValue: puzzleUrl,
                    spanStyle: {margin:"0px 10px", fontSize:"18px"},
                    textFieldStyle: {width:"600px", margin:"0px 10px"},
                    onSave: ({newValue, onSaved}) =>
                        setSingleTagForNode(
                            getCurrPuzzleId(),
                            TAG_ID.chessPuzzleUrl,
                            newValue,
                            () => {
                                onSaved()
                                navigateToNodeId(getCurrPuzzleId())
                            }
                        ),
                    placeholder: "URL",
                    popupActions: RE.Fragment({},
                        (puzzleUrl && puzzleUrl.length > 0)?iconButton({iconName: "open_in_new",
                            onClick: () => window.open(puzzleUrl)
                        }):null
                    )
                })
            )
        )
    }

    function renderFen() {
        const puzzleFen = getTagSingleValue(curNode, TAG_ID.CHESS_PUZZLE_FEN);
        return RE.Paper({style:{paddingLeft:"10px", marginBottom:"10px"}, ref:fenRef},
            RE.Container.row.left.center({},{},
                "FEN",
                re(EditableTextField,{
                    key:"puzzle-fen-" + getCurrPuzzleId(),
                    inlineActions: true,
                    initialValue: puzzleFen,
                    spanStyle: {margin:"0px 10px", fontSize:"18px"},
                    textFieldStyle: {width:"600px", margin:"0px 10px"},
                    onSave: ({newValue, onSaved}) =>
                        setSingleTagForNode(
                            getCurrPuzzleId(),
                            TAG_ID.CHESS_PUZZLE_FEN,
                            newValue,
                            () => {
                                onSaved()
                                navigateToNodeId(getCurrPuzzleId())
                            }
                        ),
                    placeholder: "FEN",
                    popupActions: puzzleFen?RE.Fragment({},
                        iconButton({iconName: "equalizer", onClick: () => window.open(
                                "https://lichess.org/analysis/standard/" + puzzleFen.replace(" ","_")
                            )
                        }),
                    ):null,
                })
            )
        )
    }

    function renderDepth() {
        const puzzleDepth = getTagSingleValue(curNode, TAG_ID.CHESS_PUZZLE_DEPTH);
        return RE.Container.row.left.center({},{},
            "Depth",
            re(EditableTextField,{
                key:"puzzle-depth-" + getCurrPuzzleId(),
                inlineActions: true,
                initialValue: puzzleDepth,
                spanStyle: {margin:"0px 10px", fontSize:"18px"},
                textFieldStyle: {width:"100px", margin:"0px 10px"},
                onSave: ({newValue, onSaved}) => {
                    if (/^\d+$/.test(newValue)) {
                        setSingleTagForNode(
                            getCurrPuzzleId(),
                            TAG_ID.CHESS_PUZZLE_DEPTH,
                            newValue,
                            () => {
                                onSaved()
                                navigateToNodeId(getCurrPuzzleId())
                            }
                        )
                    } else if (newValue.trim() == "") {
                        removeTagFromNode(
                            getCurrPuzzleId(),
                            TAG_ID.CHESS_PUZZLE_DEPTH,
                            () => {
                                onSaved()
                                navigateToNodeId(getCurrPuzzleId())
                            }
                        )
                    }
                },
                placeholder: "Depth",
            })
        )
    }

    function renderPgnSettings() {
        return RE.Container.col.top.left({},{},
            renderAutoResponseIsEnabled(),
            renderTextModeIsEnabled(),
            renderDepth()
        )
    }

    function renderPgn() {
        const puzzlePgn = getTagSingleValue(curNode, TAG_ID.CHESS_PUZZLE_PGN);
        return RE.Paper(
            {
                style:{paddingLeft:"5px", paddingRight:"5px", marginBottom:"10px"},
                onDoubleClick: () => window.scrollTo(0, fenRef.current.offsetTop)
            },
            RE.Container.row.left.top({style:{padding:"5px"}},{},
                "PGN",
                re(EditablePgnViewer, {
                    value:puzzlePgn,
                    textAreaStyle: {width:"600px", margin:"0px 0px 10px 10px"},
                    onSave: ({newValue, onSaved}) => doRpcCall(
                        "rpcSavePgnForPuzzle",
                        {puzzleId:getCurrPuzzleId(), pgn:newValue},
                        () => {
                            onSaved()
                            navigateToNodeId(getCurrPuzzleId())
                        }
                    ),
                    popupActions: puzzlePgn?RE.Fragment({},
                        iconButton({iconName: "play_arrow",
                            onClick: () => window.open(PATH.createChessboardWithPractice(getCurrPuzzleId()))}),
                        iconButton({iconName: "delete", onClick: deletePgn}),
                    ):null,
                }),
                renderPgnSettings()
            )
        )
    }

    function renderAddNewCommentControls() {
        if (newCommentText != null) {
            return RE.Container.col.top.right({},{style:{marginBottom:"5px"}},
                RE.TextField({
                    autoFocus: true,
                    multiline: true,
                    style:{width:"500px"},
                    rowsMax: 15,
                    value: newCommentText,
                    variant: "outlined",
                    onChange: e => setNewCommentText(e.target.value),
                }),
                RE.Container.row.left.center({},{style:{marginLeft: "10px"}},
                    RE.Button( {onClick:() => {
                            saveCommentForChessPuzzle(
                                {puzzleId: getCurrPuzzleId(), text: newCommentText},
                                () => {
                                    setNewCommentText(null)
                                    reloadCurrNode()
                                }
                            )
                        }, color:"primary", variant:"contained"}, "Save"),
                    RE.Button( {onClick:() => setNewCommentText(null), variant:"contained"}, "Cancel"),
                )
            )
        } else {
            return null
        }
    }

    function renderComments() {
        return RE.Container.col.top.left({},{style:{marginBottom: "10px"}},
            RE.Container.row.left.center({},{style:{marginRight:"10px"}},
                RE.Typography({variant:"subtitle2"}, "Comments"),
                (newCommentText == null)
                    ?iconButton({iconName: "add_comment", onClick:() => setNewCommentText("")})
                    :null
            ),
            renderAddNewCommentControls(),
            curNode[CHESS_PUZZLE_DTO.comments].map(comment=>paper(re(TextNodeEditable, {
                key: comment[CHESS_PUZZLE_COMMENT_DTO.id],
                value:comment[CHESS_PUZZLE_COMMENT_DTO.text],
                textAreaStyle: {width:"1000px", margin:"0px 0px 10px 10px"},
                onSave: ({newValue, onSaved}) => setSingleTagForNode(
                    comment[CHESS_PUZZLE_COMMENT_DTO.id],
                    TAG_ID.CHESS_PUZZLE_COMMENT_TEXT,
                    newValue,
                    () => {
                        reloadCurrNode()
                        onSaved()
                    }
                ),
                popupActions: RE.Fragment({},
                    iconButton({iconName: "delete",
                        onClick: () => deleteComment(comment[CHESS_PUZZLE_COMMENT_DTO.id])})
                )
            })))
        )
    }

    function deleteComment(commentId) {
        openConfirmActionDialog({
            pConfirmText: "Delete comment?",
            pOnCancel: closeConfirmActionDialog,
            pStartActionBtnText: "Delete",
            pStartAction: ({onDone}) => beRemoveNode(commentId, () => {
                closeConfirmActionDialog()
                reloadCurrNode()
            }),
            pActionDoneText: "not used",
            pActionDoneBtnText: "not used",
            pOnActionDoneBtnClick: closeConfirmActionDialog
        })
    }

    function deletePgn() {
        openConfirmActionDialog({
            pConfirmText: "Delete PGN?",
            pOnCancel: closeConfirmActionDialog,
            pStartActionBtnText: "Delete",
            pStartAction: ({onDone}) => doRpcCall(
                "rpcSavePgnForPuzzle",
                {puzzleId:getCurrPuzzleId(), pgn:""},
                () => {
                    closeConfirmActionDialog()
                    reloadCurrNode()
                }
            ),
            pActionDoneText: "not used",
            pActionDoneBtnText: "not used",
            pOnActionDoneBtnClick: closeConfirmActionDialog
        })
    }

    function togglePaused() {
        setSingleTagForNode(
            getCurrPuzzleId(), TAG_ID.CHESS_PUZZLE_PAUSED, !isPaused(), reloadCurrNode
        )
    }

    function toggleAutoResponse() {
        setSingleTagForNode(
            getCurrPuzzleId(), TAG_ID.CHESS_PUZZLE_AUTO_RESPONSE, !isAutoResponseEnabled(), reloadCurrNode
        )
    }

    function toggleTextMode() {
        setSingleTagForNode(
            getCurrPuzzleId(), TAG_ID.CHESS_PUZZLE_TEXT_MODE, !isTextModeEnabled(), reloadCurrNode
        )
    }

    function renderPaused() {
        return RE.Container.row.left.center({},{},
            RE.span({style:{color: isPaused()?"red":"green"}}, isPaused()?"Paused":"Active"),
            RE.Switch({
                checked: isPaused(),
                onChange: togglePaused,
            })
        )
    }

    function renderAutoResponseIsEnabled() {
        return RE.Container.row.left.center({},{},
            re(RedGreenSwitch,{
                checked: isAutoResponseEnabled(),
                onChange: toggleAutoResponse,
                style:{color:isAutoResponseEnabled()?"limegreen":"lightgrey"}
            }),
            RE.span({style:{color: isAutoResponseEnabled()?"limegreen":"lightgrey"}}, "Auto-response")
        )
    }

    function renderTextModeIsEnabled() {
        return RE.Container.row.left.center({},{},
            re(RedGreenSwitch,{
                checked: isTextModeEnabled(),
                onChange: toggleTextMode,
                style:{color:isTextModeEnabled()?"limegreen":"lightgrey"}
            }),
            RE.span({style:{color: isTextModeEnabled()?"limegreen":"lightgrey"}}, "Text-mode")
        )
    }

    return RE.Container.col.top.left({},{},
        renderUrl(),
        renderFen(),
        renderPgn(),
        renderComments(),
        re(ChessPuzzleHistory,{puzzleId:getCurrPuzzleId()}),
        renderConfirmActionDialog()
    )
}