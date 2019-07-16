
class EditableTextArea extends React.Component {
    constructor(props) {
        super(props)
        this.state = {
            value: props.value,
            editMode: false
        }
        this.changeState = this.changeState.bind(this)
    }

    render() {
        return re(Paper,{},
            re(Grid, {container:true, direction:"column", justify:"flex-start", alignItems:"flex-start"},
                re(Grid, {item:true},
                    re(IconButton, {color: "inherit",
                            onClick: () => !this.state.editMode
                                ?this.changeState("editMode", true)
                                :this.props.onSave(this.state.value, () => this.changeState("editMode", false))
                        },
                        re(Icon, {style: {fontSize: "24px"}}, this.state.editMode?"save":"edit")
                    )
                ),
                re(Grid, {item:true},
                    re(TextField,{className: "black-text",
                        style:{width:"1000px", margin:"0px 0px 10px 10px",
                            ...(this.props.textFieldStyle?this.props.textFieldStyle:{})},
                        multiline:true, rowsMax:30,
                        value: this.state.value,
                        disabled: !this.state.editMode,
                        variant: this.state.editMode?"outlined":"standard",
                        onChange: e => this.changeState("value", e.target.value)
                    })
                )
            )
        )
    }

    changeState(prop,value) {
        this.setState({[prop]:value})
    }
}
