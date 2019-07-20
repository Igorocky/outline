'use strict';

function getLastViewedNode(responseHandler) {
    doGetMocked({url: "getLastViewedNode", onSuccess: responseHandler, response:NODE})
}

function updateTextOfTextNode({id,text,onSuccess}) {
    doPostMocked({url:"updateTextOfTextNode", data:{id:id,text:text}, onSuccess: onSuccess})
}

function doPostMocked({url, data, onSuccess, response}) {
    console.log("POST " + url + "\n" + JSON.stringify(data));
    onSuccess(response)
}

function doGetMocked({url, onSuccess, response}) {
    console.log("GET " + url);
    console.log("response: " + JSON.stringify(response));
    onSuccess(response)
}

function doPost({url, data, onSuccess}) {
    $.ajax({
        type: "POST",
        url: url,
        data: JSON.stringify(data),
        contentType: "application/json; charset=utf-8",
        success: onSuccess
    });
}

function doGet({url, onSuccess}) {
    $.ajax({
        type: "GET",
        url: url,
        success: onSuccess
    });
}

