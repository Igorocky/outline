
const AdminView = () => {
    return RE.Container.row.left.top({},{style:{margin: "10px"}},
        re(ButtonWithCircularProgress, {pButtonText:"Backup",
            pStartAction: ({onDone}) => doRpcCall("doBackup", {}, onDone)}
        )
    )
}