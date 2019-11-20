const InitialPosition = ({backend, availableChessmanTypes, colorToMove}) => {

    const width = _.size(availableChessmanTypes);
    const height = _.size(availableChessmanTypes[0]);

    function renderPiecesToSelectFrom() {
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

    function renderColorSelector() {
        return RE.RadioGroup({
                row: true,
                value: colorToMove,
                onChange: event => backend.call("setColorToMove", {colorToMove:event.target.value})
            },
            RE.FormControlLabel({label: "White to move", value: "WHITE", control: RE.Radio({})}),
            RE.FormControlLabel({label: "Black to move", value: "BLACK", control: RE.Radio({})}),
        )
    }

    return RE.Container.col.top.left({},{},
        renderPiecesToSelectFrom(),
        renderColorSelector()
    )
}