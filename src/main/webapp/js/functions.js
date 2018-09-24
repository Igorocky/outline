function focusOnLoad(selector) {
    $(function() {
        $(selector).focus();
    });
}

function doPost(params) {
    $.ajax({
        type: "POST",
        url: params.url,
        data: JSON.stringify(params.data),
        contentType: "application/json; charset=utf-8",
        success: params.success
    });
}

function submitSelection(actionType, selectedElems, onSuccess) {
    let selection = {
        actionType: actionType,
        selections: _.map(selectedElems, selectedElemToSelectionPart())
    };
    doPost({
        url: "/v2/select",
        data: selection,
        success: onSuccess
    });
}

function selectedElemToSelectionPart() {
    return function (selectedElem) {
        return {
            objectType: $(selectedElem).attr("objecttype"),
            selectedId: $(selectedElem).attr("id")
        }
    }
}

function selectionModeOn() {
    $( ".selection-checkbox" ).show();
    $( "#selection-mode-on-btn" ).hide();
    $( "#selection-mode-off-btn" ).show();
    $( "#cut-btn" ).show();
    $( ".content-wrapper" ).css({
        "border-color": "blue",
        "border-width":"1px",
        "border-style":"solid"
    });
}

function selectionModeOff() {
    $( ".selection-checkbox" ).hide();
    $( "#selection-mode-on-btn" ).show();
    $( "#selection-mode-off-btn" ).hide();
    $( "#cut-btn" ).hide();
    $( ".content-wrapper" ).css({
        "border-style":"none"
    });
}

function doSelection(actionType) {
    let selectedElems = _.filter($(".selection-checkbox").toArray(), function (elem) {return elem.checked});
    submitSelection(
        actionType,
        selectedElems,
        function () {
            _.each(selectedElems, function (elem) {elem.checked = false});
            selectionModeOff();
        }
    )
}
function doPaste(destId) {
    doPost({
        url: "/v2/performActionOnSelectedObjects",
        data: destId == "null" ? null : destId,
        success: function () {
            window.location.reload();
        }
    });
}

function indexOf(list, predicate) {
    return _.reduce(
        list,
        function (memo, elem) {
            if (predicate(elem)) {
                return {i:memo.i+1, r: memo.i};
            } else {
                return {i:memo.i+1, r: memo.r};
            }
        },
        {i:0, r: null}
    ).r;
}

function extractFileFromEvent(event) {
    // use event.originalEvent.clipboard for newer chrome versions
    var items = (event.clipboardData  || event.originalEvent.clipboardData).items;
    // console.log(JSON.stringify(items)); // will give you the mime types
    // find pasted image among pasted items
    var blob = null;
    for (var i = 0; i < items.length; i++) {
        if (items[i].type.indexOf("image") === 0) {
            blob = items[i].getAsFile();
        }
    }
    return blob;
}

function uploadImage(file, onSuccess) {
    let fd = new FormData();
    fd.append("file", file);
    $.ajax({
        type: "POST",
        url: "/v2/uploadImage",
        data: fd,
        // contentType: "multipart/form-data",
        contentType: false,
        cache: false,
        dataType: 'json',
        processData: false,
        success: function (data) {
            onSuccess(data);
        }
    });
}

function registerShortcuts() {
  registerShortcutsOnElems("a", function (elem) {elem.click();});
  registerShortcutsOnElems("div, ul", focusFirstChild("a"));
}

function registerShortcutsOnElems(elemsSelector, action) {
    $(elemsSelector).each(function (idx, elem) {
        if (elem.hasAttribute("shortcut")) {
            let shortcut = elem.getAttribute("shortcut");
            registerShortcut(createEventSelector(shortcut), function () {action(elem);});
            $(elem).attr("title", shortcut);
        }
    })
}

function focusFirstChild(childSelector) {
    return function (elem) {
        $(elem).find(childSelector + ":first").focus();
    }
}

function registerShortcut(eventSelector, action) {
    document.addEventListener(
        "keyup",
        function onKeyUp(event) {
            if (eventSelector(event)) {
                action();
            }
        },
        false
    );
}

function createEventSelector(shortcutStr) {
    let arr = shortcutStr.split(" ");
    if (_.size(arr) == 1) {
        return function(event) {
            return event.key == shortcutStr;
        }
    } else if (_.size(arr) == 2) {
        if (_.first(arr) == "ctrl") {
            return function(event) {
                return event.ctrlKey && event.key == _.last(arr);
            }
        }
    }
}