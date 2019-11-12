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
    const [flipped, setFlipped] = useState(false)
    const [analysisWindowIsOpened, setAnalysisWindowIsOpened] = useState(false)

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

    function findPositionsToNavigateTo() {
        let result = {}
        const allHalfMoves = flatMap(curNode.parsedPgn.positions, fullMove => fullMove);
        if (!selectedMove.fen) {
            result = {next: allHalfMoves[0]}
        } else {
            result = {...result, prev: {}}
            allHalfMoves.forEach(halfMove => {
                if (!result.next) {
                    if (halfMove.fen == selectedMove.fen) {
                        result = {...result, current: halfMove}
                    } else if (result.current) {
                        result = {...result, next: halfMove}
                    } else {
                        result = {...result, prev: halfMove}
                    }
                }
            })
        }
        result = {
            ...result,
            first: result.prev?{}:null,
            last: result.next?allHalfMoves[allHalfMoves.length-1]:null
        }
        return result
    }

    function renderChessBoard() {
        const {first, prev, next, last} = findPositionsToNavigateTo()
        return RE.Container.col.top.center({},{},
            re(ChessBoardFromFen, {
                fen:selectedMove.fen,
                moveFromTo:selectedMove.move,
                wPlayer:curNode.parsedPgn.wplayer,
                bPlayer:curNode.parsedPgn.bplayer,
                flipped: flipped
            }),
            RE.ButtonGroup({variant:"contained", size:"small"},
                RE.Button({},RE.Icon({onClick: () => setFlipped(!flipped)}, "cached")),
                RE.Button({disabled:!first, onClick: () => setSelectedMove(first)},
                    RE.Icon({}, "fast_rewind")),
                RE.Button({disabled:!prev, onClick: () => setSelectedMove(prev)},
                    RE.Icon({style:{transform: "scaleX(-1)"}}, "play_arrow")),
                RE.Button({disabled:!next, onClick: () => setSelectedMove(next)},
                    RE.Icon({}, "play_arrow")),
                RE.Button({disabled:!last, onClick: () => setSelectedMove(last)},
                    RE.Icon({}, "fast_forward")),
                RE.Button({
                        disabled:!selectedMove.fen,
                        onClick: () => window.open(
                            "https://lichess.org/analysis/standard/" + selectedMove.fen.replace(" ","_")
                        )
                    },
                    RE.Icon({}, "equalizer")),
            )
        )
    }

    function renderMovesTab() {
        return RE.Container.row.left.top({},{},
            renderChessBoard(),
            renderTableWithMoves(),
            RE.Button({onClick:()=>setAnalysisWindowIsOpened(true)}, "Analyse")
        )
    }

    function renderMoveInfo(halfMove) {
        let score
        let delta
        if (halfMove.analysis) {
            if (halfMove.analysis.possibleMoves && halfMove.analysis.possibleMoves.length > 0) {
                const bestMove = halfMove.analysis.possibleMoves[0]
                if (bestMove.mate) {
                    score = "#" + bestMove.mate
                } else {
                    score = bestMove.score
                }
            }
            delta = halfMove.analysis.delta
        }
        let scoreInfo = (score || score==0 || delta || delta==0) ? RE.span({},
            "[" + ((score || score == 0) ? score : "-") + "|",
            (delta||delta==0) ? RE.span({style:{color:delta > 0?"green":"red"}}, delta) : "-",
            "]"
        ) : null
        return RE.Fragment({},
            RE.span({style:{fontWeight:"bold"}}, halfMove.notation + " "),
            scoreInfo
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
                    RE.TableCell({key:"-1"}, rowIdx+1),
                    fullMove.map((halfMove,cellIdx) => RE.TableCell({
                            key:cellIdx,
                            style: {backgroundColor: selectedMove.fen == halfMove.fen ? "yellow" : null},
                            className:"grey-background-on-hover pointer-on-hover",
                            onClick: () => setSelectedMove(halfMove),
                        },
                        renderMoveInfo(halfMove)
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
            renderTabs(),
            analysisWindowIsOpened
                ?re(GameAnalysisWindow, {gameId:getCurrGameId(), onDone: () => {
                    setAnalysisWindowIsOpened(false)
                    reloadCurrNode()
                }})
                :null
        )
    )
}

const GameAnalysisWindow = ({gameId, onDone}) => {
    const [analysisProgressInfo, setAnalysisProgressInfo] = useState({})
    useBackend({
        stateType: "PgnAnalyser",
        onBackendStateCreated: backend => backend.call("analyseGame", {gameId:gameId}),
        onMessageFromBackend: resp => {
            if ("done" == resp) {
                onDone()
            } else {
                setAnalysisProgressInfo(resp)
            }
        }
    })

    return RE.Dialog({open:true},
        RE.DialogTitle({},
            "Analysis is in progress..."
        ),
        RE.DialogContent({},
            JSON.stringify(analysisProgressInfo)
        )
    )
}