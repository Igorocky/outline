const ACTION_NEW_NODE = "action-new-node";
const ACTION_NEW_SIBLING_NODE = "action-new-sibling-node";
const ACTION_SELECT = "action-select";
const ACTION_IMPORT = "action-import";
const ACTION_EXPORT = "action-export";

const ContainerFullViewActions = props => {
    const [anchorEl, setAnchorEl] = useState(null);

    const NODE_VIEW_ACTIONS = [
        {id: ACTION_NEW_NODE, onClick: props.onNewNode, text: "New Folder"},
        {id: ACTION_NEW_SIBLING_NODE, onClick: props.onNewSiblingNode, text: "New Sibling Folder"},
        {id: ACTION_SELECT, onClick: props.onSelect, text: "Select items"},
        {id: ACTION_IMPORT, onClick: props.onImport, text: "Import"},
        {id: ACTION_EXPORT, onClick: props.onExport, text: "Export"},
    ]

    function getAction(actionId) {
        return _.find(NODE_VIEW_ACTIONS, a => a.id == actionId)
    }

    function processAction(actionId) {
        setAnchorEl(null)
        getAction(actionId).onClick()
    }

    function renderMenu() {
        return paper(re(MenuList,{},
            NODE_VIEW_ACTIONS.map(action=>
                re(MenuItem,{key:action.id, onClick:() => processAction(action.id)},
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

    return [
        re(ButtonGroup, {key: "ContainerFullViewActions-ButtonGroup", variant:"contained"},
            re(Button, {key:"new-folder-btn", variant:"contained", onClick: ()=>processAction(ACTION_NEW_NODE)},
                getAction(ACTION_NEW_NODE).text
            ),
            re(Button, {variant:"open-dropdown-btn", size:"small", onClick: openDropdown, onKeyDown: onKeyDown},
                RE.Icon({style: {fontSize: "24px"}}, "arrow_drop_down")
            )
        ),
        anchorEl
            ? clickAwayListener({
                key: "ContainerFullViewActions-clickAwayListener",
                onClickAway: () => setAnchorEl(null),
                children: re(Popper, {open: true, anchorEl: anchorEl, placement: 'bottom-end'},
                    renderMenu()
                )
            })
            : null
    ]
}