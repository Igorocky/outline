"use strict";

const CellIterator = ({positionDescription, quiz, backend}) => {
    const [idx, setIdx] = useState(-1)

    const size = (cellSize*5) + "px"

    function nextClicked() {
        if (idx < positionDescription.length) {
            setIdx(oldIdx => oldIdx+1)
        }
    }

    return RE.Container.col.top.left({},{},
        RE.Paper({style:{margin:"15px", padding:"10px"}},re(ChessPositionQuiz, {cards:quiz})),
        RE.Paper({style:{margin:"15px"}},RE.Container.row.center.center(
            {style: {width:size}},
            {style: {marginRight:"15px"}},
            RE.span({style: {fontSize:"50px"}},
                idx == -1
                    ? "Start"
                    : (idx >= positionDescription.length ? (positionDescription.length-3) : positionDescription[idx])
            ),
            idx >= positionDescription.length ? null : RE.Button({onClick: nextClicked}, "next")
        )),
    )
}