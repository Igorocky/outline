"use strict";

const CellIterator = ({cells}) => {
    const [idx, setIdx] = useState(-1)

    const size = (cellSize*5) + "px"

    return RE.Container.row.center.center(
        {style: {width:size, height:size}},
        {style: {marginRight:"15px"}},
        RE.span({style: {fontSize:"50px"}},
            idx == -1
                ? "Start"
                : (idx >= cells.length ? cells.length : cells[idx])
        ),
        idx >= cells.length ? null : RE.Button({onClick: () => setIdx(oldIdx => oldIdx+1)}, "next")
    )
}