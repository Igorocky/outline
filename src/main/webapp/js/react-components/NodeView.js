const actionButtonsStyle = {marginRight: "10px"}

const NodeView = props => {
    const [curNode, setCurNode] = useState(null)
    const [redirect, setRedirect] = useState(null)

    useEffect(() => {
        getNodeById(props.nodeIdToLoad, resp => setCurNode(resp))
    }, [props.nodeIdToLoad])

    function renderNode(node) {
        if (node[NODE_OBJECT_CLASS] === NODE_OBJECT_CLASS_TEXT) {
            return re(ListItem,{key:node[NODE_ID]},
                re(ListItemText,{},
                    paper(re(EditableTextArea,
                        {
                            value:node[NODE_TEXT],
                            textAreaStyle: {width:"1000px", margin:"0px 0px 10px 10px"}
                        }
                    ))
                )
            )
        } else if (node[NODE_OBJECT_CLASS] === NODE_OBJECT_CLASS_NODE) {
            return re(ListItem,{key:node[NODE_ID], button: true, onClick: () => setRedirect(PATH.createNodeWithIdPath(node[NODE_ID]))},
                re(ListItemIcon, {}, re(Icon, {style: {fontSize: "24px"}}, "folder")),
                re(ListItemText,{},node[NODE_NAME])
            )
        } else {
            return re(ListItem,{key:node[NODE_ID]},
                paper("Unknown type of node: " + node[NODE_OBJECT_CLASS])
            )
        }
    }

    return [
        !curNode
        ? re(LinearProgress, {key:"LinearProgress",color:"secondary"})
        : re(List, {key:"List",component:"nav"},
            curNode[NODE_CHILDREN].map(ch => renderNode(ch))
        ),
        re(Portal, {key:"Portal",container: props.actionsContainerRef.current},
            re(Button,{key:"action1", style:actionButtonsStyle, variant:"contained", onClick: ()=>console.log("action = '" + 1 + "'")}, "Action1"),
            re(Button,{key:"action2", style:actionButtonsStyle, variant:"contained", onClick: ()=>console.log("action = '" + 2 + "'")}, "Action2")
        ),
        redirect ? re(Redirect,{to: redirect}) : null
    ]
}