"use strict";

const CellIterator = ({cells, backend}) => {
    const [idx, setIdx] = useState(-1)

    const size = (cellSize*5) + "px"

    function nextClicked() {
        if (idx < cells.length) {
            setIdx(oldIdx => oldIdx+1)
        } else {
            backend.call("execChessCommand", {command: "b"})
        }
    }

    return RE.Container.row.center.center(
        {style: {width:size, height:size}},
        {style: {marginRight:"15px"}},
        RE.span({style: {fontSize:"50px"}},
            idx == -1
                ? "Start"
                : (idx >= cells.length ? (cells.length-3) : cells[idx])
        ),
        idx > cells.length ? null : RE.Button({onClick: nextClicked}, "next")
    )
}