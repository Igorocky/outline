function BooleanPassFail({cellData, componentConfig}) {
    if (cellData == "true") {
        return RE.span({style:{color:"green", fontWeight: "bold", ...componentConfig.style}}, "\u2713")
    } else {
        return RE.span({style:{color:"red", fontWeight: "bold", ...componentConfig.style}}, "\u2717")
    }
}