'use strict'

const re = React.createElement
const useState = React.useState
const useEffect = React.useEffect
const Fragment = React.Fragment

const {
    Button,
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
    Portal,
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