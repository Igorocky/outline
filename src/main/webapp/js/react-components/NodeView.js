const NODE_VIEW_LISTENER_NAME = "NODE_VIEW_LISTENER"

class NodeView extends React.Component {
    constructor(props) {
        super(props)
    }

    render() {
        return re(EditableTextArea,
            {
                value:"1+asdasd asg adfg sdhf dsfh sfg\n2+dgh kjdghsfg hsfg jfsg\n3+sasd fas dfsd ",
                onSave: (newValue, onSuccess) =>
                    updateTextOfTextNode({id:"guid-guid-123", text:newValue, onSuccess:onSuccess})
            }
        )
    }

    componentDidMount() {
        const style = {marginRight: "10px"}
        addMessageListener({name: NODE_VIEW_LISTENER_NAME, callback: msg => console.log("msg = '" + msg + "'")})
        this.props.setActions([
            re(Button,{key:"action1", style:style, variant:"contained", onClick: ()=>sendMessage(toListener(NODE_VIEW_LISTENER_NAME), "1111")}, "Action1"),
            re(Button,{key:"action2", style:style, variant:"contained", onClick: ()=>sendMessage(toListener(NODE_VIEW_LISTENER_NAME), "2222")}, "Action2"),
        ])
    }

    componentWillUnmount() {
        console.log("componentWillUnmount = '" + 5555 + "'");
        removeMessageListener(NODE_VIEW_LISTENER_NAME)
    }
}
