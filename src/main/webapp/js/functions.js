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