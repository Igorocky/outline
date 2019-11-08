
const ContainerFullViewActions = ({defaultAction, actions}) => {
    const [anchorEl, setAnchorEl] = useState(null);

    function processAction(action) {
        setAnchorEl(null)
        action.onClick()
    }

    function renderMenu() {
        return paper(RE.MenuList({},
            actions.map(action=>
                    RE.MenuItem({key:action.text, onClick:() => processAction(action)},
                    action.text
                )
            )
        ))
    }

    function openDropdown(e) {
        setAnchorEl(e.currentTarget)
        e.stopPropagation()
    }

    function onKeyDown(event) {
        if (event.keyCode == 27) {
            setAnchorEl(null)
        }
    }

    return RE.Fragment({},
        RE.ButtonGroup({variant:"contained"},
            RE.Button({variant:"contained", onClick: ()=>processAction(defaultAction)}, defaultAction.text),
            RE.Button({variant:"open-dropdown-btn", size:"small", onClick: openDropdown, onKeyDown: onKeyDown},
                RE.Icon({style: {fontSize: "24px"}}, "arrow_drop_down")
            )
        ),
        anchorEl
            ? clickAwayListener({
                onClickAway: () => setAnchorEl(null),
                children: re(Popper, {open: true, anchorEl: anchorEl, placement: 'bottom-end'},
                    renderMenu()
                )
            })
            : null
    )
}