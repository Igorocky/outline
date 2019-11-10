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
    const [currTabId, setCurrTabId] = useState(curNode.parsedPgn ? TABS.moves.id : TABS.pgn.id)
    const [selectedMove, setSelectedMove] = useState({})

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

    function renderPgn() {
        return paper(re(TextNodeEditable, {
            key: getCurrGameId() + "-PGN",
            value:getTagSingleValue(curNode, TAG_ID.CHESS_GAME_PGN),
            textAreaStyle: {width:"1000px", margin:"0px 0px 10px 10px"},
            onSave: ({newValue, onSaved}) => doRpcCall(
                "rpcSavePgn",
                {gameId:getCurrGameId(), pgn:newValue},
                () => {
                    onSaved()
                    reloadCurrNode()
                }
            ),
        }))
    }

    function renderPgnTab() {
        return RE.Fragment({},
            renderUrl(),
            renderPgn()
        )
    }

    function renderChessBoard() {
        return re(ChessBoardFromFen, {fen:selectedMove.fen, moveFromTo:selectedMove.move})
    }

    function renderMovesTab() {
        return RE.Container.row.left.top({},{},
            renderChessBoard(),
            renderTableWithMoves()
        )
    }

    function renderTableWithMoves() {
        return RE.Paper({style:{maxHeight:"450px", overflow: "scroll"}},RE.Table({size:"small"},
            RE.TableBody({},
                RE.TableRow({key: "-1"}, RE.TableCell({
                        colSpan: 3,
                        style: {backgroundColor: !selectedMove.fen ? "yellow" : null},
                        className: "grey-background-on-hover pointer-on-hover",
                        onClick: () => setSelectedMove({})
                    },
                    "Start"
                )),
                curNode.parsedPgn.positions.map((fullMove,rowIdx) => RE.TableRow({key:rowIdx},
                    RE.TableCell({key:"-1"}, rowIdx),
                    fullMove.map((halfMove,cellIdx) => RE.TableCell({
                            key:cellIdx,
                            style: {backgroundColor: selectedMove.fen == halfMove.fen ? "yellow" : null},
                            className:"grey-background-on-hover pointer-on-hover",
                            onClick: () => setSelectedMove(halfMove),
                        },
                        halfMove.notation
                    ))
                ))
            )
        ))
    }

    function renderCurrentTabContent() {
        if (TABS.pgn.id == currTabId) {
            return renderPgnTab()
        } else if (TABS.moves.id == currTabId) {
            return renderMovesTab()
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