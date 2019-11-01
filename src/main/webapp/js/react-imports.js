'use strict'

const re = React.createElement
const useState = React.useState
const useEffect = React.useEffect
const Fragment = React.Fragment

const {
    Tabs,
    Tab,
    Paper,
    colors,
    createMuiTheme,
    CssBaseline,
    Dialog,
    DialogActions,
    DialogContent,
    DialogContentText,
    DialogTitle,
    MuiThemeProvider,
    ListItemSecondaryAction,
    Grid,
    Popover,
    Popper,
    ClickAwayListener,
    CircularProgress,
    Portal,
    withStyles,
} = window['MaterialUI']

const {
    BrowserRouter,
    Redirect,
    Route,
    Switch
} = window["ReactRouterDOM"]

function reFactory(elemType) {
    return (props, ...children) => re(elemType, props, ...children)
}

const MaterialUI = window['MaterialUI']
const MuiColors = MaterialUI.colors

const DIRECTION = {row: "row", column: "column",}
const JUSTIFY = {flexStart: "flex-start", center: "center", flexEnd: "flex-end", spaceBetween: "space-between", spaceAround: "space-around",}
const ALIGN_ITEMS = {flexStart: "flex-start", center: "center", flexEnd: "flex-end", stretch: "stretch", spaceAround: "baseline",}

function gridFactory(direction, justify, alignItems) {
    return (props, childProps, ...children) => re(MaterialUI.Grid, {container:true, direction:direction,
            justify:justify, alignItems:alignItems, ...props},
        React.Children.map(children, child => {
            return re(MaterialUI.Grid, {item:true, ...childProps}, child)
        })
    )
}

const RE = {
    div: reFactory('div'),
    svg: reFactory('svg'),
    img: reFactory('img'),
    span: reFactory('span'),
    AppBar: reFactory(MaterialUI.AppBar),
    Button: reFactory(MaterialUI.Button),
    Breadcrumbs: reFactory(MaterialUI.Breadcrumbs),
    ButtonGroup: reFactory(MaterialUI.ButtonGroup),
    CircularProgress: reFactory(MaterialUI.CircularProgress),
    Checkbox: reFactory(MaterialUI.Checkbox),
    Drawer: reFactory(MaterialUI.Drawer),
    Fragment: reFactory(React.Fragment),
    Grid: reFactory(MaterialUI.Grid),
    Paper: reFactory(MaterialUI.Paper),
    Switch: reFactory(MaterialUI.Switch),
    Typography: reFactory(MaterialUI.Typography),
    Toolbar: reFactory(MaterialUI.Toolbar),
    TextField: reFactory(MaterialUI.TextField),
    Table: reFactory(MaterialUI.Table),
    TableHead: reFactory(MaterialUI.TableHead),
    TableBody: reFactory(MaterialUI.TableBody),
    TableRow: reFactory(MaterialUI.TableRow),
    TableCell: reFactory(MaterialUI.TableCell),
    IconButton: reFactory(MaterialUI.IconButton),
    Icon: reFactory(MaterialUI.Icon),
    InputBase: reFactory(MaterialUI.InputBase),
    If: (condition, ...elems) => condition?re(Fragment,{},...elems):re(Fragment,{}),
    IfNot: (condition, ...elems) => !condition?re(Fragment,{},...elems):re(Fragment,{}),
    IfTrue: (condition, ...elems) => re(Fragment,{},...elems),
    List: reFactory(MaterialUI.List),
    ListItem: reFactory(MaterialUI.ListItem),
    ListItemText: reFactory(MaterialUI.ListItemText),
    ListItemIcon: reFactory(MaterialUI.ListItemIcon),
    Link: reFactory(MaterialUI.Link),
    LinearProgress: reFactory(MaterialUI.LinearProgress),
    MenuList: reFactory(MaterialUI.MenuList),
    MenuItem: reFactory(MaterialUI.MenuItem),
    Container: {
        row: {
            left: {
                top: gridFactory(DIRECTION.row, JUSTIFY.flexStart, ALIGN_ITEMS.flexStart),
                center: gridFactory(DIRECTION.row, JUSTIFY.flexStart, ALIGN_ITEMS.center),
            },
            right: {
                top: gridFactory(DIRECTION.row, JUSTIFY.flexEnd, ALIGN_ITEMS.flexStart),
                center: gridFactory(DIRECTION.row, JUSTIFY.flexEnd, ALIGN_ITEMS.center),
            },
        },
        col: {
            top: {
                left: gridFactory(DIRECTION.column, JUSTIFY.flexStart, ALIGN_ITEMS.flexStart),
                right: gridFactory(DIRECTION.column, JUSTIFY.flexStart, ALIGN_ITEMS.flexEnd),
            },
        },
    },
}

const SVG = {
    rect: reFactory('rect'),
    image: reFactory('image'),
}

function paper(children) {
    return re(Paper,{},children)
}

function iconButton({onClick, iconName}) {
    return RE.IconButton({key: iconName, color: "inherit", onClick: onClick},
        RE.Icon({style: {fontSize: "24px"}}, iconName)
    )
}

function clickAwayListener({onClickAway, children, key}) {
    return re(ClickAwayListener, {key:key, onClickAway: onClickAway}, children)
}

function useBackend({stateType, onBackendStateCreated, onMessageFromBackend}) {
    const [stateId, setStateId] = useState(null)
    const [webSocket, setWebSocket] = useState(null)

    function callBackendStateMethod(methodName, params) {
        if (!webSocket || webSocket.readyState != 1) {
            const newWebSocket = new WebSocket("ws://" + location.host + PATH.stateWebSocketUrl)
            newWebSocket.onmessage = event => onMessageFromBackend(JSON.parse(event.data))
            newWebSocket.onopen = () => {
                newWebSocket.send(stateId)
                callBackendStateMethodInner(newWebSocket, methodName, params)
                setWebSocket(newWebSocket)
            }
        } else {
            callBackendStateMethodInner(webSocket, methodName, params)
        }
    }

    function callBackendStateMethodInner(webSocket, methodName, params) {
        webSocket.send(JSON.stringify({methodName:methodName, params:params}))
    }

    const backend = {call: callBackendStateMethod}

    useEffect(() => {
        if (!stateId) {
            doRpcCall("createNewBackendState", {stateType:stateType}, newStateId => {
                setStateId(newStateId)
            })
        } else {
            if (onBackendStateCreated) {
                onBackendStateCreated(backend)
            }
        }
        return () => {
            if (stateId) {
                doRpcCall("removeBackendState", {stateId:stateId})
            }
        }
    }, [stateId])

    return backend
}

function useConfirmActionDialog() {
    const [confirmActionDialogData, setConfirmActionDialogData] = useState(null)

    function renderConfirmActionDialog() {
        if (confirmActionDialogData) {
            return re(ConfirmActionDialog, confirmActionDialogData)
        } else {
            return null;
        }
    }

    function openConfirmActionDialog(dialogParams) {
        setConfirmActionDialogData(dialogParams)
    }

    function closeConfirmActionDialog() {
        setConfirmActionDialogData(null)
    }

    return [openConfirmActionDialog, closeConfirmActionDialog, renderConfirmActionDialog]
}