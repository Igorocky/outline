
const ConfirmActionDialog = ({pTitle, pConfirmText, pOnCancel, pStartActionBtnText, pStartAction,
    pActionInProgressText, pActionDoneText, pActionDoneBtnText, pOnActionDoneBtnClick}) => {
    const [actionIsDone, setActionIsDone] = useState(false)
    const [actionIsInProgress, setActionIsInProgress] = useState(false)

    function startAction() {
        setActionIsInProgress(true)
        pStartAction({onDone: () => {
                setActionIsInProgress(false)
                setActionIsDone(true)
        }})
    }

    function drawContent() {
        if (!actionIsDone) {
            return re(Typography, {}, pConfirmText)
        } else {
            return re(Typography, {}, pActionDoneText)
        }
    }

    function drawActionButtons() {
        if (!actionIsDone) {
            if (!actionIsInProgress) {
                return [
                    re(Button, {key:"ConfirmActionDialog-cancel-btn", onClick: pOnCancel}, "Cancel"),
                    re(Button, {key:"ConfirmActionDialog-ok-btn", color:"primary", variant:"contained",
                        onClick: startAction}, pStartActionBtnText)
                ]
            } else {
                return [
                    re(CircularProgress, {key: "ConfirmActionDialog-CircularProgress", size: 24}),
                    re(Typography, {key: "ConfirmActionDialog-Typography"}, pActionInProgressText)
                ]
            }
        } else {
            return re(Button, {key:"ConfirmActionDialog-ok-btn", color:"primary", variant:"contained",
                onClick: pOnActionDoneBtnClick}, pActionDoneBtnText)
        }
    }

    return re(Dialog, {open:true},
        re(DialogTitle, {}, pTitle),
        re(DialogContent, {}, drawContent()),
        re(DialogActions, {}, drawActionButtons())
    )
}