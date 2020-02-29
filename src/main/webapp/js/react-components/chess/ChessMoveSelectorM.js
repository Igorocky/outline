"use strict";

const ChessMoveSelectorM = ({onMoveSelected}) => {
    const [chessmanType, setChessmanType] = useState(null)
    const [additionalCoordType, setAdditionalCoordType] = useState(null)
    const [additionalCoord, setAdditionalCoord] = useState(null)
    const [xCoord, setXCoord] = useState(null)
    const [yCoord, setYCoord] = useState(null)
    const [promotion, setPromotion] = useState(null)

    useEffect(() => {
        if (additionalCoord) {
            setAdditionalCoordType(null)
        }
    }, [additionalCoord])

    function renderChessmanTypeSelector() {
        return re(KeyPad, {
            componentKey: "ChessmanTypeSelector",
            keys: [
                [
                    {symbol:"K", onClick: () => setChessmanType("K")},
                    {symbol:"Q", onClick: () => setChessmanType("Q")},
                    {symbol:"R", onClick: () => setChessmanType("R")},
                ],
                [
                    {symbol:"B", onClick: () => setChessmanType("B")},
                    {symbol:"N", onClick: () => setChessmanType("N")},
                    {symbol:"P", onClick: () => setChessmanType("P")},
                ],
            ]
        })
    }

    function renderAdditionalCoordSelector() {
        const pac = additionalCoordType == "x"
            ? ["a", "b", "c", "d", "e", "f", "g", "h"]
            : ["1", "2", "3", "4", "5", "6", "7", "8"];
        return re(KeyPad, {
            componentKey: "additionalCoordType",
            keys: [
                [
                    {symbol:pac[0], onClick: () => setAdditionalCoord(pac[0])},
                    {symbol:pac[1], onClick: () => setAdditionalCoord(pac[1])},
                    {symbol:pac[2], onClick: () => setAdditionalCoord(pac[2])},
                ],
                [
                    {symbol:pac[3], onClick: () => setAdditionalCoord(pac[3])},
                    {symbol:pac[4], onClick: () => setAdditionalCoord(pac[4])},
                    {symbol:pac[5], onClick: () => setAdditionalCoord(pac[5])},
                ],
                [
                    {symbol:pac[6], onClick: () => setAdditionalCoord(pac[6])},
                    {symbol:pac[7], onClick: () => setAdditionalCoord(pac[7])},
                    {symbol:"*", onClick: () => setAdditionalCoordType(additionalCoordType=="x"?"y":"x")},
                ],
            ]
        })
    }

    function renderXCoordSelector() {
        return re(KeyPad, {
            componentKey: "XCoordSelector",
            keys: [
                [
                    {symbol:"a", onClick: () => setXCoord("a")},
                    {symbol:"b", onClick: () => setXCoord("b")},
                    {symbol:"c", onClick: () => setXCoord("c")},
                ],
                [
                    {symbol:"d", onClick: () => setXCoord("d")},
                    {symbol:"e", onClick: () => setXCoord("e")},
                    {symbol:"f", onClick: () => setXCoord("f")},
                ],
                [
                    {symbol:"g", onClick: () => setXCoord("g")},
                    {symbol:"h", onClick: () => setXCoord("h")},
                    {symbol:"", onClick: () => null},
                ],
            ]
        })
    }

    function renderYCoordSelector() {
        return re(KeyPad, {
            componentKey: "YCoordSelector",
            keys: [
                [
                    {symbol:"1", onClick: () => setYCoord("1")},
                    {symbol:"2", onClick: () => setYCoord("2")},
                    {symbol:"3", onClick: () => setYCoord("3")},
                ],
                [
                    {symbol:"4", onClick: () => setYCoord("4")},
                    {symbol:"5", onClick: () => setYCoord("5")},
                    {symbol:"6", onClick: () => setYCoord("6")},
                ],
                [
                    {symbol:"7", onClick: () => setYCoord("7")},
                    {symbol:"8", onClick: () => setYCoord("8")},
                    {symbol:"", onClick: () => null},
                ],
            ]
        })
    }

    function renderPromotionSelector() {
        return re(KeyPad, {
            componentKey: "PromotionSelector",
            keys: [
                [
                    {symbol:"Q", onClick: () => setPromotion("Q")},
                    {symbol:"R", onClick: () => setPromotion("R")},
                ],
                [
                    {symbol:"B", onClick: () => setPromotion("B")},
                    {symbol:"N", onClick: () => setPromotion("N")},
                ],
            ]
        })
    }

    function getSelectedMove() {
        if (!chessmanType) {
            return ""
        } else {
            const ct = chessmanType == "P" ? "" : chessmanType
            const ac = emptyStrIfNull(additionalCoord)
            const toX = emptyStrIfNull(xCoord).toLowerCase()
            const toY = emptyStrIfNull(yCoord).toLowerCase()
            const pr = emptyStrIfNull(promotion)
            return ct + ac + toX + toY + pr
        }
    }

    const selectedMove = getSelectedMove()

    function renderGoButton() {
        return RE.Button({
                color: "primary",
                variant: "contained",
                onClick: () => {
                    onMoveSelected({move: selectedMove, onDone: () => clearSelection()})
                }
            },
            "Go"
        )
    }

    function renderKeyPad() {
        if (!chessmanType) {
            return renderChessmanTypeSelector()
        } else if (additionalCoordType) {
            return renderAdditionalCoordSelector()
        } else if (!xCoord) {
            return renderXCoordSelector()
        } else if (!yCoord) {
            return renderYCoordSelector()
        } else if (!promotion && chessmanType == "P" && (yCoord == "1" || yCoord == "8")) {
            return renderPromotionSelector()
        } else {
            return null
        }
    }

    function renderSelectedMoveAndControlButtons() {
        return RE.Container.row.right.center({},{style:{marginLeft:"0.5em"}},
            RE.span({style: {fontSize: "x-large"}},selectedMove),
            RE.ButtonGroup({size:"small"},
                RE.Button({onClick: doBackspace, disabled: !chessmanType},RE.Icon({}, "backspace")),
                RE.Button({
                        onClick: fromButtonClicked,
                        disabled: !(chessmanType && !xCoord && !additionalCoord),
                        style: {color:additionalCoordType?"blue":""}
                    },
                    RE.Icon({}, "gps_fixed")
                ),
            )
        )
    }

    function doBackspace() {
        if (promotion) {
            setPromotion(null)
        } else if (yCoord) {
            setYCoord(null)
        } else if (xCoord) {
            setXCoord(null)
        } else if (additionalCoord) {
            setAdditionalCoord(null)
            setAdditionalCoordType(null)
        } else if (chessmanType) {
            setChessmanType(null)
        }
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

    return RE.Container.row.right.top({},{style:{marginLeft:"0.5em"}},
        renderSelectedMoveAndControlButtons(),
        RE.Container.col.top.right({},{style:{marginBottom:"0.5em"}},
            renderKeyPad(),
            renderGoButton(),
        )
    )
}