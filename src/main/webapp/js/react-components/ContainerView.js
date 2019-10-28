const actionButtonsStyle = {marginRight: "10px"}

const ContainerShortView = ({
    id, name, navigateToNodeId, onMoveToStart, onMoveUp, onMoveDown, onMoveToEnd, onDelete,
}) => {
    return re(FolderComponent,{key:id, id:id, name:name,
        onClick: () => navigateToNodeId(id),
        onMoveToStart: onMoveToStart,
        onMoveUp: onMoveUp,
        onMoveDown: onMoveDown,
        onMoveToEnd: onMoveToEnd,
        onDelete: onDelete,
    })
}
const ContainerFullView = ({curNode, actionsContainerRef, navigateToNodeId}) => {
    const [checkedNodes, setCheckedNodes] = useState(null)
    const [importDialogOpened, setImportDialogOpened] = useState(false)
    const [openConfirmActionDialog, closeConfirmActionDialog, renderConfirmActionDialog] = useConfirmActionDialog()

    function getCurrNodeId() {
        return curNode[NODE.id]
    }

    useEffect(() => {
        document.onpaste = event => uploadImage({
            file: extractFileFromEvent(event),
            parentId: getCurrNodeId(),
            onSuccess: reloadCurrNode
        })
        return () => document.onpaste = null
    }, [getCurrNodeId()])

    function reloadCurrNode() {
        navigateToNodeId(getCurrNodeId())
    }

    function deleteNode(nodeId) {
        openConfirmActionDialog({
            pConfirmText: "Delete?",
            pOnCancel: closeConfirmActionDialog,
            pStartActionBtnText: "Delete",
            pStartAction: ({onDone}) => beRemoveNode(nodeId, () => {
                closeConfirmActionDialog()
                reloadCurrNode()
            }),
            pActionDoneText: "not used",
            pActionDoneBtnText: "not used",
            pOnActionDoneBtnClick: closeConfirmActionDialog
        })
    }

    function createMoveDeleteActions(node) {
        const nodeId = node[NODE.id];
        return {
            onMoveToStart: () => moveNodeToStart(nodeId,reloadCurrNode),
            onMoveUp: () => moveNodeUp(nodeId,reloadCurrNode),
            onMoveDown: () => moveNodeDown(nodeId,reloadCurrNode),
            onMoveToEnd: () => moveNodeToEnd(nodeId,reloadCurrNode),
            onDelete: () => deleteNode(nodeId),
        }
    }

    function renderTextShortView(node) {
        return re(TextShortView,{
            id:node[NODE.id],
            text:getTagSingleValue(node, TAG_ID.text),
            onChanged: reloadCurrNode,
            ...createMoveDeleteActions(node)
        })
    }

    function renderImageShortView(node) {
        return re(ImageShortView,{
            imgId: getTagSingleValue(node, TAG_ID.imgId),
            ...createMoveDeleteActions(node)
        })
    }

    function renderContainerShortView(node) {
        return re(ContainerShortView, {
            id: node[NODE.id],
            name: getTagSingleValue(node, TAG_ID.name),
            navigateToNodeId: navigateToNodeId,
            ...createMoveDeleteActions(node)
        })
    }

    function renderNode(node) {
        if (node[NODE.objectClass] === OBJECT_CLASS.text) {
            return renderTextShortView(node)
        } else if (node[NODE.objectClass] === OBJECT_CLASS.image) {
            return renderImageShortView(node)
        } else if (node[NODE.objectClass] === OBJECT_CLASS.container) {
            return renderContainerShortView(node)
        } else {
            return RE.ListItem({key:node[NODE.id]},
                paper("Unknown type of node: " + node[NODE.objectClass])
            )
        }
    }

    function isNodeChecked(node) {
        return checkedNodes.includes(node[NODE.id]);
    }

    function checkNode(node) {
        return () => {
            if (isNodeChecked(node)) {
                setCheckedNodes(old => _.reject(old, id => id==node[NODE.id]))
            } else {
                setCheckedNodes(old => [...old, node[NODE.id]])
            }
        }
    }

    function renderCheckBoxIfCheckMode(childNode) {
        if (checkedNodes) {
            return RE.ListItemIcon({},
                RE.Checkbox({edge:"start",
                    checked:isNodeChecked(childNode),
                    onClick: checkNode(childNode),
                    tabIndex:-1, disableRipple:true})
            )
        } else {
            return null
        }
    }

    function renderCurrNodeChildren() {
        return RE.List({key:"List"+getCurrNodeId()}, curNode[NODE.childNodes].map(childNode =>
            RE.ListItem({key:childNode[NODE.id], dense:true},
                renderCheckBoxIfCheckMode(childNode),
                RE.ListItemText({}, renderNode(childNode))
            )
        ))
    }

    function unselectAllItems() {
        setCheckedNodes([])
    }

    function openImportDialog() {
        setImportDialogOpened(true)
    }

    function openExportDialog(nodeId) {
        openConfirmActionDialog({
            pConfirmText: "Start export?",
            pOnCancel: closeConfirmActionDialog,
            pStartActionBtnText: "Export",
            pStartAction: ({onDone}) => exportToFile({nodeId:nodeId, onSuccess: onDone}),
            pActionDoneText: "Export finished.",
            pActionDoneBtnText: "OK",
            pOnActionDoneBtnClick: closeConfirmActionDialog
        })
    }

    function appendTextNode({newValue, onSaved}) {
        createChildTextNode(curNode, newValue, () => {
                onSaved()
                reloadCurrNode()
            }
        )
    }

    function renderCutBtnIfNecessary() {
        if (checkedNodes && checkedNodes.length > 0) {
            return re(Button, {
                    key: "Cut-btn", style: actionButtonsStyle, variant: "contained",
                    onClick: () => putNodeIdsToClipboard(checkedNodes, () => setCheckedNodes(null))
                }, "Cut"
            )
        } else {
            return null
        }
    }

    function renderCancelSelectionBtnIfNecessary() {
        if (checkedNodes) {
            return re(Button, {
                    key: "cancel-selection-btn", style: actionButtonsStyle, variant: "contained",
                    onClick: () => setCheckedNodes(null)
                }, "Cancel selection"
            )
        } else {
            return null
        }
    }

    function renderPasteBtnIfNecessary() {
        if (curNode[NODE.canPaste]) {
            return re(Button,{key:"Paste-btn", style:actionButtonsStyle, variant:"contained",
                onClick: () => pasteNodesFromClipboard(getCurrNodeId(), reloadCurrNode)}, "Paste"
            )
        } else {
            return null
        }
    }

    function renderActions() {
        return RE.Fragment({},
            re(ContainerFullViewActions,{key:"ContainerFullViewActions",
                onNewNode: () => createChildNode(curNode, newNodeId => navigateToNodeId(newNodeId)),
                onNewSiblingNode: () => null,
                onSelect: unselectAllItems,
                onImport: openImportDialog,
                onExport: ()=>openExportDialog(getCurrNodeId())
            }),
            re(NewTextInput, {key:"NewTextInput"+getCurrNodeId(), onSave: appendTextNode}),
            renderCancelSelectionBtnIfNecessary(),
            renderCutBtnIfNecessary(),
            renderPasteBtnIfNecessary()
        )
    }

    function renderImportDialogIfNecessary() {
        if (importDialogOpened) {
            return re(ImportDialog, {key:"Import dialog", parentId:getCurrNodeId(),
                onCancel: () => setImportDialogOpened(false),
                onImported: response => navigateToNodeId((response[NODE.id]))}
            )
        } else {
            return null;
        }
    }

    return RE.Fragment({},
        renderCurrNodeChildren(),
        re(Portal, {key:"Portal",container: actionsContainerRef.current}, renderActions()),
        renderImportDialogIfNecessary(),
        renderConfirmActionDialog()
    )
}