
const FolderComponent = props => {
    const [anchorEl, setAnchorEl] = useState(null);

    function performMove(moveFunction) {
        return () => {
            setAnchorEl(null)
            moveFunction()
        }
    }

    function viewModeButtons() {
        return [
            iconButton({iconName: "vertical_align_top", onClick: performMove(props.onMoveToStart)}),
            iconButton({iconName: "keyboard_arrow_up", onClick: performMove(props.onMoveUp)}),
            iconButton({iconName: "keyboard_arrow_down", onClick: performMove(props.onMoveDown)}),
            iconButton({iconName: "vertical_align_bottom", onClick: performMove(props.onMoveToEnd)}),
            iconButton({iconName: "delete", onClick: props.onDelete}),
        ]
    }

    function onClick(e) {
        setAnchorEl(e.currentTarget)
    }

    return [
        re(ListItem,{key:props.id, button: true,
                style:{backgroundColor: anchorEl?"#f2f2f2":""},
                ContainerProps:{className:"grey-background-on-hover-mark"},
                onClick: props.onClick},
            re(ListItemIcon,{key:"lii"}, re(Icon, {style: {fontSize: "24px"}}, "folder")),
            re(ListItemText,{key:"lit"},props.name),
            re(ListItemSecondaryAction,{key:"lisa"},
                re(IconButton, {edge: "start", color: "inherit", onClick: onClick},
                    re(Icon, {style: {fontSize: "24px"}}, "more_vert")
                )
            )
        ),
        anchorEl
            ? clickAwayListener({
                key: "Popper",
                onClickAway: () => setAnchorEl(null),
                children: re(Popper, {open: true, anchorEl: anchorEl, placement: 'top-end'},
                    paper(viewModeButtons())
                )
            })
            : null
    ]
}