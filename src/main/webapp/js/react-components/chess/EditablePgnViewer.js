"use strict";

const EditablePgnViewer = ({value, textAreaStyle, onSave, popupActions}) => {
    const [editMode, setEditMode] = useState(false)
    const [anchorEl, setAnchorEl] = useState(null)

    function save(newValue) {
        onSave({newValue: newValue, onSaved: () => {
                setEditMode(false)
                setAnchorEl(null)
        }})
    }

    function cancel() {
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
            iconButton({iconName: "save", onClick: () => null}),
            iconButton({iconName: "cancel", onClick: cancel}),
        ]
    }

    function onClick(e) {
        if (!editMode) {
            setAnchorEl(e.currentTarget)
        }
    }

    function renderTextOrEditor() {
        if (editMode) {
            return re(PgnEditor, {pgnStr:value})
        } else {
            return RE.TextField({
                className: "black-text",
                style: textAreaStyle,
                multiline: true,
                rowsMax: 3000,
                value: value,
                disabled: true,
                variant: "standard",
                onDoubleClick: () => setAnchorEl(null),
                onClick: onClick
            })
        }
    }

    return RE.Fragment({},
        renderTextOrEditor(),
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