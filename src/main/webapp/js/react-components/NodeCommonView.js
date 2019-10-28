const NodeCommonView = ({actionsContainerRef, match}) => {
    const [curNode, setCurNode] = useState(null)
    const [redirect, setRedirect] = useRedirect()

    function loadNodeById(nodeId) {
        getNodeById(nodeId, resp => setCurNode(resp))
    }

    useEffect(() => {
        const nodeIdFromUrl = getByPath(match, ["params", "id"], null)
        if (nodeIdFromUrl != getCurrNodeId()) {
            loadNodeById(nodeIdFromUrl)
        } else if (!curNode) {
            loadNodeById(null)
        }
    }, [match])

    function getCurrNodeId() {
        return curNode?curNode[NODE.id]:null
    }

    function navigateToNodeId(nodeId) {
        if (getCurrNodeId() == nodeId) {
            reloadCurrNode()
        } else {
            setRedirect(PATH.createNodeWithIdPath(nodeId))
        }
    }

    function reloadCurrNode() {
        loadNodeById(getCurrNodeId())
    }

    function renderPathToCurrNode() {
        return RE.Breadcrumbs({key:"path-to-cur-node"+getCurrNodeId()},
            RE.Link({key:"rootLink", color:"primary", className:"path-elem pointer-on-hover",
                onClick: () => setRedirect(PATH.node)}, "root"),
            curNode[NODE.path].map(pathElem =>
                    RE.Link({key:pathElem[NODE.id], color:"primary", className:"path-elem pointer-on-hover",
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

    function renderNodeContent() {
        const nodeClass = curNode[NODE.objectClass];
        if (nodeClass === OBJECT_CLASS.topContainer || nodeClass === OBJECT_CLASS.container) {
            return re(ContainerFullView,{
                curNode: curNode,
                actionsContainerRef: actionsContainerRef,
                navigateToNodeId: navigateToNodeId
            })
        } else {
            return paper("Unknown type of node: " + nodeClass)
        }
    }

    function renderPageContent() {
        if (curNode) {
            return RE.Fragment({},
                renderPathToCurrNode(),
                renderCurrNodeName(),
                renderNodeContent()
            )
        } else {
            return RE.LinearProgress({key:"LinearProgress",color:"secondary"})
        }
    }


    return RE.Fragment({},
        renderPageContent(),
        redirectTo(redirect)
    )
}