"use strict";

const ChangeNodeIconDialog = ({nodeId, onUploaded,onCancel,onDelete}) => {
    const TAB_UPLOAD = "TAB_UPLOAD"
    const TAB_DELETE = "TAB_DELETE"

    const fileInputRef = React.useRef(null)
    const [isUploading, setIsUploading] = useState(false)
    const [selectedTab, setSelectedTab] = useState(TAB_UPLOAD)

    function startUploading() {
        setIsUploading(true)
        uploadImage({file: fileInputRef.current.files[0], parentId: nodeId, isNodeIcon:true, onSuccess: onUploaded})
    }

    function renderDialogContent() {
        return reTabs({selectedTab:selectedTab, onTabSelected: setSelectedTab, tabs:{
                [TAB_UPLOAD] : {
                    label: "upload",
                    render: () => RE.Fragment({},
                        RE.DialogContent({},
                            RE.Typography({},
                                "Select file with image to set as a node icon:"
                            ),
                            re('input', {type:"file", ref:fileInputRef})
                        ),
                        RE.DialogActions({},
                            !isUploading
                                ?RE.Fragment({},
                                RE.Button({onClick: onCancel}, "Cancel"),
                                RE.Button({color:"primary", variant:"contained", onClick: startUploading}, "Save")
                                )
                                :RE.Fragment({},
                                re(CircularProgress, {size:24}),
                                RE.Typography({}, "Saving...")
                                )
                        )
                    )
                },
                [TAB_DELETE] : {
                    label: "delete",
                    render: () => RE.Fragment({},
                        RE.DialogContent({}, RE.Typography({}, "Delete icon?")),
                        RE.DialogActions({},
                            RE.Button({onClick: onCancel}, "Cancel"),
                            RE.Button({color:"primary", variant:"contained", onClick: onDelete}, "Delete")
                        )
                    )
                }
        }})
    }

    return RE.Dialog({open:true},
        RE.DialogTitle({},
            "Change node icon"
        ),
        renderDialogContent()
    )
}