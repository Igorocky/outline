function iconsTable(iconTableContainerId, iconsDataJson) {
    $('#' + iconTableContainerId).html(
        $("<table/>", {"class":"icons-table"}).html(
            _.reduce(
                iconsDataJson,
                function(memo, iconInfoList){
                    return memo.append(createRow(iconInfoList));
                },
                $("<tbody/>")
            )
        )
    )
}

function createLink(iconInfo) {
    return $("<a/>", {href: iconInfo.cellType.toLowerCase() + "?id=" + iconInfo.nodeId + "&showContent=true#main-title"}).html(
        iconInfo.iconId != null ? $("<img/>", {src: "icon/" + iconInfo.iconId}) : $("<span/>", {text: "?"})
    )
}

function createCell(iconInfo) {
    var $content;
    switch (iconInfo.cellType) {
        case "EMPTY":
            $content = $("<span/>");
            break;
        case "NUMBER":
            $content = $("<span/>", {text: iconInfo.number});
            break;
        default:
            $content = createLink(iconInfo);
    }
    return $("<td/>").html($content);
}

function createRow(iconInfoList) {
    return _.reduce(
        iconInfoList,
        function(memo, iconInfo){
            return memo.append(createCell(iconInfo));
        },
        $("<tr/>")
    );
}

