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

const MvcAdapter = Java.type('org.igye.outline2.controllers.ControllerComponentTestBase');

function onSuccessCallback(response) {
    MvcAdapter.onSuccess(JSON.stringify(response))
}

function doGet(url, onSuccess) {
    print("js-test-utils.doGet: url = " + url)
    const result = MvcAdapter.doGet(url)
    onSuccess?onSuccess(result):null
}

function doPatch(url, data, onSuccess) {
    const dataStr = JSON.stringify(data)
    print("js-test-utils.doPatch: url = " + url + " , body = " + dataStr)
    const result = Java.type('org.igye.outline2.controllers.ControllerComponentTestBase').doPatch(url, dataStr)
    print("js-test-utils.doPatch: result = " + result)
    print("js-test-utils.doPatch: onSuccess = " + onSuccess)
    print("js-test-utils.doPatch: typeof result = " + (typeof result))
    onSuccess?onSuccess(eval(result)):null
}