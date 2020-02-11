"use strict";

const CellIterator = ({positionDescription, numberOfPieces, quiz, backend}) => {
    const [idx, setIdx] = useState(-1)

    const size = (cellSize*8) + "px"

    function nextClicked() {
        if (idx < positionDescription.length) {
            setIdx(oldIdx => oldIdx+1)
        }
    }

    return RE.Container.col.top.left({},{},
        RE.Paper({style:{margin:"15px", padding:"10px"}},re(ChessPositionQuiz, {cards:quiz})),
        RE.Paper({style:{margin:"15px", padding:"10px"}},RE.Container.row.left.top(
            {style: {width:size}},
            {style: {marginRight:"15px"}},
            idx >= positionDescription.length ? null : RE.Button({onClick: nextClicked}, "next"),
            RE.span({style: {fontSize:"20px"}},
                idx == -1
                    ? "Start"
                    : (idx < positionDescription.length ? positionDescription[idx] : numberOfPieces)
            )
        )),
    )
}