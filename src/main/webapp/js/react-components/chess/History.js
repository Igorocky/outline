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