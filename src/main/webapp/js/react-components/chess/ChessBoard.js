const ChessBoard = ({backend, boardRotated, cells}) => {

    return RE.svg({width:cellSize*8, height:cellSize*8},
        _.range(7, -1, -1).map(y =>
            _.range(0, 8).map(x =>
                re(ChessBoardCell,{key:x+":"+y,backend:backend,boardRotated:boardRotated, ...cells[x][y]})
            )
        )
    )
}