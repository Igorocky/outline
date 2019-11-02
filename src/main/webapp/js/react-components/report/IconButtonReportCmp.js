function IconButtonReportCmp({cellData, componentConfig, actions}) {
    const [hovered, setHovered] = useState(false);
    if (cellData == null && componentConfig.hideOnNull) {
        return null;
    } else {
        return RE.IconButton({
                size:"small", color: "inherit", onClick: () => actions[componentConfig.onClickAction](cellData),
                onMouseEnter: () => setHovered(true), onMouseLeave: () => setHovered(false),
                style: {...componentConfig.style, ...(hovered?componentConfig.hoverStyle:{})}},
            RE.Icon({style:{"fontSize": "20px"}}, componentConfig.iconName)
        )
    }
}