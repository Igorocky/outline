"use strict";

const CellSpinner = ({cells}) => {
    const [idx, setIdx] = useState(-1)

    const size = (cellSize*5) + "px"

    return RE.Container.row.center.center(
        {
            style: {width:size, height:size},
            onClick: () => setIdx(idx+1)
        },{},
        RE.span({style: {fontSize:"50px"}},
            idx == -1
                ? "Start"
                : (idx >= cells.length ? "End" : cells[idx])
        )
    )
}