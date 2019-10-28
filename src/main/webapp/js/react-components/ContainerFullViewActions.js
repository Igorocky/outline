const ACTION_NEW_NODE = "action-new-node";
const ACTION_NEW_SIBLING_NODE = "action-new-sibling-node";
const ACTION_NEW_CHESS_PUZZLE = "action-new-chess-puzzle";
const ACTION_SELECT = "action-select";
const ACTION_REORDER = "action-reorder";
const ACTION_IMPORT = "action-import";
const ACTION_EXPORT = "action-export";

const ContainerFullViewActions = ({onNewNode, onNewSiblingNode, onNewChessPuzzle,
                                      onSelect, onReorder,
                                      onImport, onExport,}) => {
    const [anchorEl, setAnchorEl] = useState(null);

    const NODE_VIEW_ACTIONS = [
        {id: ACTION_NEW_NODE, onClick: onNewNode, text: "New Folder"},
        {id: ACTION_NEW_SIBLING_NODE, onClick: onNewSiblingNode, text: "New Sibling Folder"},
        {id: ACTION_NEW_CHESS_PUZZLE, onClick: onNewChessPuzzle, text: "New Chess Puzzle"},
        {id: ACTION_SELECT, onClick: onSelect, text: "Select items"},
        {id: ACTION_REORDER, onClick: onReorder, text: "Reorder items"},
        {id: ACTION_IMPORT, onClick: onImport, text: "Import"},
        {id: ACTION_EXPORT, onClick: onExport, text: "Export"},
    ]

    function getAction(actionId) {
        return _.find(NODE_VIEW_ACTIONS, a => a.id == actionId)
    }

    function processAction(actionId) {
        setAnchorEl(null)
        getAction(actionId).onClick()
    }

    function renderMenu() {
        return paper(RE.MenuList({},
            NODE_VIEW_ACTIONS.map(action=>
                    RE.MenuItem({key:action.id, onClick:() => processAction(action.id)},
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
        RE.ButtonGroup({key: "ContainerFullViewActions-ButtonGroup", variant:"contained"},
            RE.Button({key:"new-folder-btn", variant:"contained", onClick: ()=>processAction(ACTION_NEW_NODE)},
                getAction(ACTION_NEW_NODE).text
            ),
            RE.Button({variant:"open-dropdown-btn", size:"small", onClick: openDropdown, onKeyDown: onKeyDown},
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
    )
}