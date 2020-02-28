"use strict";

const PuzzleProgressM = ({waitingForNextMove, incorrectMove, failed}) => {

    function renderCurrentStatus() {
        if (waitingForNextMove) {
            return RE.Fragment({},
                RE.span({},
                    "Puzzle in progress..."
                ),
                incorrectMove?RE.span({style:{color:"red"}},
                    "Incorrect move - " + incorrectMove
                ):null
            )
        } else {
            if (failed) {
                return RE.span({style:{color:"red"}}, "Puzzle FAILED")
            } else {
                return RE.span({style:{color:"limegreen"}}, "Puzzle PASSED")
            }
        }
    }

    return renderCurrentStatus()
}