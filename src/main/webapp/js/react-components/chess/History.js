const historyTdStyle = {
    borderCollapse: "collapse",
    border: "1px solid black",
    padding: "0px"
}

const historyTableStyle = {
    ...historyTdStyle,
}

const History = ({backend, startPositionSelected, rows}) => {

    function getStyleForCell(selected) {
        return selected
        ?{backgroundColor: "#90EE90"}
        :{}
    }

    return RE.Paper({style:{maxHeight:"450px", overflow: "scroll"}},RE.Table(
        {size:"small"},
        RE.TableBody({},
            RE.TableRow({key: "-1"}, RE.TableCell({
                    colSpan: 3,
                    style: {backgroundColor: startPositionSelected ? "yellow" : null},
                    className: "grey-background-on-hover pointer-on-hover",
                    onClick: () => backend.call("execChessCommand", {command:"s"})
                },
                "Start"
            )),
            rows.map(move => RE.TableRow({key:move.feMoveNumber},
                RE.TableCell({key:"-1"}, move.feMoveNumber),
                RE.TableCell({
                        key:"w",
                        style: {backgroundColor: move.whitesMoveSelected ? "yellow" : null},
                        className:"grey-background-on-hover pointer-on-hover",
                        onClick: () => backend.call("execChessCommand", {command:"g " + move.feMoveNumber + "w"}),
                    },
                    move.whitesMove
                ),
                RE.TableCell({
                        key:"b",
                        style: {backgroundColor: move.blacksMoveSelected ? "yellow" : null},
                        className:"grey-background-on-hover pointer-on-hover",
                        onClick: () => backend.call("execChessCommand", {command:"g " + move.feMoveNumber + "b"}),
                    },
                    move.blacksMove
                )
            ))
        )
    ))


    return re('table', {style:{...historyTableStyle}},
        re('tbody',{},
            re('tr',{key:-1},
                re('td',{colSpan:3, style:{...historyTdStyle, ...getStyleForCell(startPositionSelected)}},
                    "Start"
                )
            ),
            rows.map(move =>
                re('tr',{key:move.feMoveNumber},
                    re('td',{key:"n", style:{...historyTdStyle}},
                        move.feMoveNumber
                    ),
                    re('td',{key:"w", style:{...historyTdStyle, ...getStyleForCell(move.whitesMoveSelected)}},
                        move.whitesMove
                    ),
                    re('td',{key:"b", style:{...historyTdStyle, ...getStyleForCell(move.blacksMoveSelected)}},
                        move.blacksMove
                    ),
                )
            )
        )
    )
}