
const AdminView = ({}) => {
    const [openConfirmActionDialog, closeConfirmActionDialog, renderConfirmActionDialog] = useConfirmActionDialog()

    useEffect(() => {
        document.title = "Admin"
    }, [])

    function doBackup() {
        openConfirmActionDialog({
            pConfirmText: "Create backup?",
            pOnCancel: closeConfirmActionDialog,
            pStartActionBtnText: "Backup",
            pStartAction: ({onDone}) => doRpcCall("doBackup", {}, onDone),
            pActionDoneText: "Backup created.",
            pActionDoneBtnText: "OK",
            pOnActionDoneBtnClick: closeConfirmActionDialog
        })
    }

    return RE.Container.row.left.top({},{style:{margin: "10px"}},
        RE.Button( {onClick:doBackup, variant:"contained"}, "Backup"),
        renderConfirmActionDialog()
    )
}