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

    function renderCurrNodeName() {
        if (!curNode || !curNode[NODE.id]) {
            return null
        }
        return re(ListItem,{key:curNode[NODE.id]},
            re(ListItemText,{},
                paper(re(EditableTextField,
                    {
                        value:curNode[NODE.name],
                        textAreaStyle: {width:"1000px", margin:"0px 0px 10px 10px"},
                        multiline: false,
                        viewComponentProvider: ({value, onClick}) => re(Typography,
                            {key:"currNodeName", variant:"h5", onClick:onClick,
                                style:value?currNodeNameStyle:{...currNodeNameStyle, color: "lightgrey"}},
                            value?value:"Enter node name here"
                        ),
                        onSave: ({newValue, onSaved}) => updateNodeName(curNode[NODE.id], newValue,
                            response => {
                                onSaved()
                                getNodeById(curNode[NODE.id], resp => setCurNode(resp))
                            }
                        )
                    }
                ))
            )
        )
    }

    function renderNode(node) {
        if (node[NODE.objectClass] === OBJECT_CLASS.text) {
            return re(ListItem,{key:node[NODE.id]},
                re(ListItemText,{},
                    paper(re(EditableTextField,
                        {
                            value:node[NODE.text],
                            textAreaStyle: {width:"1000px", margin:"0px 0px 10px 10px"},
                            multiline: true
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
        : re(List, {key:"List",component:"nav"},
            renderCurrNodeName(),
            curNode[NODE.childNodes].map(ch => renderNode(ch))
        ),
        curNode?re(Portal, {key:"Portal",container: props.actionsContainerRef.current},
            re(Button,{key:"New node", style:actionButtonsStyle, variant:"contained",
                        onClick: ()=>createChildNode(
                            curNode,
                                resp => setRedirect(PATH.createNodeWithIdPath(resp[NODE.id]))
                        )}, "New node")

        ):null,
        redirectTo(redirect)
    ]
}