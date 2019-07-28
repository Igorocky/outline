
const EditableTextField = props => {
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
            iconButton({iconName: "edit", onClick: () => {setEditMode(true);setAnchorEl(null)}}),
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
        if (!editMode && props.viewComponentProvider) {
            return props.viewComponentProvider({value:props.value, onClick:onClick, refCallback: ref=>setAnchorEl(ref)})
        } else {
            return re(TextField, {
                key: "TextField",
                ref:ref,
                autoFocus: true,
                onKeyDown: onKeyDown,
                className: "black-text",
                style: props.textAreaStyle,
                multiline: props.multiline,
                rowsMax: editMode?30:3000,
                value: value?value:"",
                disabled: !editMode,
                variant: editMode ? "outlined" : "standard",
                onChange: e => setValue(e.target.value),
                onDoubleClick: () => !editMode?setAnchorEl(null):null,
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