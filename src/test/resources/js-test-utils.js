function doTestCall(funcName, args) {

    args = eval(args)
    print("doTestCall for " + funcName)
    args.forEach(function (elem, index) {
        print("arg[" + index + "] = " + JSON.stringify(elem))
    })

    const result = JSON.stringify(eval(funcName).apply(null, args));

    print("doTestCall result = " + result)
    return result
}