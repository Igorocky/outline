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

function submitSelection(actionType, selectedParagraphIds, selectedTopicIds, onSuccess) {
    let selection = {
        actionType: actionType,
        selections: _.map(selectedParagraphIds, idToSelectionPart('PARAGRAPH')).concat(
            _.map(selectedTopicIds, idToSelectionPart('TOPIC'))
        )
    };
    doPost({
        url: "/select",
        data: selection,
        success: onSuccess
    });
}

function idToSelectionPart(objectType) {
    return function (id) {
        return {objectType: objectType, selectedId: id}
    }
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
        url: "/uploadImage",
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