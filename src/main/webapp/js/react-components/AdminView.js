const buttonsStyle = {margin: "10px"}
const inButtonCircularProgressStyle = {
    color: MuiColors.green[500],
    position: 'absolute',
    top: '50%',
    left: '50%',
    marginTop: -12,
    marginLeft: -12,
}

const AdminView = () => {
    const [backupIsInProgress, setBackupIsInProgress] = useState(false)

    function doBackup() {
        setBackupIsInProgress(true)
        doRpcCall("doBackup", {}, () => setBackupIsInProgress(false))
    }

    return Container.row.left.top({children: [
        RE.div({style:{position: 'relative'}},
            RE.Button({
                    key: "backup-btn", style: buttonsStyle, variant: "contained", disabled: backupIsInProgress,
                    onClick: doBackup
                }, "Backup"
            ),
            RE.If(backupIsInProgress,
                RE.CircularProgress({size:24, style: inButtonCircularProgressStyle})
            )
        )
    ]})
}