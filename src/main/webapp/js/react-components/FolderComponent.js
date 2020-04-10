
const FolderComponent = ({keyVal, text, props, textProps, icon, userIcon, popupActions}) => {
    const [anchorEl, setAnchorEl] = useState(null);

    function stopPropagation(e) {
        e.stopPropagation()
    }

    function openPopup(e) {
        setAnchorEl(e.currentTarget)
        stopPropagation(e)
    }
    return RE.Container.row.left.center(
        {
            key:keyVal,
            className:"grey-background-on-hover pointer-on-hover",
            classes: {root: "FolderComponent-root"},
            style:{
                backgroundColor: anchorEl?"rgb(215, 215, 215)":"",
                width:"100%"
            },
            ...props
        },
        {style: {marginRight:"7px"}},
        icon,
        popupActions?RE.Icon({
                style: {
                    fontSize: "24px",
                    borderRadius: "10px"
                },
                className:"more-options-btn",
                onClick: openPopup,
                onMouseUp: stopPropagation,
            },
            "more_vert"
        ):null,
        userIcon,
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