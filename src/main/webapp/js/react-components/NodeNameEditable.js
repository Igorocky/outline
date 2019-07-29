
const NodeNameEditable = props => {
    const [editMode, setEditMode] = useState(false)
    const [value, setValue] = useState(props.value)
    const [anchorEl, setAnchorEl] = useState(null);
    const ref = React.useRef(null)

    useEffect(() => {
        if (editMode && ref.current) {
            setAnchorEl(ref.current)
        }
    })

    function save(newValue) {
        props.onSave({newValue: newValue, onSaved: () => {
                setEditMode(false)
                setAnchorEl(null)
        }})
    }

    function cancel() {
        setValue(props.value);
        setEditMode(false);
        setAnchorEl(null)
    }

    function viewModeButtons() {
        return [
            iconButton({iconName: "edit", onClick: () => {setEditMode(true);setAnchorEl(null)}})
        ]
    }
    function editModeButtons() {
        return [
            iconButton({iconName: "save", onClick: () => save(value)}),
            iconButton({iconName: "cancel", onClick: cancel}),
        ]
    }

    function onClick(e) {
        if (!editMode) {
            setAnchorEl(e.currentTarget)
        }
    }

    function onKeyDown(event) {
        if (event.keyCode == 13){
            save(value)
        } else if (event.keyCode == 27) {
            cancel()
        }
    }

    function renderTextField() {
        if (!editMode) {
            return re(Typography,
                {key:"NodeNameEditable-Typography", variant:"h5", onClick:onClick,
                    style:value?props.style:{...props.style, color: "lightgrey"}},
                value?value:"Enter node name here"
            )
        } else {
            return re(TextField, {
                key: "NodeNameEditable-TextField",
                ref:ref,
                autoFocus: true,
                style: props.style,
                onKeyDown: onKeyDown,
                value: value?value:"",
                variant: "outlined",
                onChange: e => setValue(e.target.value),
                onClick: onClick
            })
        }
    }

    return [
        renderTextField(),
        anchorEl
            ? clickAwayListener({
                key: "Popper",
                onClickAway: () => !editMode ? setAnchorEl(null) : null,
                children: re(Popper, {open: true, anchorEl: anchorEl, placement: 'top-start'},
                    paper(editMode?editModeButtons():viewModeButtons())
                )
            })
            : null
    ]
}