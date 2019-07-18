
const EditableTextArea2 = props => {
    const [editMode, setEditMode] = useState(false)
    const [value, setValue] = useState(props.value)
    const [anchorEl, setAnchorEl] = useState(null);

    function save(value) {
        if (props.onSave(value)) {
            setEditMode(false);
            setAnchorEl(null)
        }
    }

    function viewModeButtons() {
        return [
            iconButton({iconName: "edit", onClick: () => setEditMode(true)}),
            iconButton({iconName: "vertical_align_top", onClick: props.onMoveToStart}),
            iconButton({iconName: "keyboard_arrow_up", onClick: props.onMoveUp}),
            iconButton({iconName: "keyboard_arrow_down", onClick: props.onMoveDown}),
            iconButton({iconName: "vertical_align_bottom", onClick: props.onMoveToEnd}),
            iconButton({iconName: "delete", onClick: props.onDelete}),
        ]
    }
    function editModeButtons() {
        return [
            iconButton({iconName: "save", onClick: () => save(value)}),
            iconButton({iconName: "cancel", onClick: () => {setValue(props.value);setEditMode(false);setAnchorEl(null)}}),
        ]
    }

    return [
        re(TextField, {
            key: "TextField",
            className: "black-text",
            style: props.textAreaStyle,
            multiline: true,
            rowsMax: editMode?30:3000,
            value: value,
            disabled: !editMode,
            variant: editMode ? "outlined" : "standard",
            onChange: e => setValue(e.target.value),
            onDoubleClick: () => !editMode?setAnchorEl(null):null,
            onClick: !editMode ? e => setAnchorEl(e.currentTarget) : e => null
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