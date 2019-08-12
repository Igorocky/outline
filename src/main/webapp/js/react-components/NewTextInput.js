
const NewTextInput = props => {
    const [value, setValue] = useState("")

    function save(newValue) {
        props.onSave({newValue: newValue, onSaved: () => {
                setValue("")
        }})
    }

    function cancel() {
        setValue("");
    }

    function onKeyDown(event) {
        if (event.keyCode == 13){
            save(value)
        } else if (event.keyCode == 27) {
            cancel()
        }
    }

    return re(Toolbar, {variant: "dense"},
        re(Typography, {key:"Typography", edge: "start"},
            "New text:"
        ),
        re(InputBase, {
            key: "InputBase",
            onKeyDown: onKeyDown,
            value: value?value:"",
            variant: "outlined",
            onChange: e => setValue(e.target.value),
            style: {
                background: "white",
                padding:"0px 5px",
                borderRadius: "5px",
                marginLeft: "5px",
                width: value === ""?"100px":"500px"
            }
        })
    )
}