'use strict'

const CONTEXT_PATH = "/fe"

const PATH = {
    stateWebSocketUrl: "/be/websocket/state",
    node: CONTEXT_PATH + "/node",
    nodeWithId: CONTEXT_PATH + "/node/:id",
    createNodeWithIdPath: id => CONTEXT_PATH + "/node/" + id,
    chessboard: CONTEXT_PATH + "/chessboard",
    admin: CONTEXT_PATH + "/admin",
}

function useRedirect() {
    const [redirect, setRedirect] = useState(null)
    useEffect(()=>setRedirect(null), [redirect])
    return [redirect, setRedirect]
}

function redirectTo(to) {
    return to ? re(Redirect,{key: to, to: to}) : null
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

function doPatch(url, data, onSuccess) {
    $.ajax({
        type: "PATCH",
        url: url,
        data: JSON.stringify(data),
        contentType: "application/json; charset=utf-8",
        success: onSuccess
    });
}

function doGet(url, onSuccess) {
    $.ajax({
        type: "GET",
        url: url,
        success: onSuccess
    });
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

function uploadImage({file, parentId, onSuccess}) {
    uploadFile({url: "/be/uploadImage", file, params:{parentId:parentId?parentId:null}, onSuccess})
}

function uploadImportFile({file, parentId, onSuccess}) {
    uploadFile({url: "/be/importFromFile/" + parentId, file, onSuccess})
}

function exportToFile({nodeId,onSuccess}) {
    doPost({url:"/be/exportToFile/" + nodeId, data:{}, onSuccess: onSuccess})
}

function uploadFile({url, file, params, onSuccess}) {
    let fd = new FormData();
    if (file) {
        fd.append("file", file);
        if (params) {
            _.each(_.pairs(params), ([val, key]) => fd.append(val, key))
        }
        $.ajax({
            type: "POST",
            url: url,
            data: fd,
            contentType: false,
            cache: false,
            processData: false,
            success: function (data) {
                onSuccess(data);
            }
        });
    }
}

function getByPath(obj, path, defaultValue) {
    if (_.size(path) == 0) {
        return obj
    } else if(!obj) {
        return defaultValue
    } else {
        return getByPath(obj[_.first(path)], _.tail(path), defaultValue)
    }
}
