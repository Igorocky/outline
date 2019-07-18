
const EditableTextArea2 = props => {
    const [editMode, setEditMode] = useState(false)
    const [value, setValue] = useState(props.value)
    const [anchorEl, setAnchorEl] = useState(null);

    function iconButton({onClick, iconName}) {
        return re(IconButton, {key: iconName, color: "inherit", onClick: onClick},
            re(Icon, {style: {fontSize: "24px"}}, iconName)
        )
    }

    function editButton() {
        return iconButton({onClick: () => setEditMode(true), iconName: "edit"})
    }

    function saveButtonButton() {
        return iconButton({onClick: () => null, iconName: "save"})
    }

    function clickAwayListener(onClickAway, children) {
        return re(ClickAwayListener, {onClickAway: onClickAway}, children)
    }

    return [
        re(TextField, {
            key: "TextField",
            className: "black-text",
            style: props.style,
            multiline: true,
            rowsMax: editMode?30:3000,
            value: value,
            disabled: !editMode,
            variant: editMode ? "outlined" : "standard",
            onChange: e => setValue(e.target.value),
            onDoubleClick: () => !editMode?setAnchorEl(null):null,
            onClick: !editMode ? e => setAnchorEl(e.currentTarget) : e => null
        }),
        //todo - return popper only if it is open
        re(Popper,
            {
                key: "Popper",
                open: Boolean(anchorEl),
                anchorEl: anchorEl,
                placement: 'top-start'
            },
            clickAwayListener(
                () => !editMode?setAnchorEl(null):null,
                paper([editButton(), saveButtonButton()])
            )
        )
    ]
}