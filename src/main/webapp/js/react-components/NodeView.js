const actionButtonsStyle = {marginRight: "10px"}

const currNodeNameStyle = {
    paddingLeft: "10px"
}

const NodeView = props => {
    const [curNode, setCurNode] = useState(null)
    const [redirect, setRedirect] = useRedirect()

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
                    () => getNodeById(curNode[NODE.id], resp => setCurNode(resp))
                )
            )
        })
        return () => document.onpaste = null
    }, [curNode])

    function renderPathToCurrNode() {
        return re(Breadcrumbs,{key:"path-to-cur-node"},
            re(Link, {key:"rootLink", color:"primary", className:"path-elem", onClick: () => setRedirect(PATH.node)}, "root"),
            curNode[NODE.path].map(pathElem =>
                re(Link, {key:pathElem[NODE.id], color:"primary", className:"path-elem",
                                        onClick: () => setRedirect(PATH.createNodeWithIdPath(pathElem[NODE.id]))},
                    pathElem[NODE.name]
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
                key:"NodeNameEditable",
                value:curNode[NODE.name],
                style: {width:"1000px", margin:"0px 0px 10px 10px"},
                onSave: ({newValue, onSaved}) => updateNodeName(curNode[NODE.id], newValue,
                    response => {
                        onSaved()
                        getNodeById(curNode[NODE.id], resp => setCurNode(resp))
                    }
                )
            }
        )
    }

    function renderNode(node) {
        if (node[NODE.objectClass] === OBJECT_CLASS.text) {
            return re(ListItem,{key:node[NODE.id]},
                re(ListItemText,{},
                    paper(re(TextNodeEditable,
                        {
                            value:node[NODE.text],
                            textAreaStyle: {width:"1000px", margin:"0px 0px 10px 10px"},
                            onSave: ({newValue, onSaved}) => updateTextNodeText(node[NODE.id], newValue,
                                response => {
                                    onSaved()
                                    getNodeById(curNode[NODE.id], resp => setCurNode(resp))
                                }
                            )
                        }
                    ))
                )
            )
        } else if (node[NODE.objectClass] === OBJECT_CLASS.node) {
            return re(ListItem,{key:node[NODE.id], button: true, onClick: () => setRedirect(PATH.createNodeWithIdPath(node[NODE.id]))},
                re(ListItemIcon,{key:"ListItemIcon"}, re(Icon, {style: {fontSize: "24px"}}, "folder")),
                re(ListItemText,{key:"ListItemText"},node[NODE.name])
            )
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
            re(List, {key:"List",component:"nav"},
                curNode[NODE.childNodes].map(ch => renderNode(ch))
            )
        ],
        curNode?re(Portal, {key:"Portal",container: props.actionsContainerRef.current},
            re(Button,{key:"New node", style:actionButtonsStyle, variant:"contained",
                        onClick: ()=>createChildNode(
                            curNode,
                                resp => setRedirect(PATH.createNodeWithIdPath(resp[NODE.id]))
                        )}, "New node"
            ),
            re(NewTextInput, {key:"NewTextInput", onSave: ({newValue, onSaved}) => createChildTextNode(
                    curNode,
                    resp => updateTextNodeText(resp[NODE.id], newValue,
                                        resp => {
                                            onSaved();
                                            getNodeById(curNode[NODE.id], resp => setCurNode(resp))
                                        }
                            )
                )
            })
        ):null,
        redirectTo(redirect)
    ]
}