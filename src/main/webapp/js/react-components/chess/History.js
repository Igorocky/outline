"use strict";

const History = ({backend, startPositionSelected, rows}) => {
    return RE.Paper({style:{maxHeight:"450px", overflow: "scroll"}},RE.Table(
        {size:"small"},
        RE.TableBody({},
            RE.TableRow({key: "-1"}, RE.TableCell({
                    colSpan: 3,
                    style: {backgroundColor: startPositionSelected ? "yellow" : null},
                    className: "grey-background-on-hover pointer-on-hover",
                    onClick: () => backend.call("execChessCommand", {command:"s"})
                },
                "Start"
            )),
            rows.map(move => RE.TableRow({key:move.feMoveNumber},
                RE.TableCell({key:"-1"}, move.feMoveNumber),
                RE.TableCell({
                        key:"w",
                        style: {backgroundColor: move.whitesMoveSelected ? "yellow" : null},
                        className:"grey-background-on-hover pointer-on-hover",
                        onClick: () => backend.call("execChessCommand", {command:"g " + move.feMoveNumber + "w"}),
                    },
                    move.whitesMove
                ),
                RE.TableCell({
                        key:"b",
                        style: {backgroundColor: move.blacksMoveSelected ? "yellow" : null},
                        className:"grey-background-on-hover pointer-on-hover",
                        onClick: () => backend.call("execChessCommand", {command:"g " + move.feMoveNumber + "b"}),
                    },
                    move.blacksMove
                )
            ))
        )
    ))
}