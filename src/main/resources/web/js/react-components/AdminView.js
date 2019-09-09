const buttonsStyle = {margin: "10px"}

const AdminView = () => {
    return Container.row.left.top({children: [
            re(Button, {
                    key: "backup-btn", style: buttonsStyle, variant: "contained",
                    // onClick: () => setCheckedNodes(null)
                }, "Backup"
            )
    ]})
}