
const FolderComponent = ({text, props, textProps, icon, popupActions}) => {
    const [anchorEl, setAnchorEl] = useState(null);
    const [iconIsHovered, setIconIsHovered] = useState(false);

    function stopPropagation(e) {
        e.stopPropagation()
    }

    function openPopup(e) {
        setAnchorEl(e.currentTarget)
        stopPropagation(e)
    }

    return RE.Container.row.left.center(
        {
            className:"grey-background-on-hover pointer-on-hover",
            style:{backgroundColor: anchorEl?"rgb(215, 215, 215)":"", padding:"5px 0px", height:"60px"},
            ...props
        },
        {style: {marginRight:"7px"}},
        popupActions
            ?RE.IconButton({edge: "start", color: "inherit", onClick: openPopup, onMouseUp: stopPropagation,
                style:{marginLeft: "15px"},
                onMouseEnter: () => setIconIsHovered(true), onMouseLeave: () => setIconIsHovered(false)},
            iconIsHovered
                ?RE.Icon({style: {fontSize: "24px"}}, "more_vert")
                :icon
        ):icon,
        RE.Typography({key: "folder-name", variant: "body1", ...textProps}, text),
        anchorEl
            ? clickAwayListener({
                onClickAway: () => setAnchorEl(null),
                children: re(Popper, {open: true, anchorEl: anchorEl, placement: 'top-start'},
                    RE.Paper({onMouseUp: stopPropagation}, popupActions)
                )
            })
            : null
    )
}