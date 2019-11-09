'use strict'

const ChessGameShortView = ({node, navigateToNodeId, reloadParentNode}) => {
    return re(FolderComponent,{
        text:getTagSingleValue(node, TAG_ID.name, node[NODE.objectClass]),
        onClick: () => navigateToNodeId(node[NODE.id]),
        icon: RE.img({
            src:"/img/chess/Chess_ndt45.svg",
            style: {width:"24px", height:"24px", marginTop: "5px", marginLeft: "5px"}
        }),
        popupActions: RE.Fragment({},
            iconButton({iconName: "open_in_new",
                onClick: e => {
                    window.open(getTagSingleValue(node, TAG_ID.CHESS_GAME_URL))
                    e.stopPropagation()
                }
            })
        )
    })
}

const TABS = {
    pgn:{title: "PGN", id: "PGN"},
    moves:{title: "Moves", id: "Moves"},
    practice:{title: "Practice", id: "Practice"},
}

const ChessGameFullView = ({curNode, actionsContainerRef, navigateToNodeId}) => {
    const [currTabId, setCurrTabId] = useState(TABS.pgn.id)

    function getCurrGameId() {
        return curNode[NODE.id]
    }

    function reloadCurrNode() {
        navigateToNodeId(getCurrGameId())
    }

    function renderUrl() {
        const gameUrl = getTagSingleValue(curNode, TAG_ID.CHESS_GAME_URL);
        return RE.Container.row.left.center({},{},
            "URL",
            re(EditableTextField,{
                key:"chess-game-url-" + getCurrGameId(),
                inlineActions: true,
                initialValue: gameUrl,
                spanStyle: {margin:"0px 10px", fontSize:"18px"},
                textFieldStyle: {width:"600px", margin:"0px 10px"},
                onSave: ({newValue, onSaved}) =>
                    setSingleTagForNode(
                        getCurrGameId(),
                        TAG_ID.CHESS_GAME_URL,
                        newValue,
                        () => {
                            onSaved()
                            navigateToNodeId(getCurrGameId())
                        }
                    ),
                placeholder: "URL",
                popupActions: RE.Fragment({},
                    (gameUrl && gameUrl.length > 0)?iconButton({iconName: "open_in_new",
                        onClick: () => window.open(gameUrl)
                    }):null
                )
            })
        )
    }

    function renderCurrentTabContent() {
        if (TABS.pgn.id == currTabId) {
            return renderUrl()
        } else if (TABS.moves.id == currTabId) {
            return "moves"
        } else if (TABS.practice.id == currTabId) {
            return "practice"
        }
    }

    function handleTabChange(event, newTabId) {
        setCurrTabId(newTabId)
    }
    
    function renderTabs() {
        return RE.Container.col.top.left({}, {style:{marginBottom:"5px"}},
            RE.Paper({square:true},
                RE.Tabs({value:currTabId,
                        indicatorColor:"primary",
                        textColor:"primary",
                        onChange:handleTabChange},
                    RE.Tab({label:TABS.pgn.title, value:TABS.pgn.id}),
                    RE.Tab({label:TABS.moves.title, value:TABS.moves.id}),
                    RE.Tab({label:TABS.practice.title, value:TABS.practice.id}),
                )
            ),
            renderCurrentTabContent()
        )
    }

    return RE.Container.col.top.left({},{},
        RE.Container.row.left.top({},{},
            renderTabs()
        )
    )
}