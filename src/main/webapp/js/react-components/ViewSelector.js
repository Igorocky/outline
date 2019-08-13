const VIEWS = [
    {name:"NodeView", component: NodeView, path: PATH.node},
    {name:"Chessboard", component: ChessComponent, path: PATH.chessboard},
    {name:"View3", component: View3, path: PATH.view2},
]

const ViewSelector = props => {
    const [sideMenuIsOpen, setSideMenuIsOpen] = useState(false)
    const [redirect, setRedirect] = useRedirect()
    const actionsContainerRef = React.useRef(null)

    if (!redirect) {
        setRedirect(window.location.pathname)
        return redirectTo(window.location.pathname)
    }

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
                VIEWS.map((view,idx) => re(ListItem,{button:true, key:idx, onClick:()=>setRedirect(VIEWS[idx].path)},
                    re(ListItemText,{},view.name)
                ))
            )
        )
    }

    function getViewRoutes() {
        return VIEWS.map(view => re(Route, {
            key: view.path, path: view.path,
            render: props => re(view.component, {...props, actionsContainerRef: actionsContainerRef})
        }))
    }

    return re(BrowserRouter, {},
        renderAppBar(),
        renderDrawer(),
        re(Switch, {key:"Switch"}, [
            re(Route, {
                key: PATH.nodeWithId, path: PATH.nodeWithId, exact: true,
                render: props => re(NodeView, {nodeIdToLoad:props.match.params.id, actionsContainerRef: actionsContainerRef})
            }),
            ...getViewRoutes()
        ]),
        redirectTo(redirect)
    )
}