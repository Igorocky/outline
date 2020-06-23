"use strict";

function useMorseTextInput() {
    const MODE_APPEND = "MODE_APPEND"
    const MODE_EDIT = "MODE_EDIT"
    const MODE_INSERT_BEFORE = "MODE_INSERT_BEFORE"
    const [mode, setMode] = useState(MODE_APPEND)
    const [insertBeforeIdx, setInsertBeforeIdx] = useState(null)

    const NULL_FUNCTION = () => null
    const [say, setSay] = useState(() => nullSafeSay(null))
    const [title, setTitle] = useState(null)
    const [onEnter, setOnEnter] = useState(() => NULL_FUNCTION)
    const [onEscape, setOnEscape] = useState(() => NULL_FUNCTION)

    const [userInput, setUserInput] = useState([])

    const {init:initListReader, onSymbolsChanged:onSymbolsChangedInListReader} = useListReader()

    function init({say, title, onEnter, onEscape}) {
        if (say !== undefined) {
            setSay(() => nullSafeSay(say))
        }
        if (title !== undefined) {
            setTitle(title)
            nullSafeSay(say)(title)
        }
        if (onEnter !== undefined) {
            setOnEnter(() => onEnter?onEnter:(NULL_FUNCTION))
        }
        if (onEscape !== undefined) {
            setOnEscape(() => onEscape?onEscape:(NULL_FUNCTION))
        }
        setMode(MODE_APPEND)
        setUserInput([])
    }

    function nullSafeSay(say) {
        return say?say:(str => console.log("useMorseTextInput.say: " + str))
    }

    function onSymbolsChanged(symbols) {
        if (mode == MODE_EDIT) {
            return onSymbolsChangedInListReader(symbols)
        } else if (mode == MODE_INSERT_BEFORE) {
            if (!symbols.length) {
                return symbols
            }
            const last = symbols[symbols.length-1]
            say(last.codeInfo.word + ", inserted.")
            const newUserInput = [...(userInput.filter((e, i) => i < insertBeforeIdx)), {...last.codeInfo}, ...(userInput.filter((e, i) => insertBeforeIdx <= i))];
            setUserInput(newUserInput)
            setMode(MODE_EDIT)
            reInitListReaderForEditMode({userInput: newUserInput, currElemIdx: insertBeforeIdx})
            return [last]
        } else if (mode == MODE_APPEND) {
            if (!symbols.length) {
                return symbols
            }
            const last = symbols[symbols.length-1]
            if (last.sym == MORSE.underscore.sym) {//current context info
                say("Current context is: " + title + ". Append mode. " + (userInput.length ? userInput.map(({word}) => word).join(", ") : "no symbols entered"))
            } else if (last.sym == MORSE.error.sym) {//remove last
                withSound(USE_LIST_READER_PREV_SOUND, () => {
                    if (userInput.length) {
                        userInput.pop()
                    }
                })
            } else if (last.sym == MORSE.quotation.sym) {//edit mode
                say("Edit mode.", () => setMode(MODE_EDIT))
                reInitListReaderForEditMode({userInput, currElemIdx: userInput.length-1})
            } else if (last.sym == MORSE.end.sym) {//enter
                withSound(USE_LIST_READER_ENTER_SOUND, () => {
                    onEnter(userInput)
                })
            } else if (last.sym == MORSE.start.sym) {//escape
                if (onEscape) {
                    withSound(USE_LIST_READER_ESCAPE_SOUND, () => {
                        onEscape(userInput)
                    })
                } else {
                    say("On escape is not defined.")
                }
            } else {//append
                say(last.codeInfo.word)
                setUserInput([...userInput, {...last.codeInfo}])
            }
            return [last]
        }
    }

    function reInitListReaderForEditMode({userInput, currElemIdx}) {
        initListReader({
            say,
            title: {
                say: ({currElemIdx}) => {
                    const textBefore = userInput.filter((codeInfo, i) => i < currElemIdx).map(({word}) => word).join(", ")
                    const textAfter = userInput.filter((codeInfo, i) => currElemIdx <= i).map(({word}) => word).join(", ")
                    say(textBefore, () => beep({frequencyHz:1000, durationMillis: 100, callback: () => window.setTimeout(() => say(textAfter), 0)}))
                },
            },
            currElemIdx,
            elems: userInput.map((codeInfo, idx) => ({
                say: () => say(codeInfo.word),
                onEscape: () => {//delete curr elem
                    const newUserInput = [...(userInput.filter((e, i) => i < idx)), ...(userInput.filter((e, i) => idx < i))];
                    setUserInput(newUserInput)
                    reInitListReaderForEditMode({userInput:newUserInput, currElemIdx: idx < newUserInput.length ? idx : newUserInput.length-1})
                },
                onEnter: () => {//insert before curr
                    say("Insert.")
                    setInsertBeforeIdx(idx)
                    setMode(MODE_INSERT_BEFORE)
                },
                onBack: () => {//return to append mode
                    say("Append mode.")
                    setMode(MODE_APPEND)
                }
            })),
            actionsOnEmptyList: {
                onEnter: () => {//insert before curr
                    say("Insert.")
                    setInsertBeforeIdx(0)
                    setMode(MODE_INSERT_BEFORE)
                },
                onBack: () => {//return to append mode
                    say("Append mode.")
                    setMode(MODE_APPEND)
                }
            }
        })
    }

    return {init, onSymbolsChanged: mode == MODE_EDIT ? onSymbolsChangedInListReader : onSymbolsChanged}
}
