"use strict";

function useListReader() {
    const [say, setSay] = useState(() => nullSafeSay(null))
    const [title, setTitle] = useState(null)
    const [elems, setElems] = useState([])
    const [currElemIdx, setCurrElemIdx] = useState(0)
    const [onExit, setOnExit] = useState(() => () => null)

    function init({say, title, elems, onExit}) {
        if (say !== undefined) {
            setSay(nullSafeSay(say))
        }
        if (title !== undefined) {
            setTitle(title)
            nullSafeSay(say)(title)
        }
        if (elems !== undefined) {
            setElems(elems)
            setCurrElemIdx(0)
        }
        if (onExit !== undefined) {
            setOnExit(onExit?onExit:() => null)
        }
    }

    function nullSafeSay(say) {
        return say?say:(str => console.log("useListReader.say: " + str))
    }

    function onSymbolsChanged(symbols) {
        if (!symbols.length) {
            return symbols
        }
        const last = symbols[symbols.length-1]
        if (last.symbol == "N") {
            if (elems.length-1 <= currElemIdx) {
                say("No more elements to read to the right.")
            } else {
                const newCurrElemIdx = currElemIdx+1
                setCurrElemIdx(newCurrElemIdx)
                say(elems[newCurrElemIdx])
            }
        } else if (last.symbol == "P") {
            if (currElemIdx <= 0) {
                say("No more elements to read to the left.")
            } else {
                const newCurrElemIdx = currElemIdx-1
                setCurrElemIdx(newCurrElemIdx)
                say(elems[newCurrElemIdx])
            }
        } else if (last.symbol == "R") {
            sayElem(currElemIdx)
        } else if (last.symbol == "S") {
            setCurrElemIdx(0)
            sayElem(0)
        } else if (last.symbol == "E") {
            const lastElemIdx = elems.length-1
            setCurrElemIdx(lastElemIdx)
            sayElem(lastElemIdx)
        } else if (last.symbol == "?") {
            say(title)
        } else if (last.symbol == "end") {
            onExit()
        } else {
            say("Unexpected command: " + last.codeInfo.word)
        }
        return [last]
    }

    function sayElem(idx) {
        if (elems.length == 0) {
            say("List is empty.")
        } else {
            say(elems[idx])
        }
    }

    return {init, onSymbolsChanged}
}
