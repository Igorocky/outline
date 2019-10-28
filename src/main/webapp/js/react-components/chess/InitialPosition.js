const InitialPosition = ({backend, availableChessmanTypes}) => {

    const width = _.size(availableChessmanTypes);
    const height = _.size(availableChessmanTypes[0]);
    return RE.svg({
            width: width*cellSize,
            height: height*cellSize
        },
        _.range(height-1, -1, -1).map(y =>
            _.range(0, width).map(x =>
                re(ChessBoardCell,{key:x+":"+y,backend:backend, ...availableChessmanTypes[x][y], xShift:-10, yShift:6})
            )
        )
    )
}