'use strict'

const ChessPuzzleShortView = ({node, navigateToNodeId, reloadParentNode}) => {
    return re(FolderComponent,{
        text:getTagSingleValue(node, TAG_ID.name, node[NODE.objectClass]),
        onClick: () => navigateToNodeId(node[NODE.id]),
        icon: RE.img({
            src:"/img/chess/chess_puzzle.png",
            style: {width:"24px", height:"24px", marginTop: "5px", marginLeft: "5px"}
        })
    })
}

const ChessPuzzleFullView = ({curNode, actionsContainerRef, navigateToNodeId}) => {
    return RE.Container.col.top.left({},{},
        RE.Container.row.left.center({},{},
            "URL",
            re(EditableTextField,{
                initialValue:getTagSingleValue(curNode, TAG_ID.chessPuzzleUrl),
                typographyStyle: {margin:"0px 10px"},
                textFieldStyle: {width:"1000px", margin:"0px 10px"},
                onSave: ({newValue, onSaved}) =>
                    setSingleTagForNode(
                        curNode[NODE.id],
                        TAG_ID.chessPuzzleUrl,
                        newValue,
                        () => {
                            onSaved()
                            navigateToNodeId(curNode[NODE.id])
                        }
                    ),
                placeholder: "URL"
            })
        )
    )
}