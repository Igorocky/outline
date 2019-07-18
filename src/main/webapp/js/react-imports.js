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