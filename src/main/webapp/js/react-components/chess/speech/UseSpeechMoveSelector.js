"use strict";

function useSpeechMoveSelector() {
    const [say, setSay] = useState(() => nullSafeSay(null))
    const [title, setTitle] = useState(null)
    const [onMoveSelected, setOnMoveSelected] = useState(() => () => null)
    const [userInputFallback, setUserInputFallback] = useState(() => symbols => symbols)

    const [chessmanType, setChessmanType] = useState(null)
    const [additionalCoordType, setAdditionalCoordType] = useState(null)
    const [additionalCoord, setAdditionalCoord] = useState(null)
    const [xCoord, setXCoord] = useState(null)
    const [yCoord, setYCoord] = useState(null)
    const [promotion, setPromotion] = useState(null)
    const [saySelectedMove, setSaySelectedMove] = useState(false)

    useEffect(() => {
        if (dosaySelectedMove) {
            doSaySelectedMove()
            setSaySelectedMove(false)
        }
    }, [saySelectedMove])

    function init({say, title, onMoveSelected, userInputFallback}) {
        if (say !== undefined) {
            setSay(nullSafeSay(say))
        }
        if (title !== undefined) {
            setTitle(title)
            nullSafeSay(say)(title)
        }
        if (onMoveSelected !== undefined) {
            setOnMoveSelected(onMoveSelected?onMoveSelected:() => null)
        }
        if (userInputFallback !== undefined) {
            setUserInputFallback(userInputFallback?userInputFallback:symbols => symbols)
        }
    }

    function nullSafeSay(say) {
        return say?say:(str => console.log("useSpeechMoveSelector.say: " + str))
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

    function isXCoord(symbol) {
        return ["A","B","C","D","E","F","G","H"].includes(symbol)
    }

    function isYCoord(symbol) {
        return ["1","2","3","4","5","6","7","8"].includes(symbol)
    }

    function isAdditionalCoord(symbol) {
        return additionalCoordType == "X" && isXCoord(symbol)
            || additionalCoordType == "Y" && isYCoord(symbol)
    }

    function isPromotionNeeded(yCoord) {
        return chessmanType == "P" && (yCoord == "1" || yCoord == "8")
    }

    const promotionIsNeeded = isPromotionNeeded(yCoord)

    const CHESSMAN_TYPES = {"P": "Pawn", "N": "Knight", "B": "Bishop", "R": "Rook", "Q": "Queen", "K": "King",}
    function onSymbolsChanged(symbols) {
        if (!symbols.length) {
            return symbols
        }
        const last = symbols[symbols.length-1]
        if (last.symbol == "error") {
            doBackspace()
            setSaySelectedMove(true)
        } else if (last.symbol == "?") {
            say(title, doSaySelectedMove)
        } else if (!chessmanType && last.symbol == "start") {
            go()
        } else if (!chessmanType && CHESSMAN_TYPES[last.symbol]) {
            setChessmanType(last.symbol)
            say(CHESSMAN_TYPES[last.symbol])
        } else if (!xCoord && !additionalCoord && (last.symbol == "X" || last.symbol == "Y")) {
            setAdditionalCoordType(last.symbol)
            say("Enter additional " + last.symbol + " coordinate.")
        } else if (isAdditionalCoord(last.symbol)) {
            setAdditionalCoord(last.symbol)
            setAdditionalCoordType(null)
            say(last.symbol)
        } else if (!xCoord && isXCoord(last.symbol)) {
            setXCoord(last.symbol)
            say(last.symbol)
        } else if (!yCoord && isYCoord(last.symbol)) {
            setYCoord(last.symbol)
            say(last.symbol, () => {
                if (isPromotionNeeded(last.symbol)) {
                    setSaySelectedMove(true)
                }
            })
        } else if (!promotion && promotionIsNeeded && CHESSMAN_TYPES[last.symbol]) {
            setPromotion(last.symbol)
        } else {
            userInputFallback(symbols)
        }
        return [last]
    }

    function go() {
        onMoveSelected(getSelectedMove(), () => clearSelection())
    }

    function doSaySelectedMove() {
        if (chessmanType) {
            return "Nothing was entered."
        } else {
            const wordsToSay = []
            if (chessmanType) {
                wordsToSay.push(CHESSMAN_TYPES[chessmanType])
            }
            if (additionalCoord) {
                wordsToSay.push(additionalCoord)
            }
            if (xCoord) {
                wordsToSay.push(xCoord)
            }
            if (yCoord) {
                wordsToSay.push(yCoord)
            }
            if (promotion) {
                wordsToSay.push("Promotes to " + CHESSMAN_TYPES[promotion])
            }
            say(wordsToSay.reduce((m,e) => m + ". " + e, ""))
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

    function doBackspace() {
        if (promotion) {
            setPromotion(null)
        } else if (yCoord) {
            setYCoord(null)
        } else if (xCoord) {
            setXCoord(null)
        } else if (additionalCoord) {
            setAdditionalCoord(null)
        } else if (additionalCoordType) {
            setAdditionalCoordType(null)
        } else if (chessmanType) {
            setChessmanType(null)
        }
    }

    return {init, onSymbolsChanged}
}
