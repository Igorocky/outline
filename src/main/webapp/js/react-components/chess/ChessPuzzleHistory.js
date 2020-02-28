'use strict'

const ChessPuzzleHistory = ({puzzleId}) => {
    const [newHistoryRecord, setNewHistoryRecord] = useState(null)
    const [openConfirmActionDialog, closeConfirmActionDialog, renderConfirmActionDialog] = useConfirmActionDialog()
    const [puzzleHistory, setPuzzleHistory] = useState(null)

    useEffect(() => loadPuzzleHistory(puzzleId),[puzzleId])

    function saveHistoryRecord() {
        doRpcCall(
            "rpcSaveChessPuzzleAttempt",
            {puzzleId:puzzleId, ...newHistoryRecord},
            () => {
                loadPuzzleHistory(puzzleId)
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

    function loadPuzzleHistory(puzzleId) {
        doRpcCall(
            "rpcRunReport",
            {name:"puzzle-history", params:{puzzleId:puzzleId}},
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
                    pStartAction: () => loadPuzzleHistory(puzzleId)
                })
        )
    }

    function deleteHistoryRecord(historyRecordId) {
        openConfirmActionDialog({
            pConfirmText: "Delete history record?",
            pOnCancel: closeConfirmActionDialog,
            pStartActionBtnText: "Delete",
            pStartAction: ({onDone}) => beRemoveNode(historyRecordId, () => {
                closeConfirmActionDialog()
                loadPuzzleHistory(puzzleId)
            }),
            pActionDoneText: "not used",
            pActionDoneBtnText: "not used",
            pOnActionDoneBtnClick: closeConfirmActionDialog
        })
    }

    return RE.Fragment({},
        renderHistory(),
        renderConfirmActionDialog()
    )
}