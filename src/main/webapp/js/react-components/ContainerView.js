const actionButtonsStyle = {marginRight: "10px"}

const ContainerShortView = ({node, navigateToNodeId, reloadParentNode, createLink}) => {
    const [uploadNodeIconDialogOpened, setUploadNodeIconDialogOpened] = useState(false)

    const nodeId = node[NODE.id]

    const popupActions = RE.Fragment({},
        iconButton({iconName: "insert_photo",
            onClick: () => setUploadNodeIconDialogOpened(true)
        })
    )

    function renderUploadNodeIconDialog() {
        if (uploadNodeIconDialogOpened) {
            return re(UploadNodeIconDialog, {
                parentId: nodeId,
                onUploaded: () => {
                    setUploadNodeIconDialogOpened(false)
                    reloadParentNode()
                },
                onDelete: () => doRpcCall(
                    "rpcRemoveNodeIconForNode",
                    {nodeId:nodeId},
                    () => {
                        setUploadNodeIconDialogOpened(false)
                        reloadParentNode()
                    }
                ),
                onCancel: () => setUploadNodeIconDialogOpened(false)
            })
        } else {
            return null
        }
    }

    function getUserIcon() {
        const nodeIconImgId = getTagSingleValue(node, TAG_ID.NODE_ICON_IMG_ID)
        if (nodeIconImgId) {
            return RE.img({
                src:"/be/image/" + nodeIconImgId,
                style: {maxWidth:"85px", maxHeight:"85px", borderRadius: "20px"}
            })
        }
    }

    return RE.Fragment({},
        re(FolderComponent,{
            keyVal:nodeId,
            text:getTagSingleValue(node, TAG_ID.name),
            props: createLink(PATH.createNodeWithIdPath(nodeId)),
            icon: RE.Icon({style: {fontSize: "24px", marginTop: "5px", marginLeft: "5px"}}, "folder"),
            userIcon: getUserIcon(),
            popupActions: popupActions
        }),
        renderUploadNodeIconDialog()
    )
}

const OBJECT_CLASS_TO_SHORT_VIEW_MAP = {
    [OBJECT_CLASS.container]: ContainerShortView,
    [OBJECT_CLASS.text]: TextShortView,
    [OBJECT_CLASS.image]: ImageShortView,
    [OBJECT_CLASS.chessPuzzle]: ChessPuzzleShortView,
    [OBJECT_CLASS.CHESS_GAME]: ChessGameShortView,
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
        checkMode ? RE.Checkbox({
            edge: "start",
            checked: checked,
            onClick: onChecked,
            tabIndex: -1, disableRipple: true
        }) : null,
        reorderMode?iconButton({
            onClick: e => {
                setAnchorEl(e.currentTarget)
                e.stopPropagation()
            },
            iconName: "more_vert"
        }):null,
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

const ContainerFullView = ({curNode, actionsContainerRef, navigateToNodeId, createLink}) => {
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
            return re(shortView,{
                node:node,
                navigateToNodeId:navigateToNodeId,
                reloadParentNode:reloadCurrNode,
                createLink: createLink
            })
        } else {
            return RE.Link({
                    color:"primary",
                    className:"path-elem pointer-on-hover",
                    ...createLink(PATH.createNodeWithIdPath(node[NODE.id]))
                },
                "[" + node[NODE.objectClass] + "]"
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
        return RE.Container.col.top.left(
            {classes: {root: "NodeChildren-root"}},
            {classes: {item: (!checkedNodes && !reorderMode)?"NodeChildren-item":""}},
            curNode[NODE.childNodes].map(childNode => RE.Container.row.left.center(
                {classes: {root: "NodeChildren-inner-root"}},
                {classes: {root: "NodeChildren-inner-item"}},
                re(ChildItemLeftButton, {
                    checkMode: checkedNodes, checked: checkedNodes && isNodeChecked(childNode),
                    onChecked: checkNode(childNode),
                    reorderMode: reorderMode, ...createMoveDeleteActions(childNode)
                }),
                renderChildNodeShortView(childNode)
            ))
        )
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
                }, "Complete reordering"
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

    const defaultAction = {text: "New Folder", onClick: () => createChildNode(curNode, OBJECT_CLASS.container, navigateToNodeId)}
    const actions = [
        defaultAction,
        {text: "New Sibling Folder", onClick: () => null},
        {text: "New Chess Puzzle", onClick: () => createChildNode(curNode, OBJECT_CLASS.chessPuzzle, navigateToNodeId)},
        {text: "New Chess Game", onClick: () => createChildNode(curNode, OBJECT_CLASS.CHESS_GAME, navigateToNodeId)},
        {text: "Select items", onClick: () => {cancelReordering(); unselectAllItems();}},
        {text: "Reorder items", onClick: () => {cancelSelection();setReorderMode(true);}},
        {text: "Import", onClick: openImportDialog},
        {text: "Export", onClick: ()=>openExportDialog(getCurrNodeId())},
    ]
    function renderActions() {
        return RE.Fragment({},
            re(ContainerFullViewActions,{defaultAction: defaultAction, actions: actions}),
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