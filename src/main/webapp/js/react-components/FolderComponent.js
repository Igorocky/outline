
const FolderComponent = ({text, props, icon, popupActions}) => {
    const [anchorEl, setAnchorEl] = useState(null);
    const [iconIsHovered, setIconIsHovered] = useState(false);

    function openPopup(e) {
        setAnchorEl(e.currentTarget)
        e.stopPropagation()
    }

    return RE.Container.row.left.center(
        {
            className:"grey-background-on-hover pointer-on-hover",
            style:{backgroundColor: anchorEl?"rgb(215, 215, 215)":"", padding:"5px 0px", height:"60px"},
            ...props
        },
        {style: {marginRight:"7px"}},
        popupActions
            ?RE.IconButton({edge: "start", color: "inherit", onClick: openPopup,
                style:{marginLeft: "15px"},
                onMouseEnter: () => setIconIsHovered(true), onMouseLeave: () => setIconIsHovered(false)},
            iconIsHovered
                ?RE.Icon({style: {fontSize: "24px"}}, "more_vert")
                :icon
        ):icon,
        RE.Typography({key: "folder-name", variant: "body1"}, text),
        anchorEl
            ? clickAwayListener({
                onClickAway: () => setAnchorEl(null),
                children: re(Popper, {open: true, anchorEl: anchorEl, placement: 'top-start'},
                    paper(popupActions)
                )
            })
            : null
    )
}