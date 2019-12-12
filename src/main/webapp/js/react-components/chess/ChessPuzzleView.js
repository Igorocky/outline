'use strict'

const ChessPuzzleShortView = ({node, navigateToNodeId, reloadParentNode, createLink}) => {
    return re(FolderComponent,{
        text:getTagSingleValue(node, TAG_ID.name, node[NODE.objectClass]),
        props: createLink(PATH.createNodeWithIdPath(node[NODE.id])),
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
    const [newHistoryRecord, setNewHistoryRecord] = useState(null)
    const [openConfirmActionDialog, closeConfirmActionDialog, renderConfirmActionDialog] = useConfirmActionDialog()
    const [puzzleHistory, setPuzzleHistory] = useState(null)

    useEffect(() => loadPuzzleHistory(),[])

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
        return RE.Paper({style:{paddingLeft:"10px", marginBottom:"10px"}},
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

    function renderPgn() {
        const puzzlePgn = getTagSingleValue(curNode, TAG_ID.CHESS_PUZZLE_PGN);
        return RE.Paper({style:{paddingLeft:"5px", paddingRight:"5px", marginBottom:"10px"}},
            RE.Container.row.left.center({style:{padding:"5px"}},{},
                "PGN",
                re(EditablePgnViewer, {
                    value:puzzlePgn,
                    textAreaStyle: {width:"1000px", margin:"0px 0px 10px 10px"},
                    onSave: ({newValue, onSaved}) => doRpcCall(
                        "rpcSavePgnForPuzzle",
                        {puzzleId:getCurrPuzzleId(), pgn:newValue},
                        () => {
                            onSaved()
                            navigateToNodeId(getCurrPuzzleId())
                        }
                    ),
                })
            )
        )
    }

    function saveHistoryRecord() {
        doRpcCall(
            "rpcSaveChessPuzzleAttempt",
            {puzzleId:getCurrPuzzleId(), ...newHistoryRecord},
            () => {
                loadPuzzleHistory()
                setNewHistoryRecord(null)
            }
        )
    }

    function onKeyDownInNewHistoryRecord(event) {
        if (event.keyCode == 13){
            saveHistoryRecord()
        } else if (event.keyCode == 27) {
            setNewHistoryRecord(null)
        }
    }

    function renderAddNewHistoryRecordControls() {
        if (newHistoryRecord != null) {
            return RE.Fragment({},
                RE.span({
                        style:{
                            color: newHistoryRecord.passed?"green":"red",
                            fontWeight:"bold", fontSize: "30px", marginRight:"10px", cursor:"pointer"},
                        onClick: () => setNewHistoryRecord(oldVal => ({...oldVal, passed: !oldVal.passed}))
                    },
                    newHistoryRecord.passed?"Passed":"Failed"
                ),
                RE.TextField({
                    autoFocus: true,
                    style: {width:"100px"},
                    onKeyDown: onKeyDownInNewHistoryRecord,
                    value: newHistoryRecord.pauseDuration,
                    variant: "outlined",
                    onChange: e => {
                        const pauseDuration = e.target.value
                        setNewHistoryRecord(oldVal => ({...oldVal, pauseDuration: pauseDuration}))
                    },
                }),
                iconButton({iconName:"save",onClick:saveHistoryRecord}),
                iconButton({iconName:"cancel",onClick:() => setNewHistoryRecord(null)}),
            )
        } else {
            return iconButton({iconName: "add", onClick:() => setNewHistoryRecord({passed:false, pauseDuration:""})})
        }
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

    function loadPuzzleHistory() {
        doRpcCall(
            "rpcRunReport",
            {name:"puzzle-history", params:{puzzleId:getCurrPuzzleId()}},
            res => setPuzzleHistory(res)
        )
    }

    function renderHistory() {
        return RE.Container.col.top.left({},{},
            RE.Container.row.left.center({},{style:{marginRight:"10px"}},
                RE.Typography({variant:"subtitle2"}, "History"),
                renderAddNewHistoryRecordControls()
            ),
            puzzleHistory
                ?re(ReportResult, {...puzzleHistory, actions:{
                    deleteHistoryRecord: deleteHistoryRecord
                }})
                :re(ButtonWithCircularProgress,{
                    pButtonText: "Show History",
                    variant:"text",
                    pStartAction: loadPuzzleHistory
                })
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

    function deleteHistoryRecord(historyRecordId) {
        openConfirmActionDialog({
            pConfirmText: "Delete history record?",
            pOnCancel: closeConfirmActionDialog,
            pStartActionBtnText: "Delete",
            pStartAction: ({onDone}) => beRemoveNode(historyRecordId, () => {
                closeConfirmActionDialog()
                loadPuzzleHistory()
            }),
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

    function renderPaused() {
        return RE.Container.row.left.center({},{},
            RE.span({style:{color: isPaused()?"red":"green", cursor:"pointer"}, onClick: togglePaused},
                isPaused()?"Paused":"Active"),
            RE.Switch({
                checked: isPaused(),
                onChange: togglePaused,
            })
        )
    }

    return RE.Container.col.top.left({},{},
        renderUrl(),
        renderFen(),
        renderPgn(),
        renderComments(),
        renderHistory(),
        renderConfirmActionDialog()
    )
}