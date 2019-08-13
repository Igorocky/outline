'use strict'

const re = React.createElement
const useState = React.useState
const useEffect = React.useEffect
const Fragment = React.Fragment

const {
    Button,
    ButtonGroup,
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
    return props => re(Grid, {container:true, direction:direction,
            justify:justify, alignItems:alignItems, style:props.style},
        React.Children.map(props.children, child => {
            return re(Grid, {item:true, style:props.childStyle}, child)
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

    }
}

