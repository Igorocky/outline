
const EditableTextField = ({placeholder, initialValue, variant, spanStyle, textFieldStyle, onSave,
                               popupActions, inlineActions}) => {
    const [editMode, setEditMode] = useState(false)
    const [value, setValue] = useState(initialValue)
    const [anchorEl, setAnchorEl] = useState(null);
    const ref = React.useRef(null)

    useEffect(() => {
        if (editMode && ref.current) {
            setAnchorEl(ref.current)
        }
    })

    function save(newValue) {
        onSave({newValue: newValue, onSaved: () => {
                setEditMode(false)
                setAnchorEl(null)
        }})
    }

    function cancel() {
        setValue(initialValue);
        setEditMode(false);
        setAnchorEl(null)
    }

    function viewModeButtons() {
        return RE.Fragment({},
            iconButton({iconName: "edit", onClick: () => {setEditMode(true);setAnchorEl(null)}}),
            popupActions
        )
    }
    function editModeButtons() {
        return RE.Fragment({},
            iconButton({iconName: "save", onClick: () => save(value)}),
            iconButton({iconName: "cancel", onClick: cancel})
        )
    }

    function onClick(e) {
        if (!inlineActions && !editMode) {
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
            return RE.span(
                {onClick:onClick,
                    style:initialValue?spanStyle:{...spanStyle, color: "lightgrey"}},
                initialValue?initialValue:placeholder
            )
        } else {
            return RE.TextField({
                ref:ref,
                autoFocus: true,
                style: textFieldStyle,
                onKeyDown: onKeyDown,
                value: value?value:"",
                variant: "outlined",
                onChange: e => setValue(e.target.value),
                onClick: onClick
            })
        }
    }

    function renderPopUp() {
        if (anchorEl) {
            return clickAwayListener({
                key: "Popper",
                onClickAway: () => !editMode ? setAnchorEl(null) : null,
                children: re(Popper, {open: true, anchorEl: anchorEl, placement: 'top-start'},
                    paper(editMode?editModeButtons():viewModeButtons())
                )
            })
        } else {
            return null
        }
    }

    return RE.Fragment({},
        (inlineActions && !editMode)?viewModeButtons():null,
        renderTextField(),
        (inlineActions && editMode)?editModeButtons():null,
        !inlineActions?renderPopUp():null,
    )
}