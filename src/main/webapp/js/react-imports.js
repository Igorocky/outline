'use strict'

const re = React.createElement
const useState = React.useState
const useEffect = React.useEffect
const Fragment = React.Fragment

const {
    Button,
    ButtonGroup,
    Tabs,
    Tab,
    MenuList,
    MenuItem,
    Paper,
    colors,
    createMuiTheme,
    CssBaseline,
    Dialog,
    DialogActions,
    DialogContent,
    DialogContentText,
    DialogTitle,
    Icon,
    MuiThemeProvider,
    Typography,
    Breadcrumbs,
    Link,
    AppBar,
    Toolbar,
    IconButton,
    Drawer,
    List,
    ListItem,
    ListItemIcon,
    ListItemText,
    ListItemSecondaryAction,
    TextField,
    InputBase,
    Grid,
    Popover,
    Popper,
    ClickAwayListener,
    LinearProgress,
    CircularProgress,
    Portal,
    Checkbox,
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
    Button: reFactory(MaterialUI.Button),
    CircularProgress: reFactory(MaterialUI.CircularProgress),
    Grid: reFactory(MaterialUI.Grid),
    Paper: reFactory(MaterialUI.Paper),
    If: (condition, ...elems) => condition?re(Fragment,{},...elems):re(Fragment,{}),
    IfTrue: (condition, ...elems) => re(Fragment,{},...elems),
    Container: {
        row: {
            left: {
                top: gridFactory(DIRECTION.row, JUSTIFY.flexStart, ALIGN_ITEMS.flexStart)
            }
        },
        col: {
            top: {
                left: gridFactory(DIRECTION.column, JUSTIFY.flexStart, ALIGN_ITEMS.flexStart)
            }
        }
    },
}

function paper(children) {
    return re(Paper,{},children)
}

function iconButton({onClick, iconName}) {
    return re(IconButton, {key: iconName, color: "inherit", onClick: onClick},
        re(Icon, {style: {fontSize: "24px"}}, iconName)
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