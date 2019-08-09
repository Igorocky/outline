const actionButtonsStyle = {marginRight: "10px"}

const NodeView = props => {
    const [curNode, setCurNode] = useState(null)
    const [checkedNodes, setCheckedNodes] = useState(null)
    const [importDialogOpened, setImportDialogOpened] = useState(false)
    const [exportingDialogOpened, setExportingDialogOpened] = useState(false)
    const [redirect, setRedirect] = useRedirect()

    useEffect(() => {
        getNodeById(props.nodeIdToLoad, resp => setCurNode(resp))
    }, [props.nodeIdToLoad])

    useEffect(() => {
        document.onpaste = event => curNode?uploadImage({
            file: extractFileFromEvent(event),
            parentId:getCurrNodeId(),
            onSuccess: reloadCurrNode
        }):null
        return () => document.onpaste = null
    }, [curNode])

    function getCurrNodeId() {
        return curNode?curNode[NODE.id]:null
    }

    function getTagSingleValue(node, tagId, defaultValue) {
        return getByPath(node, [NODE.tags, tagId, 0, TAG.value], defaultValue)
    }

    function getTagSingleRef(node, tagId, defaultValue) {
        return getByPath(node, [NODE.tags, tagId, 0, TAG.ref], defaultValue)
    }

    function reloadCurrNode() {
        getNodeById(getCurrNodeId(), resp => setCurNode(resp))
    }

    function renderPathToCurrNode() {
        return re(Breadcrumbs,{key:"path-to-cur-node"+getCurrNodeId()},
            re(Link, {key:"rootLink", color:"primary", className:"path-elem pointer-on-hover",
                onClick: () => setRedirect(PATH.node)}, "root"),
            curNode[NODE.path].map(pathElem =>
                re(Link, {key:pathElem[NODE.id], color:"primary", className:"path-elem pointer-on-hover",
                        onClick: () => setRedirect(PATH.createNodeWithIdPath(pathElem[NODE.id]))},
                    getTagSingleValue(pathElem, TAG_ID.name, "")
                )
            )
        )
    }
    
    function renderCurrNodeName() {
        if (!curNode || !getCurrNodeId()) {
            return null
        }
        return re(NodeNameEditable,
            {
                key:"NodeNameEditable" + getCurrNodeId(),
                value:getTagSingleValue(curNode, TAG_ID.name),
                style: {width:"1000px", margin:"0px 0px 10px 10px"},
                onSave: ({newValue, onSaved}) => updateNodeName(getCurrNodeId(), newValue,
                    response => {
                        onSaved()
                        reloadCurrNode()
                    }
                )
            }
        )
    }

    function renderTextNode(node) {
        return paper(re(TextNodeEditable,
            {
                value:getTagSingleValue(node, TAG_ID.text),
                textAreaStyle: {width:"1000px", margin:"0px 0px 10px 10px"},
                onSave: ({newValue, onSaved}) => updateTextNodeText(node[NODE.id], newValue,
                    response => {
                        onSaved()
                        reloadCurrNode()
                    }
                ),
                onMoveToStart: () => moveNodeToStart(node[NODE.id],reloadCurrNode),
                onMoveUp: () => moveNodeUp(node[NODE.id],reloadCurrNode),
                onMoveDown: () => moveNodeDown(node[NODE.id],reloadCurrNode),
                onMoveToEnd: () => moveNodeToEnd(node[NODE.id],reloadCurrNode),
            }
        ))
    }

    function renderImageNode(node) {
        return paper(re(ImageNodeComponent, {
            imgId: getTagSingleRef(node, TAG_ID.imgId),
            onMoveToStart: () => moveNodeToStart(node[NODE.id],reloadCurrNode),
            onMoveUp: () => moveNodeUp(node[NODE.id],reloadCurrNode),
            onMoveDown: () => moveNodeDown(node[NODE.id],reloadCurrNode),
            onMoveToEnd: () => moveNodeToEnd(node[NODE.id],reloadCurrNode),
        }))
    }

    function renderContainerNode(node) {
        return re(FolderComponent,{key:node[NODE.id], id:node[NODE.id], name:getTagSingleValue(node, TAG_ID.name),
            onClick: () => setRedirect(PATH.createNodeWithIdPath(node[NODE.id])),
            onMoveToStart: () => moveNodeToStart(node[NODE.id],reloadCurrNode),
            onMoveUp: () => moveNodeUp(node[NODE.id],reloadCurrNode),
            onMoveDown: () => moveNodeDown(node[NODE.id],reloadCurrNode),
            onMoveToEnd: () => moveNodeToEnd(node[NODE.id],reloadCurrNode),
        })
    }

    function renderNode(node) {
        if (node[NODE.objectClass] === OBJECT_CLASS.text) {
            return renderTextNode(node)
        } else if (node[NODE.objectClass] === OBJECT_CLASS.image) {
            return renderImageNode(node)
        } else if (node[NODE.objectClass] === OBJECT_CLASS.node) {
            return renderContainerNode(node)
        } else {
            return re(ListItem,{key:node[NODE.id]},
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
            return re(ListItemIcon,{},
                re(Checkbox,{edge:"start",
                    checked:isNodeChecked(childNode),
                    onClick: checkNode(childNode),
                    tabIndex:-1, disableRipple:true})
            )
        } else {
            return null
        }
    }

    function renderCurrNodeChildren() {
        return re(List, {key:"List"+getCurrNodeId()}, curNode[NODE.childNodes].map(childNode =>
            re(ListItem,{key:childNode[NODE.id], dense:true},
                renderCheckBoxIfCheckMode(childNode),
                re(ListItemText,{}, renderNode(childNode))
            )
        ))
    }

    function renderPageContent() {
        if (curNode) {
            return [
                renderPathToCurrNode(),
                renderCurrNodeName(),
                renderCurrNodeChildren()
            ]
        } else {
            return re(LinearProgress, {key:"LinearProgress",color:"secondary"})
        }
    }

    function createChildNodeIfPossible() {
        if (curNode) {
            createChildNode(
                curNode,
                resp => setRedirect(PATH.createNodeWithIdPath(resp[NODE.id]))
            )
        }
    }

    function unselectAllItems() {
        setCheckedNodes([])
    }

    function openImportDialog() {
        setImportDialogOpened(true)
    }

    function openExportDialog() {
        setExportingDialogOpened(true)
    }

    function appendTextNode({newValue, onSaved}) {
        if (curNode) {
            createChildTextNode(curNode, resp => updateTextNodeText(resp[NODE.id], newValue,
                    resp => {
                        onSaved()
                        reloadCurrNode()
                    }
            ))
        }
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
        if (curNode && curNode[NODE.canPaste]) {
            return re(Button,{key:"Paste-btn", style:actionButtonsStyle, variant:"contained",
                onClick: () => pasteNodesFromClipboard(getCurrNodeId(), reloadCurrNode)}, "Paste"
            )
        } else {
            return null
        }
    }

    function renderActions() {
        return [
            re(NodeViewActions,{key:"NodeViewActions",
                onNewNode: createChildNodeIfPossible, onNewSiblingNode: () => null,
                onSelect: unselectAllItems, onImport: openImportDialog, onExport: openExportDialog
            }),
            re(NewTextInput, {key:"NewTextInput"+getCurrNodeId(), onSave: appendTextNode}),
            renderCancelSelectionBtnIfNecessary(),
            renderCutBtnIfNecessary(),
            renderPasteBtnIfNecessary()
        ]
    }

    function renderImportDialogIfNecessary() {
        if (importDialogOpened) {
            return re(ImportDialog, {key:"Import dialog", parentId:getCurrNodeId(),
                onCancel: () => setImportDialogOpened(false),
                onImported: response => setRedirect(PATH.createNodeWithIdPath(response[NODE.id]))}
            )
        } else {
            return null;
        }
    }

    function renderExportDialogIfNecessary() {
        if (exportingDialogOpened) {
            return re(ExportDialog, {key:"ExportDialog", nodeId:getCurrNodeId(),
                onCancel: () => setExportingDialogOpened(false),
                onExported: () => setExportingDialogOpened(false)
            })
        } else {
            return null;
        }
    }

    return [
        renderPageContent(),
        re(Portal, {key:"Portal",container: props.actionsContainerRef.current},
            renderActions()
        ),
        renderImportDialogIfNecessary(),
        renderExportDialogIfNecessary(),
        redirectTo(redirect)
    ]
}