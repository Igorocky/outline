"use strict";

const CellIteratorM = ({numberOfPieces, quiz}) => {
    const [idx, setIdx] = useState(-1)

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

    return RE.Paper({},
        RE.Container.row.center.center({}, {},
            iconButton({iconName: "arrow_back_ios", onClick: prevClicked}),
            RE.span({style: {fontSize: "large"}},
                idx == -1
                    ? "Start"
                    : (idx < quiz.length ? (quiz[idx].question + " : " + quiz[idx].answer) : numberOfPieces)
            ),
            iconButton({iconName: "arrow_forward_ios", onClick: nextClicked}),
        )
    )
}