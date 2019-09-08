const availablePiecesTdStyle = {
    borderCollapse: "collapse",
    border: "1px solid black",
    padding: "0px"
}

const availablePiecesListTableStyle = {
    ...availablePiecesTdStyle,
}

const InitialPosition = ({backend, availableChessmanTypes}) => {

    return re('table', {style:{...availablePiecesListTableStyle}},
        re('tbody',{},
            _.range(_.size(availableChessmanTypes[0])-1, -1, -1).map(y =>
                re('tr',{key:y},
                    _.range(0, _.size(availableChessmanTypes)).map(x =>
                        re('td',{key:x, style:{...availablePiecesTdStyle}},
                            re(ChessBoardCell,{backend:backend, ...availableChessmanTypes[x][y]})
                        )
                    )
                )
            )
        )
    )
}