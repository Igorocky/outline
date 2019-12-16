const cellSize = 60;
const codeToImgArr = {
    9817:"Chess_plt45",
    9816:"Chess_nlt45",
    9815:"Chess_blt45",
    9814:"Chess_rlt45",
    9813:"Chess_qlt45",
    9812:"Chess_klt45",
    9823:"Chess_pdt45",
    9822:"Chess_ndt45",
    9821:"Chess_bdt45",
    9820:"Chess_rdt45",
    9819:"Chess_qdt45",
    9818:"Chess_kdt45",
    10007:"delete-icon",
}

const ChessBoardCell = ({backend,xShift,yShift,coords,backgroundColor,borderColor,code}) => {

    function clicked() {
        backend.call("cellLeftClicked", {coords:coords})
    }

    function getCursorType() {
        if (code == 0) {
            return "default"
        } else {
            return "pointer"
        }
    }

    function codeToImg(code) {
        if (code == 0) {
            return null
        } else {
            return "/img/chess/" + codeToImgArr[code] + ".svg"
        }
    }

    const cellXPos = (coords.x+(xShift?xShift:0))*cellSize
    const cellYPos = (7-(coords.y+(yShift?yShift:0)))*cellSize
    return RE.Fragment({},
        SVG.rect({
            x:cellXPos, y:cellYPos, width:cellSize, height:cellSize,
            style:{fill:backgroundColor},
            onClick:clicked
        }),
        borderColor
            ?SVG.rect({
                x:cellXPos, y:cellYPos, width:cellSize, height:cellSize,
                style:{fill:borderColor, fillOpacity:"0.6"},
                onClick:clicked
            })
            :null,
        code == 0
            ?null
            :SVG.image({
                x:cellXPos, y:cellYPos, width:cellSize, height:cellSize,
                href:codeToImg(code),
                onClick:clicked
            })
    )
}