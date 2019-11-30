'use strict'

const ChessGameShortView = ({node, navigateToNodeId, reloadParentNode, createLink}) => {
    return re(FolderComponent,{
        text:getTagSingleValue(node, TAG_ID.name, node[NODE.objectClass]),
        props: createLink(PATH.createNodeWithIdPath(node[NODE.id])),
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

const CHESS_GAME_FULL_VIEW_TABS = {
    pgn:{title: "PGN", id: "PGN"},
    moves:{title: "Moves", id: "Moves"},
    practice:{title: "Practice", id: "Practice"},
}

const ChessGameFullView = ({curNode, actionsContainerRef, navigateToNodeId}) => {
    const [currTabId, setCurrTabId] = useState(curNode.parsedPgn ? CHESS_GAME_FULL_VIEW_TABS.moves.id : CHESS_GAME_FULL_VIEW_TABS.pgn.id)
    const [selectedMoveIdx, setSelectedMoveIdx] = useState(-1)
    const [flipped, setFlipped] = useState(false)
    const [analysisWindowIsOpened, setAnalysisWindowIsOpened] = useState(false)
    const [showArrow, setShowArrow] = useState(null)
    const allHalfMoves = flatMap(curNode.parsedPgn.positions, fullMove => fullMove)

    useEffect(() => setShowArrow(null),[selectedMoveIdx])

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

    function getHalfMove(halfMoveIdx) {
        if (halfMoveIdx < 0) {
            return null
        } else {
            return allHalfMoves[halfMoveIdx]
        }
    }

    function getSelectedHalfMove() {
        return getHalfMove(selectedMoveIdx)
    }

    function getCurrentFen() {
        return getByPath(getSelectedHalfMove(), ["fen"], curNode.parsedPgn.initialPositionFen)
    }

    function renderChessBoard() {
        const prevMoveIdx = selectedMoveIdx>-1?selectedMoveIdx-1:selectedMoveIdx;
        const numberOfHalfMoves = _.size(allHalfMoves)
        const nextMoveIdx = selectedMoveIdx<numberOfHalfMoves-1?selectedMoveIdx+1:selectedMoveIdx;
        const currFen = getCurrentFen()
        return RE.Container.col.top.center({},{},
            re(ChessBoardFromFen, {
                fen:currFen,
                moveFromTo:getByPath(getSelectedHalfMove(), ["move"]),
                wPlayer:curNode.parsedPgn.wplayer,
                bPlayer:curNode.parsedPgn.bplayer,
                flipped: flipped,
                arrow: showArrow
            }),
            RE.ButtonGroup({variant:"contained", size:"small"},
                RE.Button({},RE.Icon({onClick: () => setFlipped(!flipped)}, "cached")),
                RE.Button({disabled:selectedMoveIdx==-1, onClick: () => setSelectedMoveIdx(-1)},
                    RE.Icon({}, "fast_rewind")),
                RE.Button({disabled:selectedMoveIdx==prevMoveIdx, onClick: () => setSelectedMoveIdx(prevMoveIdx)},
                    RE.Icon({style:{transform: "scaleX(-1)"}}, "play_arrow")),
                RE.Button({disabled:selectedMoveIdx==nextMoveIdx, onClick: () => setSelectedMoveIdx(nextMoveIdx)},
                    RE.Icon({}, "play_arrow")),
                RE.Button({disabled:selectedMoveIdx==numberOfHalfMoves - 1,
                        onClick: () => setSelectedMoveIdx(numberOfHalfMoves - 1)},
                    RE.Icon({}, "fast_forward")),
                RE.Button({
                        onClick: () => window.open(
                            "https://lichess.org/analysis/standard/" + currFen.replace(" ","_")
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
            RE.Container.col.top.left({},{style:{marginTop: "10px"}},
                RE.Button({onClick:()=>setAnalysisWindowIsOpened(true)}, "Analyse"),
                renderPossibleMoves(),
                renderCurrentPositionFen()
            )
        )
    }

    function getStyleForDelta(delta) {
        let color
        let weight
        if (delta > 0) {
            color = "green"
        } else if (Math.abs(delta) < 100) {
            color = "lightgray"
        } else if (Math.abs(delta) < 200) {
            color = "orange"
        } else {
            color = "red"
            weight = "bold"
        }
        return {fontWeight: weight, color:color}
    }

    function getStyleForScore(score) {
        if (score >= 0) {
            return {backgroundColor: "white", color:"black"}
        } else {
            return {backgroundColor: "grey", color:"white"}
        }
    }

    function renderMoveInfo(halfMove) {
        let score
        let scoreStr
        let delta
        if (halfMove.analysis) {
            if (halfMove.analysis.possibleMoves && halfMove.analysis.possibleMoves.length > 0) {
                const bestMove = halfMove.analysis.possibleMoves[0]
                if (bestMove.mate) {
                    score = bestMove.mate
                    scoreStr = "#" + bestMove.mate
                } else {
                    score = bestMove.score
                    scoreStr = parseFloat(bestMove.score / 100).toFixed(2)
                }
            }
            delta = halfMove.analysis.delta
        }
        let scoreInfo = (score || score==0 || delta || delta==0) ? RE.span({},
            (score || score == 0)
                ? RE.span({style: {...getStyleForScore(score), padding: "0px 5px", border: "1px solid grey"}},
                    scoreStr
                )
                : "-",
            (delta||delta==0)
                ? RE.span({style:{...getStyleForDelta(delta), padding:"0px 5px"}},
                    parseFloat(delta / 100).toFixed(2)
                )
                : "-",
        ) : null
        return RE.Fragment({},
            RE.span({style:{fontWeight:"bold"}}, halfMove.notation + " "),
            scoreInfo
        )
    }

    function renderCurrentPositionFen() {
        const currentFen = getCurrentFen()
        if (currentFen) {
            return RE.span({}, "FEN: " + currentFen)
        } else {
            return null
        }
    }

    function renderPossibleMoves() {
        const selectedHalfMove = getSelectedHalfMove()
        if (getByPath(selectedHalfMove, ["analysis", "possibleMoves"])) {
            return RE.Paper({},RE.Table({size:"small"},
                RE.TableBody({},
                    selectedHalfMove.analysis.possibleMoves.map((possMove,rowIdx) => RE.TableRow({
                            key:rowIdx,
                            className: "grey-background-on-hover pointer-on-hover",
                            onMouseEnter: () => setShowArrow(possMove.move),
                            onMouseLeave: () => setShowArrow(null)
                        },
                        RE.TableCell({},
                            (possMove.mate || possMove.mate==0)
                                ?('#'+possMove.mate)
                                :parseFloat(possMove.score / 100).toFixed(2)
                        ),
                        RE.TableCell({}, possMove.move),
                        RE.TableCell({}, "depth = " + possMove.depth),
                    ))
                )
            ))
        }
    }

    function renderTableWithMoves() {
        return RE.Paper({style:{maxHeight:"450px", overflow: "scroll"}},RE.Table({size:"small"},
            RE.TableBody({},
                RE.TableRow({key: "-1"}, RE.TableCell({
                        colSpan: 3,
                        style: {backgroundColor: selectedMoveIdx==-1 ? "yellow" : null},
                        className: "grey-background-on-hover pointer-on-hover",
                        onClick: () => setSelectedMove({})
                    },
                    "Start"
                )),
                curNode.parsedPgn.positions.map((fullMove,rowIdx) => RE.TableRow({key:rowIdx},
                    RE.TableCell({key:"-1"}, rowIdx+1),
                    fullMove.map((halfMove,cellIdx) => RE.TableCell({
                            key:cellIdx,
                            style: {backgroundColor: selectedMoveIdx == rowIdx*2+cellIdx ? "yellow" : null},
                            className:"grey-background-on-hover pointer-on-hover",
                            onClick: () => setSelectedMoveIdx(rowIdx*2+cellIdx),
                        },
                        renderMoveInfo(halfMove)
                    ))
                ))
            )
        ))
    }

    function renderTabs() {
        return reTabs({
            selectedTab:currTabId,
            onTabSelected: setCurrTabId,
            tabs: {
                [CHESS_GAME_FULL_VIEW_TABS.pgn.id]: {
                    label:CHESS_GAME_FULL_VIEW_TABS.pgn.title,
                    render: renderPgnTab
                },
                [CHESS_GAME_FULL_VIEW_TABS.moves.id]: {
                    label:CHESS_GAME_FULL_VIEW_TABS.moves.title,
                    render: renderMovesTab
                },
                [CHESS_GAME_FULL_VIEW_TABS.practice.id]: {
                    label:CHESS_GAME_FULL_VIEW_TABS.practice.title,
                    render: () => "practice"
                },
            }
        })
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