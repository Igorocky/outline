const actionButtonsStyle = {marginRight: "10px"}

const currNodeNameStyle = {
    paddingLeft: "10px"
}

const NodeView = props => {
    const [curNode, setCurNode] = useState(null)
    const [redirect, setRedirect] = useRedirect()

    function getCurrNodeId() {
        return curNode?curNode[NODE.id]:null
    }

    function reloadCurrNode() {
        getNodeById(getCurrNodeId(), resp => setCurNode(resp))
    }

    useEffect(() => {
        getNodeById(props.nodeIdToLoad, resp => setCurNode(resp))
    }, [props.nodeIdToLoad])

    useEffect(() => {
        document.onpaste = event => uploadImage({
            file: extractFileFromEvent(event),
            onSuccess: imgDto => createChildImageNode(
                curNode,
                imgNode => updateImageNodeImage(
                    imgNode[NODE.id],
                    imgDto[NODE.id],
                    reloadCurrNode
                )
            )
        })
        return () => document.onpaste = null
    }, [curNode])

    function renderPathToCurrNode() {
        return re(Breadcrumbs,{key:"path-to-cur-node"+getCurrNodeId()},
            re(Link, {key:"rootLink", color:"primary", className:"path-elem pointer-on-hover", onClick: () => setRedirect(PATH.node)}, "root"),
            curNode[NODE.path].map(pathElem =>
                re(Link, {key:pathElem[NODE.id], color:"primary", className:"path-elem pointer-on-hover",
                        onClick: () => setRedirect(PATH.createNodeWithIdPath(pathElem[NODE.id]))},
                    pathElem[NODE.name]?pathElem[NODE.name]:""
                )
            )
        )
    }
    
    function renderCurrNodeName() {
        if (!curNode || !curNode[NODE.id]) {
            return null
        }
        return re(NodeNameEditable,
            {
                key:"NodeNameEditable" + getCurrNodeId(),
                value:curNode[NODE.name],
                style: {width:"1000px", margin:"0px 0px 10px 10px"},
                onSave: ({newValue, onSaved}) => updateNodeName(curNode[NODE.id], newValue,
                    response => {
                        onSaved()
                        reloadCurrNode()
                    }
                )
            }
        )
    }

    function renderNode(node) {
        if (node[NODE.objectClass] === OBJECT_CLASS.text) {
            return paper(re(TextNodeEditable,
                {
                    value:node[NODE.text],
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
        } else if (node[NODE.objectClass] === OBJECT_CLASS.image) {
            return paper(re(ImageNodeComponent, {
                imgId: node[NODE.imgId],
                onMoveToStart: () => moveNodeToStart(node[NODE.id],reloadCurrNode),
                onMoveUp: () => moveNodeUp(node[NODE.id],reloadCurrNode),
                onMoveDown: () => moveNodeDown(node[NODE.id],reloadCurrNode),
                onMoveToEnd: () => moveNodeToEnd(node[NODE.id],reloadCurrNode),
            }))
        } else if (node[NODE.objectClass] === OBJECT_CLASS.node) {
            return re(FolderComponent,{key:node[NODE.id], id:node[NODE.id], name:node[NODE.name],
                onClick: () => setRedirect(PATH.createNodeWithIdPath(node[NODE.id])),
                onMoveToStart: () => moveNodeToStart(node[NODE.id],reloadCurrNode),
                onMoveUp: () => moveNodeUp(node[NODE.id],reloadCurrNode),
                onMoveDown: () => moveNodeDown(node[NODE.id],reloadCurrNode),
                onMoveToEnd: () => moveNodeToEnd(node[NODE.id],reloadCurrNode),
            })
        } else {
            return re(ListItem,{key:node[NODE.id]},
                paper("Unknown type of node: " + node[NODE.objectClass])
            )
        }
    }

    return [
        !curNode
        ? re(LinearProgress, {key:"LinearProgress",color:"secondary"})
        : [
            renderPathToCurrNode(),
            renderCurrNodeName(),
            re(List, {key:"List"+getCurrNodeId()},
                curNode[NODE.childNodes].map(ch =>
                    re(ListItem,{key:ch[NODE.id], dense:true},
                        re(ListItemText,{},
                            renderNode(ch)
                        )
                    )
                )
            )
        ],
        re(Portal, {key:"Portal"+getCurrNodeId(),container: props.actionsContainerRef.current},
            re(Button,{key:"New node", style:actionButtonsStyle, variant:"contained",
                        onClick: curNode?()=>createChildNode(
                            curNode,
                                resp => setRedirect(PATH.createNodeWithIdPath(resp[NODE.id]))
                        ):()=>{}}, "New node"
            ),
            re(NewTextInput, {key:"NewTextInput", onSave: curNode?({newValue, onSaved}) => createChildTextNode(
                    curNode,
                    resp => updateTextNodeText(resp[NODE.id], newValue,
                                        resp => {
                                            onSaved()
                                            reloadCurrNode()
                                        }
                            )
                ):()=>{}
            })
        ),
        redirectTo(redirect)
    ]
}