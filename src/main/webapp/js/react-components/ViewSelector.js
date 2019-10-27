const VIEWS = [
    {name:"NodeView", component: NodeView, path: PATH.node},
    {name:"Chessboard", component: ChessComponent, path: PATH.chessboard},
    {name:"Admin", component: AdminView, path: PATH.admin},
]

const ViewSelector = () => {
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

    function openSideMenu(event) {
        if (isTabOrShift(event)) {
            return
        }
        setSideMenuIsOpen(true)
    }

    function closeSideMenu(event) {
        if (isTabOrShift(event)) {
            return
        }
        setSideMenuIsOpen(false)
    }

    function renderAppBar() {
        return RE.AppBar({position: "static"},
            RE.Toolbar({variant: "dense", ref:actionsContainerRef},
                RE.IconButton({edge: "start", color: "inherit", onClick: openSideMenu},
                    RE.Icon({style: {fontSize: "24px"}}, "menu")
                )
            )
        )
    }

    function renderDrawer() {
        return RE.Drawer({
                open: sideMenuIsOpen,
                onClick: closeSideMenu,
                onKeyDown: closeSideMenu
            },
            RE.List({},
                VIEWS.map((view,idx) => RE.ListItem({button:true, key:idx, onClick:()=>setRedirect(VIEWS[idx].path)},
                    RE.ListItemText({},view.name)
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
        re(Switch, {},
            re(Route, {
                key: PATH.nodeWithId, path: PATH.nodeWithId, exact: true,
                render: props => re(NodeView, {
                    ...props, nodeIdToLoad:props.match.params.id, actionsContainerRef: actionsContainerRef
                })
            }),
            ...getViewRoutes()
        ),
        redirectTo(redirect)
    )
}