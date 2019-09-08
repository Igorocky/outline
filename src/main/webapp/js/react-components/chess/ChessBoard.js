const chessBoardTdStyle = {
    borderCollapse: "collapse",
    border: "1px solid black",
    padding: "0px"
}

const chessBoardTableStyle = {
    ...chessBoardTdStyle,
}

const ChessBoard = ({backend, cells}) => {

    return re('table', {style:{...chessBoardTableStyle}},
        re('tbody',{},
            _.range(7, -1, -1).map(y =>
                re('tr',{key:y},
                    _.range(0, 8).map(x =>
                        re('td',{key:x, style:{...chessBoardTdStyle}},
                            re(ChessBoardCell,{backend:backend, ...cells[x][y]})
                        )
                    )
                )
            )
        )
    )
}