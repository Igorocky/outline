function iconsTable(iconTableContainerId, iconsDataJson) {
    $('#' + iconTableContainerId).html(
        $("<table/>", {"class":"table table-bordered"}).html(
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
    return (iconInfo.objectType == "TOPIC"
            ? $("<a/>", {href:"topic?id=" + iconInfo.nodeId + "&showContent=true"})
            : $("<a/>", {href:"paragraph?id=" + iconInfo.nodeId + "&showContent=true"})).append(
        $("<img/>", {
            src: "icon/" + iconInfo.iconId
        })
    )
}

function createCell(iconInfo) {
    return $("<td/>").html(
        iconInfo.objectType == null
            ? $("<span/>")
            : (iconInfo.iconId == null
                ? $("<span/>", {text: "?"})
                : createLink(iconInfo)
            )
    )
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

