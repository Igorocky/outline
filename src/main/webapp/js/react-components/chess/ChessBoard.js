const chessBoardTdStyle = {
    borderCollapse: "collapse",
    border: "1px solid black",
    padding: "0px"
}

const chessBoardTableStyle = {
    ...chessBoardTdStyle,
    marginTop:"10px",
    marginLeft:"10px",
}

const ChessBoard = ({setComponentState, cells}) => {

    return re('table', {style:{...chessBoardTableStyle}},
        re('tbody',{},
            _.range(0, 7).map(y =>
                re('tr',{key:y},
                    _.range(0, 7).map(x =>
                        re('td',{key:x, style:{...chessBoardTdStyle}},
                            re(ChessBoardCell,{setComponentState:setComponentState, ...cells[x][y]})
                        )
                    )
                )
            )
        )
    )
}