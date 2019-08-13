const availablePiecesTdStyle = {
    borderCollapse: "collapse",
    border: "1px solid black",
    padding: "0px"
}

const availablePiecesListTableStyle = {
    ...availablePiecesTdStyle,
}

const AvailablePiecesList = ({setRootComponentState, availablePieces}) => {

    return re('table', {style:{...availablePiecesListTableStyle}},
        re('tbody',{},
            _.range(_.size(availablePieces[0])-1, -1, -1).map(y =>
                re('tr',{key:y},
                    _.range(0, _.size(availablePieces)).map(x =>
                        re('td',{key:x, style:{...availablePiecesTdStyle}},
                            re(ChessBoardCell,{setRootComponentState:setRootComponentState, ...availablePieces[x][y]})
                        )
                    )
                )
            )
        )
    )
}