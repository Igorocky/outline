'use strict'

const CONTEXT_PATH = "/fe"

const PATH = {
    node: CONTEXT_PATH + "/node",
    nodeWithId: CONTEXT_PATH + "/node/:id",
    createNodeWithIdPath: id => CONTEXT_PATH + "/node/" + id,
    view1: CONTEXT_PATH + "/view1",
    view2: CONTEXT_PATH + "/view2",
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

function uploadImage({file, onSuccess}) {
    let fd = new FormData();
    if (file) {
        fd.append("file", file);
        $.ajax({
            type: "POST",
            url: "/be/uploadImage",
            data: fd,
            contentType: false,
            cache: false,
            dataType: 'json',
            processData: false,
            success: function (data) {
                onSuccess(data);
            }
        });
    }
}
