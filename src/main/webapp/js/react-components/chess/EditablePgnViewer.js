"use strict";

const EditablePgnViewer = ({value, autoResponse, textAreaStyle, onSave, popupActions}) => {
    const [editMode, setEditMode] = useState(false)

    function save(newValue) {
        onSave({newValue: newValue, onSaved: () => setEditMode(false)})
    }

    function cancel() {
        setEditMode(false)
    }

    function viewModeButtons() {
        return RE.Fragment({},
            iconButton({
                iconName: "edit",
                onClick: () => setEditMode(true)
            }),
            popupActions?popupActions:null
        )
    }

    function renderTextOrEditor() {
        if (editMode) {
            return re(PgnEditor, {
                pgnStr:value,
                autoResponse:autoResponse,
                onSave:save,
                onCancel:cancel
            })
        } else {
            return RE.Container.row.left.center({},{},
                viewModeButtons(),
                RE.TextField({
                    className: "black-text",
                    style: textAreaStyle,
                    multiline: true,
                    rowsMax: 3000,
                    value: value?value:"",
                    disabled: true,
                    variant: "standard",
                })
            )
        }
    }

    return renderTextOrEditor()
}