
const ImportDialog = props => {
    const fileInputRef = React.useRef(null)
    const [isImporting, setIsImporting] = useState(false)

    function startImport() {
        setIsImporting(true)
        uploadImportFile({file: fileInputRef.current.files[0], parentId:props.parentId, onSuccess: props.onImported})
    }

    return re(Dialog, {open:true},
        re(DialogTitle, {},
            "Import"
        ),
        re(DialogContent, {},
            re(DialogContentText, {},
                "Select file with data to import:"
            ),
            re('input', {type:"file", ref:fileInputRef})
        ),
        re(DialogActions, {},
            !isImporting
                ?[
                    re(Button, {key:"ImportDialog-cancel-btn", onClick: props.onCancel}, "Cancel"),
                    re(Button, {key:"ImportDialog-import-btn", color:"primary", variant:"contained",
                        onClick: startImport}, "Import")
                ]
                :[
                    re(CircularProgress, {key:"ImportDialog-CircularProgress", size:24}),
                    re(Typography, {key:"ImportDialog-Typography"}, "Importing...")
                ]
        )
    )
}