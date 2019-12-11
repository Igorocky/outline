"use strict";

const SequencePractice = ({backend, waitingForNextMove, colorToMove, incorrectMove, failed,}) => {

    function renderNextActionDescription() {
        if (waitingForNextMove) {
            return RE.Container.row.left.top({},{},
                "Make next move for " + colorToMove + ".",
                RE.Button({onClick: () => backend.call("showCorrectMove", {})}, "Show move")
            )
        } else {
            return failed?"":"PASSED"
        }
    }

    return RE.Container.col.top.left({},{},
        failed?"FAILED":"",
        renderNextActionDescription(),
        incorrectMove?"Incorrect move!":"",
    )
}