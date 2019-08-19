
const CommandInput = ({onExecCommand, style}) => {
    const [commandStr, setCommandStr] = useState(null)
    const [errorMsg, setErrorMsg] = useState(null)
    const [anchorEl, setAnchorEl] = useState(null);
    const ref = React.useRef(null)

    useEffect(() => {
        if (ref.current && errorMsg) {
            setAnchorEl(ref.current)
        } else {
            setAnchorEl(null)
        }
    }, [ref.current, errorMsg])

    function execCommand() {
        onExecCommand({commandStr: commandStr, onDone: ({errorMsg}) => {
            if (errorMsg) {
                setErrorMsg(errorMsg)
            } else {
                setErrorMsg(null)
                setCommandStr(null)
            }
        }})
    }

    function cancel() {
        setCommandStr(null);
        setErrorMsg(null);
    }

    function onKeyDown(event) {
        if (event.keyCode == 13){
            execCommand()
        } else if (event.keyCode == 27) {
            cancel()
        }
    }

    function renderTextField() {
        return re('input', {
            key: "CommandInput-TextField",
            type:"text",
            ref:ref,
            // autoFocus: true,
            style: style,
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
                onClickAway: () => setAnchorEl(null),
                children: re(Popper, {open: true, anchorEl: anchorEl, placement: 'top-start'},
                    paper(re('span',{style:{color:"red"}},errorMsg))
                )
            })
            : null
    ]
}