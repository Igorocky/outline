"use strict";

const HistoryM = ({backend, startPositionSelected, rows}) => {

    return RE.Paper({},
        RE.span({
            key:"-1",
            style: {backgroundColor: startPositionSelected ? "yellow" : null},
            onClick: () => backend.call("execChessCommand", {command:"s"}),
        }, "Start "),
        rows.map(move => RE.span({key:move.feMoveNumber},
            RE.span({key:"-1", style:{fontWeight:"bold", color:"blue"}}, move.feMoveNumber + ". "),
            RE.span({
                    key:"w",
                    style: {backgroundColor: move.whitesMoveSelected ? "yellow" : null},
                    onClick: () => move.whitesMove
                        ?backend.call("execChessCommand", {command:"g " + move.feMoveNumber + "w"})
                        :null,
                },
                emptyStrIfNull(move.whitesMove) + " "
            ),
            RE.span({
                    key:"b",
                    style: {backgroundColor: move.blacksMoveSelected ? "yellow" : null},
                    onClick: () => move.blacksMove
                        ?backend.call("execChessCommand", {command:"g " + move.feMoveNumber + "b"})
                        :null,
                },
                emptyStrIfNull(move.blacksMove) + " "
            )
        ))
    )
}