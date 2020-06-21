"use strict";

const USE_LIST_READER_NEXT_SOUND = soundUrl("on-next.mp3")
const USE_LIST_READER_PREV_SOUND = soundUrl("on-prev.mp3")
const USE_LIST_READER_GO_TO_START_SOUND = soundUrl("on-go-to-start3.mp3")
const USE_LIST_READER_GO_TO_END_SOUND = soundUrl("on-go-to-end-teleport.mp3")
const USE_LIST_READER_ENTER_SOUND = soundUrl("on-enter2.mp3")
const USE_LIST_READER_BACKSPACE_SOUND = soundUrl("on-backspace.mp3")
const USE_LIST_READER_ESCAPE_SOUND = soundUrl("on-escape.mp3")

function useListReader() {
    const [say, setSay] = useState(() => nullSafeSay(null))
    const [title, setTitle] = useState(null)
    const [elems, setElems] = useState([])
    const [currElemIdx, setCurrElemIdx] = useState(0)

    function withSound(audioFileName, callback) {
        const audio = new Audio(audioFileName);
        audio.play().then(window.setTimeout(callback,500))
    }

    function init({say:sayParam, title, elems, sayCurrentElem, currElemIdx:currElemIdxParam}) {
        if (sayParam !== undefined) {
            setSay(() => nullSafeSay(sayParam))
        }
        if (title !== undefined) {
            setTitle(title)
            if (title.say) {
                if (!sayCurrentElem) {
                    title.say()
                }
            } else {
                say("Title is not defined.")
            }
        }
        const newCurrElemIdx = currElemIdxParam !== undefined ? currElemIdxParam : 0
        setCurrElemIdx(newCurrElemIdx)
        if (elems !== undefined) {
            setElems(elems)
            if (sayCurrentElem) {
                if (elems.length == 0) {
                    say("List is empty.")
                } else {
                    const action = elems[newCurrElemIdx]["say"]
                    if (action) {
                        action()
                    } else {
                        say("Say is not defined on current elem.")
                    }
                }
            }
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
        if (last.sym == MORSE.t.sym) {//ok
            withSound(USE_LIST_READER_NEXT_SOUND, () => {
                if (elems.length-1 <= currElemIdx) {
                    say("No more elements to read to the right.")
                } else {
                    const newCurrElemIdx = currElemIdx+1
                    setCurrElemIdx(newCurrElemIdx)
                    sayElem(newCurrElemIdx)
                }
            })
        } else if (last.sym == MORSE.i.sym) {//ok
            withSound(USE_LIST_READER_PREV_SOUND, () => {
                if (currElemIdx <= 0) {
                    say("No more elements to read to the left.")
                } else {
                    const newCurrElemIdx = currElemIdx - 1
                    setCurrElemIdx(newCurrElemIdx)
                    sayElem(newCurrElemIdx)
                }
            })
        } else if (last.sym == MORSE.e.sym) {//ok
            sayElem(currElemIdx)
        } else if (last.sym == MORSE.o.sym) {//ok
            withSound(USE_LIST_READER_GO_TO_START_SOUND, () => {
                setCurrElemIdx(0)
                sayElem(0)
            })
        } else if (last.sym == MORSE.j.sym) {//ok
            withSound(USE_LIST_READER_GO_TO_END_SOUND, () => {
                const lastElemIdx = elems.length-1
                setCurrElemIdx(lastElemIdx)
                sayElem(lastElemIdx)
            })
        } else if (last.sym == MORSE.a.sym) {//ok
            if (title && title.say) {
                title.say()
            } else {
                say("Title is not defined.")
            }
        } else if (last.sym == MORSE.u.sym) {//ok
            if (title && title.spell) {
                title.spell()
            } else {
                say("Title spell is not defined.")
            }
        } else if (last.sym == MORSE.m.sym) {//ok
            withSound(USE_LIST_READER_ENTER_SOUND, () => onAction(currElemIdx, "onEnter", () => say("On enter is undefined.")))
        } else if (last.sym == MORSE.n.sym) {//ok
            onAction(currElemIdx, "onSpell", () => say("On spell is undefined."))
        } else if (last.sym == MORSE.s.sym) {//ok
            withSound(USE_LIST_READER_BACKSPACE_SOUND, () => onAction(currElemIdx, "onBack", () => say("On back is undefined.")))
        } else if (last.sym == MORSE.error.sym) {//ok
            withSound(USE_LIST_READER_ESCAPE_SOUND, () => onAction(currElemIdx, "onEscape", () => say("On escape is undefined.")))
        } else {
            say("Unexpected command: " + last.codeInfo.word)
        }
        return [last]
    }

    function sayElem(idx) {
        onAction(idx, "say")
    }

    function onAction(idx, actionName, onActionIsUndefined) {
        if (elems.length == 0) {
            say("List is empty.")
        } else {
            const action = elems[idx][actionName];
            if (action) {
                action()
            } else if (onActionIsUndefined) {
                onActionIsUndefined()
            }
        }
    }

    return {init, onSymbolsChanged}
}
