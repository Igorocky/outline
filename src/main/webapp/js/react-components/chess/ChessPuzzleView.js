'use strict'

const ChessPuzzleShortView = ({node, navigateToNodeId, reloadParentNode}) => {
    return re(FolderComponent,{
        text:getTagSingleValue(node, TAG_ID.name, node[NODE.objectClass]),
        onClick: () => navigateToNodeId(node[NODE.id]),
        icon: RE.img({
            src:"/img/chess/chess_puzzle.png",
            style: {width:"24px", height:"24px", marginTop: "5px", marginLeft: "5px"}
        }),
        popupActions: RE.Fragment({},
            iconButton({iconName: "open_in_new",
                onClick: e => {
                    window.open(getTagSingleValue(node, TAG_ID.chessPuzzleUrl))
                    e.stopPropagation()
                }
            })
        )
    })
}

const ChessPuzzleFullView = ({curNode, actionsContainerRef, navigateToNodeId}) => {
    const [newCommentText, setNewCommentText] = useState(null)
    const [openConfirmActionDialog, closeConfirmActionDialog, renderConfirmActionDialog] = useConfirmActionDialog()
    const [puzzleHistory, setPuzzleHistory] = useState(null)

    function getCurrPuzzleId() {
        return curNode[NODE.id]
    }

    function reloadCurrNode() {
        navigateToNodeId(getCurrPuzzleId())
    }

    function isPaused() {
        return "true" == getTagSingleValue(curNode, TAG_ID.CHESS_PUZZLE_PAUSED)
    }

    function renderUrl() {
        return RE.Container.row.left.center({},{},
            "URL",
            re(EditableTextField,{
                key:"puzzle-url-" + curNode[NODE.id],
                initialValue:getTagSingleValue(curNode, TAG_ID.chessPuzzleUrl),
                typographyStyle: {margin:"0px 10px"},
                textFieldStyle: {width:"1000px", margin:"0px 10px"},
                onSave: ({newValue, onSaved}) =>
                    setSingleTagForNode(
                        curNode[NODE.id],
                        TAG_ID.chessPuzzleUrl,
                        newValue,
                        () => {
                            onSaved()
                            navigateToNodeId(curNode[NODE.id])
                        }
                    ),
                placeholder: "URL",
                popupActions: RE.Fragment({},
                    iconButton({iconName: "open_in_new",
                        onClick: () => window.open(getTagSingleValue(curNode, TAG_ID.chessPuzzleUrl))
                    })
                )
            })
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
            return RE.Button( {onClick:() => setNewCommentText(""), variant:"contained"}, "Add comment")
        }
    }

    function renderComments() {
        return RE.Container.col.top.left({},{style:{marginBottom: "10px"}},
            RE.Typography({variant:"subtitle2"}, "Comments"),
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

    function renderHistory() {
        return RE.Container.col.top.left({},{},
            RE.Typography({variant:"subtitle2"}, "History"),
            puzzleHistory
                ?re(ReportResult, puzzleHistory)
                :re(ButtonWithCircularProgress,{
                    pButtonText: "Load History",
                    pStartAction: ({onDone}) => doRpcCall(
                        "rpcRunReport",
                        {name:"puzzle-history", params:{puzzleId:getCurrPuzzleId()}},
                        res => {
                            console.log("puzzle-history = " + JSON.stringify(res));
                            setPuzzleHistory(res)
                        })
                })
        )
    }

    function deleteComment(commentId) {
        openConfirmActionDialog({
            pConfirmText: "Delete?",
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

    function renderPaused() {
        return RE.Container.row.left.center({},{},
            isPaused()?"Paused":"Active",
            RE.Switch({
                checked: isPaused(),
                onChange: () => setSingleTagForNode(
                    getCurrPuzzleId(), TAG_ID.CHESS_PUZZLE_PAUSED, !isPaused(), reloadCurrNode
                ),
            })
        )
    }

    return RE.Container.col.top.left({},{style:{marginBottom:"20px"}},
        renderUrl(),
        renderPaused(),
        renderComments(),
        renderHistory(),
        renderConfirmActionDialog()
    )
}