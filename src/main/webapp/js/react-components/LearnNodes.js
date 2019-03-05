const LearnNodes = props => re(NodesContainer,{...props.pageData})

class NodesContainer extends React.Component {
    constructor(props) {
        super(props)
        this.state = {nodes: null, cycleNum:0}
        this.reloadNodes = this.reloadNodes.bind(this)
    }

    render() {
        return re('div',{},
            re(TitleComponent, {rootId:this.props.rootId, rootNodeName:this.props.rootNodeName, key:this.state.cycleNum + "-title"}),
            re('div',{}, re(Button,{variant:"contained", color:"primary", onClick:this.reloadNodes},"Reload")),
            this.renderNodes()
        )
    }

    renderNodes() {
        if (this.state.nodes) {
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
        return {opened:idx === 2, key:this.state.cycleNum + "-" + idx, ...this.state.nodes[idx]}
    }

    reloadNodes() {
        doGet({
            url: "nodesToLearn?id=" + this.props.rootId,
            success: nodes => this.setState((state,props)=>({nodes: nodes, cycleNum: state.cycleNum+1}))
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
            return re('a', {href:"paragraph?id=" + this.props.rootId + "&showContent=true#main-title"},
                re(Typography,{variant:"subheading"}, this.props.rootNodeName)
            )
        } else {
            return re(Button, {color:"primary", onClick:this.openTitle}, "Show")
        }
    }

    openTitle() {
        this.setState((state,props)=>({opened: true}))
    }
}

