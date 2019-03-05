function doPost(params) {
    $.ajax({
        type: "POST",
        url: params.url,
        data: JSON.stringify(params.data),
        contentType: "application/json; charset=utf-8",
        success: params.success
    });
}

function doGet(params) {
    $.ajax({
        type: "GET",
        url: params.url,
        success: params.success
    });
}

