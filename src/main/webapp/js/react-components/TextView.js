const TextShortView = ({node, navigateToNodeId, reloadParentNode}) => {
    return paper(re(TextNodeEditable, {
            value:getTagSingleValue(node, TAG_ID.text),
            textAreaStyle: {width:"1000px", margin:"0px 0px 10px 10px"},
            onSave: ({newValue, onSaved}) => updateTextNodeText(node[NODE.id], newValue, reloadParentNode)
    }))
}