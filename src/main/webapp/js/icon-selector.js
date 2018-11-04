let WAIT_FOR_ICON = "waitForIcon";

function iconSelector(iconSelectorContainerId, onIconDelete, attrName, initIconId) {
    $('#' + iconSelectorContainerId).html(
        $("<span/>", {
            id: "no-icon-span", text: "No icon.",
            onclick: "waitForIcon('" + iconSelectorContainerId + "')", hidden: "hidden"
        })
    ).append(
        $("<span/>", {
            id: "click-ctrl-v-btn", text: "Click Ctrl+V", hidden: "hidden"
        })
    ).append(
        $("<a/>", {
            id: "cancel-wait-for-icon-btn", text: " Cancel",
            onclick: "cancelWaitForIcon('" + iconSelectorContainerId + "')", hidden: "hidden"
        })
    ).append(
        $("<a/>", {
            id: "delete-icon-btn", text: "Delete", hidden: "hidden"
        }).click(function () {onIconDelete();})
    ).append(
        $("<img/>", {
            id: "icon-img", hidden: "hidden"
        })
    ).append(
        $("<input/>", {
            id: "icon-hidden-input", type:"hidden", name: attrName
        })
    ).prop(WAIT_FOR_ICON, false);

    if (initIconId == null) {
        unsetIcon(iconSelectorContainerId);
    } else {
        setIcon(iconSelectorContainerId, initIconId);
    }
}

function waitForIcon(iconSelectorContainerId) {
    $('#' + iconSelectorContainerId).prop(WAIT_FOR_ICON, true);
    $('#' + iconSelectorContainerId + " #no-icon-span" ).hide();
    $('#' + iconSelectorContainerId + " #click-ctrl-v-btn" ).show();
    $('#' + iconSelectorContainerId + " #cancel-wait-for-icon-btn" ).show();
}

function cancelWaitForIcon(iconSelectorContainerId) {
    $('#' + iconSelectorContainerId).prop(WAIT_FOR_ICON, false);
    $('#' + iconSelectorContainerId + " #no-icon-span" ).show();
    $('#' + iconSelectorContainerId + " #click-ctrl-v-btn" ).hide();
    $('#' + iconSelectorContainerId + " #cancel-wait-for-icon-btn" ).hide();
}

function isWaitingForIcon(iconSelectorContainerId) {
    return $('#' + iconSelectorContainerId).prop(WAIT_FOR_ICON);
}

function setIcon(iconSelectorContainerId, iconId) {
    $('#' + iconSelectorContainerId).prop(WAIT_FOR_ICON, false);
    $('#' + iconSelectorContainerId + " #no-icon-span" ).hide();
    $('#' + iconSelectorContainerId + " #click-ctrl-v-btn" ).hide();
    $('#' + iconSelectorContainerId + " #cancel-wait-for-icon-btn" ).hide();
    $('#' + iconSelectorContainerId + " #delete-icon-btn" ).show();
    $('#' + iconSelectorContainerId + " #icon-img" ).attr("src", "icon/" + iconId).show();
    $('#' + iconSelectorContainerId + " #icon-hidden-input" ).attr("value", iconId);
}

function unsetIcon(iconSelectorContainerId) {
    $('#' + iconSelectorContainerId).prop(WAIT_FOR_ICON, false);
    $('#' + iconSelectorContainerId + " #no-icon-span" ).show();
    $('#' + iconSelectorContainerId + " #click-ctrl-v-btn" ).hide();
    $('#' + iconSelectorContainerId + " #cancelWaitForIcon" ).hide();
    $('#' + iconSelectorContainerId + " #delete-icon-btn" ).hide();
    $('#' + iconSelectorContainerId + " #icon-img" ).attr("src", "").hide();
    $('#' + iconSelectorContainerId + " #icon-hidden-input" ).attr("value", null);
}

function getIconId(iconSelectorContainerId) {
    return $('#' + iconSelectorContainerId + " #icon-hidden-input" ).attr("value");
}