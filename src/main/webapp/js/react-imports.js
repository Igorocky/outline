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

const DIRECTION = {
    row: "row",
    column: "column",
}

const JUSTIFY = {
    flexStart: "flex-start",
    center: "center",
    flexEnd: "flex-end",
    spaceBetween: "space-between",
    spaceAround: "space-around",
}

const ALIGN_ITEMS = {
    flexStart: "flex-start",
    center: "center",
    flexEnd: "flex-end",
    stretch: "stretch",
    spaceAround: "baseline",
}

function containerFactory(direction, justify, alignItems) {
    return ({style, children, childStyle}) => re(Grid, {container:true, direction:direction,
            justify:justify, alignItems:alignItems, style:style},
        React.Children.map(children, child => {
            return re(Grid, {item:true, style:childStyle}, child)
        })
    )
}

const Container = {
    row: {
        left: {
            top: containerFactory(DIRECTION.row, JUSTIFY.flexStart, ALIGN_ITEMS.flexStart)
        }
    },
    col: {
        top: {
            left: containerFactory(DIRECTION.column, JUSTIFY.flexStart, ALIGN_ITEMS.flexStart)
        }
    }
}

function useBackend({stateType, onRegistered}) {
    const [stateId, setStateId] = useState(null)

    function createBackend(stateId){
        return {
            call: function (methodName, params, onSuccess) {
                doRpcCall(
                    "invokeMethodOnBackendState",
                    {stateId:stateId, methodName:methodName, params:params},
                    onSuccess
                )
            }
        }
    }

    useEffect(() => {
        if (!stateId) {
            doRpcCall("registerNewBackendState", {stateType:stateType}, newStateId => {
                setStateId(newStateId)
                if (onRegistered) {
                    onRegistered(createBackend(newStateId))
                }
            })
        }
        return () => {
            if (stateId) {
                doRpcCall("removeBackendState", {stateId:stateId})
            }
        }
    }, [stateId])

    return createBackend(stateId)
}