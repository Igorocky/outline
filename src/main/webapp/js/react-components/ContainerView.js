const actionButtonsStyle = {marginRight: "10px"}

const ContainerShortView = ({node, navigateToNodeId, reloadParentNode}) => {
    return re(FolderComponent,{
        name:getTagSingleValue(node, TAG_ID.name),
        onClick: () => navigateToNodeId(node[NODE.id])
    })
}

const OBJECT_CLASS_TO_SHORT_VIEW_MAP = {
    [OBJECT_CLASS.container]: ContainerShortView,
    [OBJECT_CLASS.text]: TextShortView,
    [OBJECT_CLASS.image]: ImageShortView,
}

const ChildItemLeftButton = ({checkMode, checked, onChecked, reorderMode,
                                 onMoveToStart, onMoveUp, onMoveDown, onMoveToEnd, onDelete}) => {
    const [anchorEl, setAnchorEl] = useState(null)

    function performMove(moveFunction) {
        return () => {
            setAnchorEl(null)
            moveFunction()
        }
    }

    function moveDeleteButtons() {
        return RE.Fragment({},
            iconButton({iconName: "vertical_align_top", onClick: performMove(onMoveToStart)}),
            iconButton({iconName: "keyboard_arrow_up", onClick: performMove(onMoveUp)}),
            iconButton({iconName: "keyboard_arrow_down", onClick: performMove(onMoveDown)}),
            iconButton({iconName: "vertical_align_bottom", onClick: performMove(onMoveToEnd)}),
            iconButton({iconName: "delete", onClick: onDelete}),
        )
    }

    return RE.Fragment({},
        checkMode ? RE.ListItemIcon({},
            RE.Checkbox({
                edge: "start",
                checked: checked,
                onClick: onChecked,
                tabIndex: -1, disableRipple: true
            })
        ) : null,
        reorderMode?RE.ListItemIcon({},
            iconButton({
                onClick: e => {
                    setAnchorEl(e.currentTarget)
                    e.stopPropagation()
                },
                iconName: "more_vert"
            })
        ):null,
        anchorEl
            ? clickAwayListener({
                onClickAway: () => setAnchorEl(null),
                children: re(Popper, {open: true, anchorEl: anchorEl, placement: 'top-start'},
                    paper(moveDeleteButtons())
                )
            })
            : null
    )
}

const ContainerFullView = ({curNode, actionsContainerRef, navigateToNodeId}) => {
    const [checkedNodes, setCheckedNodes] = useState(null)
    const [reorderMode, setReorderMode] = useState(false)
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

    function renderChildNodeShortView(node) {
        const shortView = OBJECT_CLASS_TO_SHORT_VIEW_MAP[node[NODE.objectClass]]
        if (shortView) {
            return re(shortView,{node:node, navigateToNodeId:navigateToNodeId, reloadParentNode:reloadCurrNode})
        } else {
            return RE.Link({
                    color:"primary", className:"path-elem pointer-on-hover",
                    onClick: () => navigateToNodeId(node[NODE.id])},
                node[NODE.objectClass]
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

    function renderCurrNodeChildren() {
        return RE.List({key:"List"+getCurrNodeId()}, curNode[NODE.childNodes].map(childNode =>
            RE.ListItem({key:childNode[NODE.id], dense:true},
                re(ChildItemLeftButton, {
                    checkMode: checkedNodes, checked: checkedNodes && isNodeChecked(childNode),
                    onChecked: checkNode(childNode),
                    reorderMode: reorderMode, ...createMoveDeleteActions(childNode)
                }),
                RE.ListItemText({}, renderChildNodeShortView(childNode))
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
            return RE.Button({
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
            return RE.Button({
                    key: "cancel-selection-btn", style: actionButtonsStyle, variant: "contained",
                    onClick: cancelSelection
                }, "Cancel selection"
            )
        } else {
            return null
        }
    }

    function renderCancelReorderingBtnIfNecessary() {
        if (reorderMode) {
            return RE.Button({
                    style: actionButtonsStyle, variant: "contained",
                    onClick: cancelReordering
                }, "Cancel reordering"
            )
        } else {
            return null
        }
    }

    function renderPasteBtnIfNecessary() {
        if (curNode[NODE.canPaste]) {
            return RE.Button({key:"Paste-btn", style:actionButtonsStyle, variant:"contained",
                onClick: () => pasteNodesFromClipboard(getCurrNodeId(), reloadCurrNode)}, "Paste"
            )
        } else {
            return null
        }
    }

    function cancelSelection() {
        setCheckedNodes(null)
    }

    function cancelReordering() {
        setReorderMode(false)
    }

    function renderActions() {
        return RE.Fragment({},
            re(ContainerFullViewActions,{key:"ContainerFullViewActions",
                onNewNode: () => createChildNode(curNode, OBJECT_CLASS.container, navigateToNodeId),
                onNewSiblingNode: () => null,
                onNewChessPuzzle: () => createChildNode(curNode, OBJECT_CLASS.chessPuzzle, navigateToNodeId),
                onSelect: () => {
                    cancelReordering()
                    unselectAllItems()
                },
                onReorder: () => {
                    cancelSelection()
                    setReorderMode(true)
                },
                onImport: openImportDialog,
                onExport: ()=>openExportDialog(getCurrNodeId())
            }),
            re(NewTextInput, {key:"NewTextInput"+getCurrNodeId(), onSave: appendTextNode}),
            renderCancelSelectionBtnIfNecessary(),
            renderCancelReorderingBtnIfNecessary(),
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