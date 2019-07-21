const actionButtonsStyle = {marginRight: "10px"}

const NodeView2 = props => {
    const [curNode, setCurNode] = useState(null)

    useEffect(() => {
        getLastViewedNode(resp => setCurNode(resp))
    }, [])

    function renderNode(node) {
        if (node[NODE_OBJECT_CLASS] === NODE_OBJECT_CLASS_TEXT) {
            return re(ListItem,{key:node[NODE_ID]},
                re(ListItemText,{},
                    paper(re(EditableTextArea2,
                        {
                            value:node[NODE_TEXT],
                            textAreaStyle: {width:"1000px", margin:"0px 0px 10px 10px"}
                        }
                    ))
                )
            )
        } else {
            return re(ListItem,{key:node[NODE_ID]},
                paper("Unknown type of node")
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
        )
    ]
}