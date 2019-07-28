function doTestCall(funcName, args) {

    args = eval(args)
    print("js-test-utils.doTestCall for " + funcName)
    args.forEach(function (elem, index) {
        print("arg[" + index + "] = " + JSON.stringify(elem))
    })

    const result = JSON.stringify(eval(funcName).apply(null, args));

    print("js-test-utils.doTestCall result = " + result)
    return result
}

const MvcAdapter = Java.type('org.igye.outline2.controllers.BeControllerComponentTest');

function doGet(url, onSuccess) {
    print("js-test-utils.doGet: url = " + url)
    MvcAdapter.doGet(url)
}

function doPatch(url, data, onSuccess) {
    const dataStr = JSON.stringify(data)
    print("js-test-utils.doPatch: url = " + url + " , data = " + dataStr)
    MvcAdapter.doPatch(url, dataStr)
}