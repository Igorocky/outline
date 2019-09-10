const buttonsStyle = {margin: "10px"}

const AdminView = () => {
    const [backupIsInProgress, setBackupIsInProgress] = useState(false)

    function doBackup() {
        setBackupIsInProgress(true)
        doRpcCall("doBackup", {}, () => setBackupIsInProgress(false))
    }

    return Container.row.left.top({children: [
            RE.Button({
                    key: "backup-btn", style: buttonsStyle, variant: "contained", disabled: backupIsInProgress,
                    onClick: doBackup
                }, "Backup"
            ),
            RE.If(backupIsInProgress, RE.CircularProgress({size:24}))
    ]})
}