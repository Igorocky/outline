"use strict";

const ContainerFullViewActions = ({defaultAction, actions}) => {
    const [anchorEl, setAnchorEl] = useState(null);

    function processAction(action) {
        setAnchorEl(null)
        action.onClick()
    }

    function getActionIcon(action) {
        if (action.iconName) {
            return RE.Icon({/*style:{fontSize: "30px"}*/}, action.iconName)
        } else if (action.icon) {
            return action.icon
        } else {
            return null
        }
    }

    function renderMenu() {
        return paper(RE.MenuList({},
            actions.map(action=>
                RE.MenuItem({key:action.text, onClick:() => processAction(action)},
                    getActionIcon(action),
                    RE.span({style: {marginLeft: "5px"}}, action.text)
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
            RE.Button({variant:"contained", onClick: ()=>processAction(defaultAction)},
                getActionIcon(defaultAction),
                RE.span({style: {marginLeft: "5px"}}, defaultAction.text)
            ),
            RE.Button({variant:"contained", size:"small", onClick: openDropdown, onKeyDown: onKeyDown},
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