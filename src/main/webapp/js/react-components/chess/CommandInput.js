const fontSize = "20px";

const CommandInput = ({style, onExecCommand, responseMsg, errorMsg, onClickAway}) => {
    const [commandStr, setCommandStr] = useState(null)
    const [anchorEl, setAnchorEl] = useState(null);
    const ref = React.useRef(null)
    const [history, setHistory] = useState([]);
    const [historyFilter, setHistoryFilter] = useState(null);
    const [historyIndex, setHistoryIndex] = useState(null);

    useEffect(() => {
        if (ref.current && (errorMsg || responseMsg)) {
            setAnchorEl(ref.current)
        } else {
            setAnchorEl(null)
        }
    }, [ref.current, errorMsg, responseMsg])

    useEffect(() => {
        if (!isCommandNotEmpty()) {
            resetSearchHistoryState()
        }
    }, [commandStr])

    function setNextCommandFromHistory(toBeginning, filter) {
        const sizeOfHistory = _.size(history);
        if (sizeOfHistory <= 0) {
            return
        }
        let idx = (historyIndex!=null)?historyIndex:(toBeginning?sizeOfHistory:-1)
        idx = idx + (toBeginning?-1:1)
        while (toBeginning?(idx >= 0):(idx < sizeOfHistory)) {
            let candidateCommand = history[idx]
            if (filter==null || candidateCommand.includes(filter)) {
                setHistoryIndex(idx)
                setHistoryFilter(filter)
                setCommandStr(candidateCommand)
                return
            }
            idx = idx + (toBeginning?-1:1)
        }
    }

    function isCommandNotEmpty() {
        return commandStr && commandStr.trim().length > 0
    }

    function resetSearchHistoryState() {
        setHistoryIndex(null)
        setHistoryFilter(null)
    }

    function execCommand() {
        if (isCommandNotEmpty()) {
            setAnchorEl(null)
            setCommandStr(null)
            resetSearchHistoryState()
            const trimmedCommandStr = commandStr.trim();
            setHistory([trimmedCommandStr, ...(history.filter(cmd => cmd != trimmedCommandStr))])
            onExecCommand(trimmedCommandStr)
        }
    }

    function cancel() {
        setCommandStr(null)
        setHistoryIndex(null)
        setHistoryFilter(null)
    }

    function getFilter() {
        if (historyFilter) {
            return historyFilter
        } else if (historyIndex == null && isCommandNotEmpty()) {
            return commandStr
        } else {
            return null
        }
    }

    function onKeyDown(event) {
        if (event.keyCode == ENTER_KEY_CODE){
            execCommand()
        } else if (event.keyCode == ESC_KEY_CODE) {
            cancel()
        } else if (event.keyCode == UP_KEY_CODE) {
            setNextCommandFromHistory(false, getFilter())
        } else if (event.keyCode == DOWN_KEY_CODE) {
            setNextCommandFromHistory(true, getFilter())
        }
    }

    function renderTextField() {
        return re('input', {
            key: "CommandInput-TextField",
            type:"text",
            ref:ref,
            autoFocus: true,
            style: {fontSize: fontSize, ...style},
            onKeyDown: onKeyDown,
            value: commandStr?commandStr:"",
            variant: "outlined",
            onChange: e => setCommandStr(e.target.value)
        })
    }

    return [
        renderTextField(),
        anchorEl
            ? clickAwayListener({
                key: "CommandInput-Popper",
                onClickAway: onClickAway,
                children: re(Popper, {open: true, anchorEl: anchorEl, placement: 'top-start'},
                    paper(re('span',{style:{...(errorMsg?{color:"red"}:{}), fontSize: fontSize}},errorMsg?errorMsg:responseMsg))
                )
            })
            : null
    ]
}