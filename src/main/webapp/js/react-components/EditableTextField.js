
const EditableTextField = ({placeholder, initialValue, variant, typographyStyle, textFieldStyle, onSave}) => {
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
            return RE.Typography(
                {variant:variant, onClick:onClick,
                    style:initialValue?typographyStyle:{...typographyStyle, color: "lightgrey"}},
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

    return RE.Fragment({},
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
    )
}