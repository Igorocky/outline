
const FolderComponent = ({text, onClick, icon}) => {
    return RE.Container.row.left.center(
        {
            onClick: onClick,
            className: "grey-background-on-hover pointer-on-hover",
            style: {padding:"5px 0px"}
        },
        {style: {marginRight:"7px"}},
        icon,
        RE.Typography({key: "folder-name", variant: "body1"}, text)
    )
}