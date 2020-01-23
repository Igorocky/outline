"use strict";

const SequencePractice = ({backend, waitingForNextMove, colorToMove, incorrectMove, failed, history, fen}) => {

    function renderNextActionDescription() {
        if (waitingForNextMove) {
            return RE.Container.row.left.center({},{},
                colorToMove + " to move.",
                RE.Button({onClick: () => backend.call("showCorrectMove", {})}, "Show move"),
            )
        } else {
            return null
        }
    }

    function getOverallStatus() {
        if (failed) {
            return RE.span({style:{color:waitingForNextMove?"orange":"red", fontWeight:"bold"}},"FAILED")
        } else if (!waitingForNextMove) {
            return RE.span({style:{color:"limegreen", fontWeight:"bold"}},"PASSED")
        } else {
            return null
        }
    }

    function renderProgressStatus() {
        return RE.Container.row.left.center({},{},
            RE.span({},waitingForNextMove?"In progress... ":"Completed: "),
            getOverallStatus()
        )
    }

    return RE.Container.row.left.top({},{style:{marginRight:"15px"}},
        re(History,{backend:backend, ...history}),
        RE.Container.col.top.left({},{},
            renderProgressStatus(),
            renderNextActionDescription(),
            RE.span({style:{color:"red"}},incorrectMove?"Incorrect move!":""),
            RE.Button({
                onClick: () => window.open(
                    PATH.createChessboardWithFen(fen.replace(/ /g,"_").replace(/\//g,"+"))
                )
            }, "play"),

        )
    )
}