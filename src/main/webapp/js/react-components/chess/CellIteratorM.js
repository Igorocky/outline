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

    return RE.Container.row.right.bottom({}, {style:{marginLeft:"1em"}},
        RE.span({style: {fontSize: "large"}},
            idx == -1
                ? "Start"
                : (idx < quiz.length ? (quiz[idx].question + " : " + quiz[idx].answer) : numberOfPieces)
        ),
        RE.Paper({},
            RE.Container.col.top.right({},{},
                iconButton({iconName: "arrow_back_ios", onClick: prevClicked}),
                iconButton({iconName: "arrow_forward_ios", onClick: nextClicked}),
            )
        )
    )
}