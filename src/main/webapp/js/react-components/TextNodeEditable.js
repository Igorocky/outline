
const TextNodeEditable = ({value, textAreaStyle, onSave, popupActions}) => {
    const [editMode, setEditMode] = useState(false)
    const [newValue, setNewValue] = useState(value)
    const [anchorEl, setAnchorEl] = useState(null)

    function save(newValue) {
        onSave({newValue: newValue, onSaved: () => {
                setEditMode(false)
                setAnchorEl(null)
        }})
    }

    function cancel() {
        setNewValue(value);
        setEditMode(false);
        setAnchorEl(null)
    }

    function viewModeButtons() {
        return RE.Fragment({},
            iconButton({iconName: "edit", onClick: () => setEditMode(true)}),
            popupActions?popupActions:null
        )
    }
    function editModeButtons() {
        return [
            iconButton({iconName: "save", onClick: () => save(newValue)}),
            iconButton({iconName: "cancel", onClick: cancel}),
        ]
    }

    function onClick(e) {
        if (!editMode) {
            setAnchorEl(e.currentTarget)
        }
    }

    function onKeyDown(event) {
        if (event.keyCode == 27) {
            cancel()
        }
    }

    return RE.Fragment({},
        RE.TextField({
            key: "TextField",
            autoFocus: true,
            onKeyDown: onKeyDown,
            className: "black-text",
            style: textAreaStyle,
            multiline: true,
            rowsMax: editMode?30:3000,
            value: newValue?newValue:"",
            disabled: !editMode,
            variant: editMode ? "outlined" : "standard",
            onChange: e => setNewValue(e.target.value),
            onDoubleClick: () => !editMode?setAnchorEl(null):null,
            onClick: onClick
        }),
        anchorEl
            ? clickAwayListener({
                key: "Popper",
                onClickAway: () => !editMode ? setAnchorEl(null) : null,
                children: re(Popper, {open: true, anchorEl: anchorEl, placement: 'top-start'},
                    paper(editMode?editModeButtons():viewModeButtons())
                )
            })
            : null
    )
}