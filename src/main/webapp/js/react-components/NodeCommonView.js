'use strict'

const OBJECT_CLASS_TO_FULL_VIEW_MAP = {
    [OBJECT_CLASS.topContainer]: ContainerFullView,
    [OBJECT_CLASS.container]: ContainerFullView,
    [OBJECT_CLASS.chessPuzzle]: ChessPuzzleFullView,
}

const NodeCommonView = ({actionsContainerRef, match}) => {
    const [curNode, setCurNode] = useState(null)
    const [redirect, setRedirect] = useRedirect()

    function loadNodeById(nodeId) {
        getNode({id:nodeId, depth: 1, includeCanPaste: true, includePath: true}, resp => setCurNode(resp))
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
                    getTagSingleValue(pathElem, TAG_ID.name, "[" + pathElem[NODE.objectClass] + "]")
                )
            )
        )
    }
    
    function renderCurrNodeName() {
        if (!curNode || !getCurrNodeId()) {
            return null
        }
        return re(EditableTextField,
            {
                key:"name-of-node-" + getCurrNodeId(),
                initialValue:getTagSingleValue(curNode, TAG_ID.name),
                variant: "h5",
                typographyStyle: {width:"1000px", margin:"0px 0px 10px 10px"},
                textFieldStyle: {width:"1000px", margin:"0px 0px 10px 10px"},
                onSave: ({newValue, onSaved}) => updateNodeName(getCurrNodeId(), newValue,
                    response => {
                        onSaved()
                        reloadCurrNode()
                    }
                ),
                placeholder: "Enter node name here"
            }
        )
    }

    function renderNodeContent() {
        const fullView = OBJECT_CLASS_TO_FULL_VIEW_MAP[curNode[NODE.objectClass]]
        if (fullView) {
            return re(fullView,{
                curNode: curNode,
                actionsContainerRef: actionsContainerRef,
                navigateToNodeId: navigateToNodeId
            })
        } else {
            return RE.TextField({
                className: "black-text",
                style: {width:"1000px", margin:"0px 0px 10px 10px"},
                multiline: true,
                rowsMax: 3000,
                value: JSON.stringify(curNode, (k,v) => (k != NODE.path)?v:undefined, 2),
                disabled: true,
                variant: "standard",
            })
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