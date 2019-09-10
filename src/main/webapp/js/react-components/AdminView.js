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

    function renderBackupButton() {
        return RE.div({style:{position: 'relative'}},
            RE.Button({
                    key: "backup-btn", variant: "contained", disabled: backupIsInProgress,
                    onClick: doBackup
                }, "Backup"
            ),
            RE.If(backupIsInProgress,
                RE.CircularProgress({size:24, style: inButtonCircularProgressStyle})
            )
        )

    }

    return RE.Container.row.left.top({},{style:{margin: "10px"}},
        renderBackupButton()
    )
}