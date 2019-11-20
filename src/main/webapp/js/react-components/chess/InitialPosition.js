const InitialPosition = ({backend, availableChessmanTypes, colorToMove,
                             whiteLongCastlingIsAvailable,
                             whiteShortCastlingIsAvailable,
                             blackLongCastlingIsAvailable,
                             blackShortCastlingIsAvailable,}) => {

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

    function renderCastlingAvailability() {
        return RE.Container.col.top.left({},{},
            RE.Container.row.left.top({},{},
                RE.FormControlLabel({
                    labelPlacement:"start",
                    label:"Black O-O-O",
                    control: RE.Checkbox({
                        checked: blackLongCastlingIsAvailable,
                        onClick: () => backend.call("changeCastlingAvailability", {color:"BLACK", isLong: true}),
                    }),
                    style:{marginRight:"20px"}
                }),
                RE.FormControlLabel({
                    labelPlacement:"end",
                    label:"O-O",
                    control: RE.Checkbox({
                        checked: blackShortCastlingIsAvailable,
                        onClick: () => backend.call("changeCastlingAvailability", {color:"BLACK", isLong: false}),
                    })
                }),
            ),
            RE.Container.row.left.top({},{},
                RE.FormControlLabel({
                    labelPlacement:"start",
                    label:"White O-O-O",
                    control: RE.Checkbox({
                        checked: whiteLongCastlingIsAvailable,
                        onClick: () => backend.call("changeCastlingAvailability", {color:"WHITE", isLong: true}),
                    }),
                    style:{marginRight:"20px"}
                }),
                RE.FormControlLabel({
                    labelPlacement:"end",
                    label:"O-O",
                    control: RE.Checkbox({
                        checked: whiteShortCastlingIsAvailable,
                        onClick: () => backend.call("changeCastlingAvailability", {color:"WHITE", isLong: false}),
                    })
                }),
            )
        )
    }

    return RE.Container.col.top.left({},{},
        renderPiecesToSelectFrom(),
        renderColorSelector(),
        renderCastlingAvailability()
    )
}