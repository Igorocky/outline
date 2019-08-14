const historyTdStyle = {
    borderCollapse: "collapse",
    border: "1px solid black",
    padding: "0px"
}

const historyTableStyle = {
    ...historyTdStyle,
}

const History = ({setRootComponentState, moves}) => {

    return re('table', {style:{...historyTableStyle}},
        re('tbody',{},
            re('tr',{key:-1},
                re('td',{colSpan:3, style:{...historyTdStyle}},
                    "Start"
                )
            ),
            moves.map(move =>
                re('tr',{key:move.feMoveNumber},
                    re('td',{key:"n", style:{...historyTdStyle}},
                        move.feMoveNumber
                    ),
                    re('td',{key:"w", style:{...historyTdStyle}},
                        move.whitesMove
                    ),
                    re('td',{key:"b", style:{...historyTdStyle}},
                        move.blacksMove
                    ),
                )
            )
        )
    )
}