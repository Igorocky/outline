const BooleanPassFail = ({cellData}) => {
    if (cellData == "true") {
        return RE.span({style:{color:"green", fontWeight: "bold"}}, "\u2713")
    } else {
        return RE.span({style:{color:"red", fontWeight: "bold"}}, "x")
    }
}