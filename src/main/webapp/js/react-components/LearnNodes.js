const LearnNodes = props => re(NodesContainer,{...props.pageData})

const TRANSITIONS_TOTAL_CNT = "transitionsTotalCnt"
const NUMBER_OF_CONNECTED_TRANSITIONS = "numberOfConnectedTransitions"
const NUMBER_OF_CYCLES = "numberOfCycles"

class NodesContainer extends React.Component {
    constructor(props) {
        super(props)
        this.state = {cycleNum:0}
        this.reloadNodes = this.reloadNodes.bind(this)
    }

    render() {
        return re('div',{},
            re(TitleComponent, {path:this.state.path, key:this.state.cycleNum + "-title"}),
            re('div',{},
                re(Button,{variant:"contained", color:"primary", onClick:this.reloadNodes}, "Reload"),
                re(Paper, {},
                    this.state[NUMBER_OF_CYCLES] +
                    "/" + this.state[NUMBER_OF_CONNECTED_TRANSITIONS] +
                    "/" + this.state[TRANSITIONS_TOTAL_CNT]
                )
            ),
            this.renderNodes()
        )
    }

    renderNodes() {
        if (this.state.nodesToLearn) {
            return re('ul',{className:"NodesContainer-list-of-nodes"}, [
                re('li', {}, re(NodeComponent, this.getPropsForNodeComponent(0))),
                re('li', {}, re(NodeComponent, this.getPropsForNodeComponent(1))),
                re('li', {}, re(NodeComponent, this.getPropsForNodeComponent(2))),
                re('li', {}, re(NodeComponent, this.getPropsForNodeComponent(3))),
                re('li', {}, re(NodeComponent, this.getPropsForNodeComponent(4))),
            ])
        } else {
            return "..."
        }
    }

    getPropsForNodeComponent(idx) {
        return {opened:idx === 2, key:this.state.cycleNum + "-" + idx, ...this.state.nodesToLearn[idx]}
    }

    reloadNodes() {
        doGet({
            url: "nodesToLearn?id=" + this.props.rootId,
            success: nodesToLearnDto => this.setState((state,props)=>({cycleNum: state.cycleNum+1, ...nodesToLearnDto}))
        })
    }

    componentDidMount() {
        this.reloadNodes()
    }
}

class NodeComponent extends React.Component {
    constructor(props) {
        super(props)
        this.state = {
            opened: props.opened
        }
        this.openImage = this.openImage.bind(this)
    }

    render() {
        if (this.state.opened) {
            if (!this.props.id) {
                return re('span',{},this.props.title)
            } else {
                return re('a', {href:this.props.url},
                    this.props.iconId
                        ? re('img',{src: "icon/" + this.props.iconId})
                        : this.props.title
                )
            }
        } else {
            return re(Button, {color:"primary", onClick:this.openImage}, "Open")
        }
    }

    openImage() {
        this.setState((state,props)=>({opened: true}))
    }
}

class TitleComponent extends React.Component {
    constructor(props) {
        super(props)
        this.state = {opened: false}
        this.openTitle = this.openTitle.bind(this)
    }

    render() {
        if (this.state.opened) {
            return re('div', {}, _.map(this.props.path, pathNode=>
                re('a', {href:pathNode.url, key:pathNode.id},
                    re(Typography,{variant:"subheading"}, pathNode.title)
                )
            ))
        } else {
            return re(Button, {color:"primary", onClick:this.openTitle}, "Show")
        }
    }

    openTitle() {
        this.setState((state,props)=>({opened: true}))
    }
}

