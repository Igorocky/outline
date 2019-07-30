
const ImageNodeComponent = props => {
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
        re('img',
            {
                key: "imageNode",
                src:"/be/image/" + props.imgId,
                style: {margin:"10px"},
                onDoubleClick: () => setAnchorEl(null),
                onClick: onClick
            }
        ),
        anchorEl
            ? clickAwayListener({
                key: "Popper",
                onClickAway: () => setAnchorEl(null),
                children: re(Popper, {open: true, anchorEl: anchorEl, placement: 'top-start'},
                    paper(viewModeButtons())
                )
            })
            : null
    ]
}