const actionButtonsStyle = {marginRight: "10px"}

const NodeView = props => {
    const [curNode, setCurNode] = useState(null)
    const [redirect, setRedirect] = useState(null)

    useEffect(() => {
        getNodeById(props.nodeIdToLoad, resp => setCurNode(resp))
    }, [props.nodeIdToLoad])

    function renderNode(node) {
        if (node[NODE.objectClass] === OBJECT_CLASS.text) {
            return re(ListItem,{key:node[NODE.id]},
                re(ListItemText,{},
                    paper(re(EditableTextArea,
                        {
                            value:node[NODE.text],
                            textAreaStyle: {width:"1000px", margin:"0px 0px 10px 10px"}
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
            curNode[NODE.childNodes].map(ch => renderNode(ch))
        ),
        re(Portal, {key:"Portal",container: props.actionsContainerRef.current},
            re(Button,{key:"action1", style:actionButtonsStyle, variant:"contained", onClick: ()=>console.log("action = '" + 1 + "'")}, "Action1"),
            re(Button,{key:"action2", style:actionButtonsStyle, variant:"contained", onClick: ()=>console.log("action = '" + 2 + "'")}, "Action2")
        ),
        redirectTo(redirect)
    ]
}