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