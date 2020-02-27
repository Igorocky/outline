"use strict";

const ChessMoveSelectorM = ({onMoveSelected}) => {
    const [chessmanType, setChessmanType] = useState(null)
    const [additionalCoordType, setAdditionalCoordType] = useState(null)
    const [additionalCoord, setAdditionalCoord] = useState(null)
    const [xCoord, setXCoord] = useState(null)
    const [yCoord, setYCoord] = useState(null)
    const [promotion, setPromotion] = useState(null)

    function renderChessmanTypeSelector() {
        return RE.ButtonGroup({variant:"contained", size:"small"},
            ["K","Q","R","B","N","P"].map(ct => RE.Button(
                {
                    key:ct,
                    color:chessmanType==ct ? "primary" : "default",
                    onClick: () => setChessmanType(ct)
                },
                ct
            ))
        )
    }

    function renderAdditionalCoordSelector() {
        if (additionalCoordType) {
            const possibleAdditionalCoords = additionalCoordType == "x"
                ? ["a", "b", "c", "d", "e", "f", "g", "h"]
                : ["1", "2", "3", "4", "5", "6", "7", "8"];
            return RE.ButtonGroup({variant:"contained", size:"small"},
                RE.Button(
                    {
                        key:"*",
                        color:"default",
                        onClick: () => {
                            setAdditionalCoordType(additionalCoordType=="x"?"y":"x")
                            setAdditionalCoord(null)
                        }
                    },
                    "*"
                ),
                possibleAdditionalCoords.map(ac => RE.Button(
                    {
                        key:ac,
                        color:additionalCoord==ac ? "primary" : "default",
                        onClick: () => setAdditionalCoord(ac)
                    },
                    ac
                ))
            )
        } else {
            return null
        }
    }

    function renderXCoordSelector() {
        return RE.ButtonGroup({variant:"contained", size:"small"},
            ["a","b","c","d","e","f","g","h"].map(coord => RE.Button(
                {
                    key:coord,
                    color:xCoord==coord ? "primary" : "default",
                    onClick: () => setXCoord(coord)
                },
                coord
            ))
        )
    }

    function renderYCoordSelector() {
        return RE.ButtonGroup({variant:"contained", size:"small"},
            ["1","2","3","4","5","6","7","8"].map(coord => RE.Button(
                {
                    key:coord,
                    color:yCoord==coord ? "primary" : "default",
                    onClick: () => setYCoord(coord)
                },
                coord
            ))
        )
    }

    function renderPromotionSelector() {
        if ((yCoord == "1" || yCoord == "8") && chessmanType == "P") {
            return RE.ButtonGroup({variant:"contained", size:"small"},
                ["Q","R","B","N"].map(prom => RE.Button(
                    {
                        key:prom,
                        color:promotion==prom ? "primary" : "default",
                        onClick: () => setPromotion(prom)
                    },
                    prom
                ))
            )
        } else {
            return null
        }
    }

    function renderGoButton() {
        return RE.Button({
                color: "primary",
                variant: "contained",
                disabled: !(xCoord && yCoord),
                onClick: () => {
                    const ct = (chessmanType == "P" || !chessmanType) ? "" : chessmanType
                    const ac = additionalCoord ? additionalCoord : ""
                    const toX = xCoord ? xCoord.toLowerCase() : "-"
                    const toY = yCoord ? yCoord.toLowerCase() : "-"
                    const pr = promotion ? promotion : ""
                    onMoveSelected({move: ct + ac + toX + toY + pr, onDone: () => clearSelection()})
                }
            },
            "Go"
        )
    }

    function clearSelection() {
        setChessmanType(null)
        setAdditionalCoordType(null)
        setAdditionalCoord(null)
        setXCoord(null)
        setYCoord(null)
        setPromotion(null)
    }

    function fromButtonClicked() {
        if (!additionalCoordType) {
            setAdditionalCoordType("x")
        } else {
            setAdditionalCoordType(null)
        }
        setAdditionalCoord(null)
    }

    return RE.Paper({},
        RE.Container.col.top.left({},{style:{marginTop:"0.5em"}},
            RE.Container.row.spaceBetween.center({style:{width:"100%"}},{xs:"auto"},
                "Select move",
                RE.ButtonGroup({size:"small"},
                    RE.Button({onClick: fromButtonClicked},"From"),
                    RE.Button({onClick: clearSelection},"Clear"),
                )
            ),
            renderChessmanTypeSelector(),
            renderAdditionalCoordSelector(),
            renderXCoordSelector(),
            renderYCoordSelector(),
            renderPromotionSelector(),
            renderGoButton(),
        )
    )
}