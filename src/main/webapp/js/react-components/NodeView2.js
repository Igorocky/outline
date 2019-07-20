
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

    return !curNode
        ? re(LinearProgress, {color:"secondary"})
        : re(List, {component:"nav"},
            curNode[NODE_CHILDREN].map(ch => renderNode(ch))
        )
}