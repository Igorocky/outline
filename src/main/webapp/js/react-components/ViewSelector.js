const VIEWS = [
    {name:"NodeView", component: NodeView2},
    {name:"View2", component: View2},
    {name:"View3", component: View3},
]

class ViewSelector extends React.Component {
    constructor(props) {
        super(props)
        this.state = {sideMenuIsOpen:false, selectedView: VIEWS[0], selectedViewActions: null, context: null}

        this.openSideMenu = this.openSideMenu.bind(this)
        this.closeSideMenu = this.closeSideMenu.bind(this)
        this.selectView = this.selectView.bind(this)
        this.setActions = this.setActions.bind(this)
        this.updateContext = this.updateContext.bind(this)
    }

    render() {
        return [
            this.renderAppBar(),
            this.renderDrawer(),
            this.state.selectedView
                ?re(this.state.selectedView.component,{
                    key:this.state.selectedView.name,
                    setActions: this.setActions,
                    updateContext: this.updateContext
                })
                :null
        ]
    }

    setActions(actions) {
        this.setState({selectedViewActions: actions})
    }

    updateContext(contextPatch) {
        this.setState(state => ({context: {...state.context, ...contextPatch}}))
    }

    isTabOrShift(event) {
        return event.type === 'keydown' && (event.key === 'Tab' || event.key === 'Shift')
    }

    openSideMenu(even) {
        if (this.isTabOrShift(even)) {
            return
        }
        this.setState({sideMenuIsOpen: true});
    }

    closeSideMenu(even) {
        if (this.isTabOrShift(even)) {
            return
        }
        this.setState({sideMenuIsOpen: false});
    }

    renderAppBar() {
        return re(AppBar, {key: "AppBar", position: "static"},
            re(Toolbar, {variant: "dense"},
                re(IconButton, {edge: "start", color: "inherit", onClick: this.openSideMenu},
                    re(Icon, {style: {fontSize: "24px"}}, "menu")
                ),
                this.state.selectedViewActions?this.state.selectedViewActions:null
            )
        )
    }

    renderDrawer() {
        return re(Drawer, {
                key: "drawer",
                open: this.state.sideMenuIsOpen,
                onClick: this.closeSideMenu,
                onKeyDown: this.closeSideMenu
            },
            re(List, {},
                VIEWS.map((view,idx) => re(ListItem,{button:true, key:idx, onClick:()=>this.selectView(idx)},
                    re(ListItemText,{},view.name)
                ))
            )
        )
    }

    selectView(idx) {
        this.setState(state => ({
            selectedView: VIEWS[idx],
            selectedViewActions: state.selectedView !== VIEWS[idx] ? null : state.selectedViewActions
        }))
    }
}
