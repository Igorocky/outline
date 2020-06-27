"use strict";

function useShortcutsExecutor() {
    const [say, setSay] = useState(() => nullSafeSay(null))
    const [title, setTitle] = useState(null)
    const [actions, setActions] = useState([])
    const [actionOnUnrecognizedShortcut, setActionOnUnrecognizedShortcut] = useState(null)

    const PHASE_ENTER_SHORTCUT = "PHASE_ENTER_SHORTCUT"
    const PHASE_READ_SHORTCUTS = "PHASE_READ_SHORTCUTS"
    const [phase, setPhase] = useState(PHASE_ENTER_SHORTCUT)

    const {init:initListReader, onSymbolsChanged:onSymbolsChangedInListReader} = useListReader()

    function init({say:sayParam, title, actions, sayTitle, actionOnUnrecognizedShortcut}) {
        if (sayParam !== undefined) {
            setSay(() => nullSafeSay(sayParam))
        }
        if (title !== undefined) {
            setTitle(title)
            if (title.say) {
                if (!hasValue(sayTitle) || sayTitle) {
                    title.say()
                }
            }
        }
        setActions(actions)
        setActionOnUnrecognizedShortcut(actionOnUnrecognizedShortcut)
    }

    function nullSafeSay(say) {
        return say?say:(str => console.log("useShortcutsExecutor.say: " + str))
    }

    function onSymbolsChanged(symbols) {
        if (!symbols.length) {
            return symbols
        }
        const last = symbols[symbols.length-1]
        if (last.sym == MORSE.underscore.sym) {//current context info
            say("Current context is: enter shortcut.")
        } else if (last.sym == MORSE.apostrophe.sym) {//read all shortcuts
            say("Current context is: enter shortcut.")
        } else {
            const action = actions.find(({char}) => last.sym == char)
            if (action) {
                action.action()
            } else if (actionOnUnrecognizedShortcut) {
                actionOnUnrecognizedShortcut(last.sym)
            } else {
                say("Unexpected shortcut: " + last.codeInfo.word)
            }
        }
        return [last]
    }

    function charToPhonetic(char) {
        return MORSE_ARR.find(({sym, word}) => sym == char).word
    }

    function reInitListReader() {
        initListReader({
            say,
            title: {say: () => say("Reading shortcuts")},
            elems: actions.map(({char, descr, action}) => ({
                say: () => say(charToPhonetic(char) + ". " + descr),
                onEnter: () => action()
            }))
        })
    }

    return {init, onSymbolsChanged: phase == PHASE_ENTER_SHORTCUT ? onSymbolsChanged : onSymbolsChangedInListReader}
}
