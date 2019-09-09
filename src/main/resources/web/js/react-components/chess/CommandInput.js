const fontSize = "20px";

const CommandInput = ({onExecCommand, responseMsg, errorMsg}) => {
    const [commandStr, setCommandStr] = useState(null)
    const [anchorEl, setAnchorEl] = useState(null);
    const ref = React.useRef(null)

    useEffect(() => {
        if (ref.current && (errorMsg || responseMsg)) {
            setAnchorEl(ref.current)
        } else {
            setAnchorEl(null)
        }
    }, [ref.current, errorMsg, responseMsg])

    function execCommand() {
        setAnchorEl(null)
        setCommandStr(null)
        onExecCommand(commandStr)
    }

    function cancel() {
        setCommandStr(null);
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
            style: {fontSize: fontSize},
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
                    paper(re('span',{style:{...(errorMsg?{color:"red"}:{}), fontSize: fontSize}},errorMsg?errorMsg:responseMsg))
                )
            })
            : null
    ]
}