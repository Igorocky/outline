'use strict';

const re = React.createElement;
const useState = React.useState;
const useEffect = React.useEffect;

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
    AppBar,
    Toolbar,
    IconButton,
    Drawer,
    List,
    ListItem,
    ListItemIcon,
    ListItemText,
    TextField,
    Grid,
    Popover,
    Popper,
    ClickAwayListener,
    withStyles,
} = window['MaterialUI'];

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