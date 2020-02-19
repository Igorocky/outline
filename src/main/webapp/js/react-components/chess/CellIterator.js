"use strict";

const CellIterator = ({numberOfPieces, quiz, backend}) => {
    const [idx, setIdx] = useState(-1)

    const size = (cellSize*8) + "px"

    function nextClicked() {
        if (idx < quiz.length) {
            setIdx(oldIdx => oldIdx+1)
        }
    }

    function prevClicked() {
        if (-1 < idx) {
            setIdx(oldIdx => oldIdx-1)
        }
    }

    return RE.Container.col.top.left({},{},
        RE.Paper({style:{marginBottom:"15px", padding:"10px"}},re(ChessPositionQuiz, {cards:quiz})),
        RE.Paper({style:{marginBottom:"15px", paddingTop:"5px"}},RE.Container.row.left.top(
            {style: {width:size}},
            {style: {marginRight:"15px"}},
            RE.Button({onClick: prevClicked, disabled: idx <= -1}, "prev"),
            RE.Button({onClick: nextClicked, disabled: quiz.length <= idx}, "next"),
            RE.span({style: {fontSize: "25px"}},
                idx == -1
                    ? "Start"
                    : (idx < quiz.length ? (quiz[idx].question + " : " + quiz[idx].answer) : numberOfPieces)
            ),
        )),
    )
}