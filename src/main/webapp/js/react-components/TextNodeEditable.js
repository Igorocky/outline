
const TextNodeEditable = props => {
    const [editMode, setEditMode] = useState(false)
    const [value, setValue] = useState(props.value)
    const [anchorEl, setAnchorEl] = useState(null)

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

    function performMove(moveFunction) {
        return () => {
            setAnchorEl(null)
            moveFunction()
        }
    }

    function viewModeButtons() {
        return [
            iconButton({iconName: "edit", onClick: () => setEditMode(true)}),
            iconButton({iconName: "vertical_align_top", onClick: performMove(props.onMoveToStart)}),
            iconButton({iconName: "keyboard_arrow_up", onClick: performMove(props.onMoveUp)}),
            iconButton({iconName: "keyboard_arrow_down", onClick: performMove(props.onMoveDown)}),
            iconButton({iconName: "vertical_align_bottom", onClick: performMove(props.onMoveToEnd)}),
            iconButton({iconName: "delete", onClick: props.onDelete}),
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
        if (event.keyCode == 27) {
            cancel()
        }
    }

    return [
        re(TextField, {
            key: "TextField",
            autoFocus: true,
            onKeyDown: onKeyDown,
            className: "black-text",
            style: props.textAreaStyle,
            multiline: true,
            rowsMax: editMode?30:3000,
            value: value?value:"",
            disabled: !editMode,
            variant: editMode ? "outlined" : "standard",
            onChange: e => setValue(e.target.value),
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
    ]
}