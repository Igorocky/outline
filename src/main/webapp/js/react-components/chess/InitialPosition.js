const InitialPosition = ({backend, availableChessmanTypes, colorToMove,
                             whiteLongCastlingIsAvailable,
                             whiteShortCastlingIsAvailable,
                             blackLongCastlingIsAvailable,
                             blackShortCastlingIsAvailable,
                                fen,}) => {

    const width = _.size(availableChessmanTypes);
    const height = _.size(availableChessmanTypes[0]);

    function renderInitialSetupButtons() {
        return RE.Container.row.left.top({},{},
            RE.Button({
                onClick: () => backend.call("setPositionFromFen", {
                    fen:"8/8/8/8/8/8/8/8 w - - 0 1"
                })
            }, "Empty board"),
            RE.Button({
                onClick: () => backend.call("setPositionFromFen", {
                    fen:"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
                })
            }, "Set initial position"),
        )
    }

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

    function renderFen() {
        return "FEN: " + fen
    }

    const [fenTextFieldValue, setFenTextFieldValue] = useState(null)
    function renderSetFromFenTextField() {
        function onKeyDown(event) {
            if (event.keyCode == 13){
                backend.call("setPositionFromFen", {fen:fenTextFieldValue})
            } else if (event.keyCode == 27) {
                setFenTextFieldValue(null)
            }
        }
        if (!fenTextFieldValue) {
            return RE.Button({onClick: () => setFenTextFieldValue(" ")}, "Set position from FEN")
        } else {
            return RE.TextField({
                autoFocus: true,
                style: {width:"550px"},
                onKeyDown: onKeyDown,
                value: fenTextFieldValue?fenTextFieldValue:"",
                variant: "outlined",
                onChange: e => setFenTextFieldValue(e.target.value),
            })
        }
    }

    const [pgnTextFieldValue, setPgnTextFieldValue] = useState(null)
    function renderLoadPgnTextArea() {
        function onKeyDown(event) {
            if (event.keyCode == 27) {
                setPgnTextFieldValue(null)
            }
        }
        if (!pgnTextFieldValue) {
            return RE.Button({onClick: () => setPgnTextFieldValue(" ")}, "Load PGN")
        } else {
            return RE.Container.col.top.right({},{style:{marginBottom:"5px"}},
                RE.TextField({
                    autoFocus: true,
                    multiline: true,
                    rowsMax: 30,
                    style: {width:"550px"},
                    onKeyDown: onKeyDown,
                    value: pgnTextFieldValue?pgnTextFieldValue:"",
                    variant: "outlined",
                    onChange: e => setPgnTextFieldValue(e.target.value),
                }),
                RE.Button({color:"primary", variant:"contained", onClick: () => backend.call(
                        "loadFromPgn",
                        {pgn:pgnTextFieldValue, tabToOpen:CHESS_COMPONENT_STAGE.moves},
                )}, "Load")
            )
        }
    }

    return RE.Container.col.top.left({},{},
        renderInitialSetupButtons(),
        renderPiecesToSelectFrom(),
        renderColorSelector(),
        renderCastlingAvailability(),
        renderFen(),
        renderSetFromFenTextField(),
        renderLoadPgnTextArea(),
    )
}