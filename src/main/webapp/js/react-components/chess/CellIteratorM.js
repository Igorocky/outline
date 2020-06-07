"use strict";

const CellIteratorM = ({numberOfPieces, quiz}) => {
    const [idx, setIdx] = useState(-1)

    function nextClicked() {
        setIdx(oldIdx => oldIdx+1)
    }

    function prevClicked() {
        if (-1 < idx) {
            setIdx(oldIdx => oldIdx-1)
        }
    }

    function renderQuizCard(card) {
        if (card.question) {
            return card.question + " : " + card.answer
        } else {
            return card.answer
        }
    }

    if (idx > quiz.length) {
        return RE.Button({onClick: () => setIdx(0)}, numberOfPieces)
    } else {
        return RE.Container.row.right.bottom({}, {style:{marginLeft:"1em"}},
            RE.span({style: {fontSize: "large"}},
                idx == -1
                    ? "Start"
                    : (idx < quiz.length ? renderQuizCard(quiz[idx]) : numberOfPieces)
            ),
            RE.Paper({},
                RE.Container.col.top.right({},{},
                    iconButton({iconName: "arrow_back_ios", onClick: prevClicked}),
                    iconButton({iconName: "arrow_forward_ios", onClick: nextClicked}),
                )
            )
        )
    }
}