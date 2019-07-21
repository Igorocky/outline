const VIEWS = [
    {name:"NodeView", component: NodeView2},
    {name:"View2", component: View2},
    {name:"View3", component: View3},
]

const ViewSelector2 = props => {
    const [sideMenuIsOpen, setSideMenuIsOpen] = useState(false)
    const [selectedView, setSelectedView] = useState(VIEWS[0])
    const actionsContainerRef = React.useRef(null);

    function isTabOrShift(event) {
        return event.type === 'keydown' && (event.key === 'Tab' || event.key === 'Shift')
    }

    function openSideMenu(even) {
        if (isTabOrShift(even)) {
            return
        }
        setSideMenuIsOpen(true)
    }

    function closeSideMenu(even) {
        if (isTabOrShift(even)) {
            return
        }
        setSideMenuIsOpen(false)
    }

    function renderAppBar() {
        return re(AppBar, {key: "AppBar", position: "static"},
            re(Toolbar, {variant: "dense", ref:actionsContainerRef},
                re(IconButton, {edge: "start", color: "inherit", onClick: openSideMenu},
                    re(Icon, {style: {fontSize: "24px"}}, "menu")
                )
            )
        )
    }

    function renderDrawer() {
        return re(Drawer, {
                key: "drawer",
                open: sideMenuIsOpen,
                onClick: closeSideMenu,
                onKeyDown: closeSideMenu
            },
            re(List, {},
                VIEWS.map((view,idx) => re(ListItem,{button:true, key:idx, onClick:()=>setSelectedView(VIEWS[idx])},
                    re(ListItemText,{},view.name)
                ))
            )
        )
    }

    return [
        renderAppBar(),
        renderDrawer(),
        selectedView
            ?re(selectedView.component,{
                key:selectedView.name,
                actionsContainerRef: actionsContainerRef
            })
            :null
    ]
}