
const FolderComponent = props => {
    const [anchorEl, setAnchorEl] = useState(null);
    const [iconIsHovered, setIconIsHovered] = useState(false);

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
        e.stopPropagation()
    }

    return [
        re(Grid,{key:"folder-grid", container:true, direction:"row", justify:"flex-start", alignItems:"center",
                className:"grey-background-on-hover pointer-on-hover",
                style:{backgroundColor: anchorEl?"rgb(215, 215, 215)":""},
                onClick: props.onClick
            },
            re(IconButton, {key:"folder-button", edge: "start", color: "inherit", onClick: onClick,
                    style:{marginLeft: "15px"},
                    onMouseEnter: () => setIconIsHovered(true), onMouseLeave: () => setIconIsHovered(false)},
                iconIsHovered
                    ?re(Icon, {style: {fontSize: "24px"}}, "more_vert")
                    :re(Icon, {style: {fontSize: "24px"}}, "folder")
            ),
            re(Typography, {key:"folder-name", variant:"body1"},
                props.name
            )
        ),
        anchorEl
            ? clickAwayListener({
                key: "folder-Popper",
                onClickAway: () => setAnchorEl(null),
                children: re(Popper, {open: true, anchorEl: anchorEl, placement: 'top-start'},
                    paper(viewModeButtons())
                )
            })
            : null
    ]
}