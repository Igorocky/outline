
const ExportDialog = props => {
    const [exportDone, setExportDone] = useState(false)
    const [isExporting, setIsExporting] = useState(false)

    function startExport() {
        setIsExporting(true)
        exportToFile({nodeId:props.nodeId, onSuccess: () => {
            setIsExporting(false)
            setExportDone(true)
        }})
    }

    return re(Dialog, {open:true},
        re(DialogTitle, {},
            "Export"
        ),
        re(DialogContent, {},
            isExporting
                ? [
                    re(CircularProgress, {key: "ExportDialog-CircularProgress", size: 24}),
                    re(Typography, {key: "ExportDialog-Typography"}, "Exporting...")
                ]
                : exportDone
                        ?re(Typography, {key: "ExportDialog-Typography-done-msg"}, "Export finished.")
                        :re(Typography, {key: "ExportDialog-Typography-question"}, "Start export?")
        ),
        re(DialogActions, {},
            exportDone
                ?re(Button, {key:"ExportDialog-ok-btn", color:"primary", variant:"contained",
                    onClick: props.onExported}, "OK")
                :!isExporting?[
                    re(Button, {key:"ImportDialog-cancel-btn", onClick: props.onCancel}, "Cancel"),
                    re(Button, {key:"ImportDialog-import-btn", color:"primary", variant:"contained",
                        onClick: startExport}, "Export")
                ]:null
        )
    )
}