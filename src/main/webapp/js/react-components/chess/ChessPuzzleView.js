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