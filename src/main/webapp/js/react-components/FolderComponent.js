
const FolderComponent = ({name, onClick}) => {
    return re(Grid, {
            key: "folder-grid", container: true, direction: "row", justify: "flex-start", alignItems: "center",
            className: "grey-background-on-hover pointer-on-hover",
            onClick: onClick
        },
        iconButton({onClick: () => null, iconName: "folder"}),
        re(Typography, {key: "folder-name", variant: "body1"}, name)
    )
}