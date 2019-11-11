
const ImportDialog = props => {
    const fileInputRef = React.useRef(null)
    const [isImporting, setIsImporting] = useState(false)

    function startImport() {
        setIsImporting(true)
        uploadImportFile({file: fileInputRef.current.files[0], parentId:props.parentId, onSuccess: props.onImported})
    }

    return RE.Dialog({open:true},
        RE.DialogTitle({},
            "Import"
        ),
        RE.DialogContent({},
            RE.DialogContentText({},
                "Select file with data to import:"
            ),
            re('input', {type:"file", ref:fileInputRef})
        ),
        RE.DialogActions({},
            !isImporting
                ?[
                    RE.Button({key:"ImportDialog-cancel-btn", onClick: props.onCancel}, "Cancel"),
                    RE.Button({key:"ImportDialog-import-btn", color:"primary", variant:"contained",
                        onClick: startImport}, "Import")
                ]
                :[
                    re(CircularProgress, {key:"ImportDialog-CircularProgress", size:24}),
                    RE.Typography({key:"ImportDialog-Typography"}, "Importing...")
                ]
        )
    )
}