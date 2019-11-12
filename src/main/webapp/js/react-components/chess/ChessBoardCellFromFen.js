const cellSizeFromFen = 45;
function xCoordFromChessboardToSvg(x) {
    return x*cellSizeFromFen
}

function yCoordFromChessboardToSvg(y) {
    return (7-y)*cellSizeFromFen
}

const chCodeToImg = {
    ["P".charCodeAt(0)]:"Chess_plt45",
    ["N".charCodeAt(0)]:"Chess_nlt45",
    ["B".charCodeAt(0)]:"Chess_blt45",
    ["R".charCodeAt(0)]:"Chess_rlt45",
    ["Q".charCodeAt(0)]:"Chess_qlt45",
    ["K".charCodeAt(0)]:"Chess_klt45",
    ["p".charCodeAt(0)]:"Chess_pdt45",
    ["n".charCodeAt(0)]:"Chess_ndt45",
    ["b".charCodeAt(0)]:"Chess_bdt45",
    ["r".charCodeAt(0)]:"Chess_rdt45",
    ["q".charCodeAt(0)]:"Chess_qdt45",
    ["k".charCodeAt(0)]:"Chess_kdt45",
}

const ChessBoardCellFromFen = ({chCode,x,y,selected}) => {

    function codeToImg(code) {
        if (code == 0) {
            return null
        } else {
            return "/img/chess/" + chCodeToImg[code] + ".svg"
        }
    }

    const cellXPos = xCoordFromChessboardToSvg(x)
    const cellYPos = yCoordFromChessboardToSvg(y)
    return RE.Fragment({},
        SVG.rect({
            x:cellXPos, y:cellYPos, width:cellSizeFromFen, height:cellSizeFromFen,
            style:{fill:(x + y) % 2 == 0 ? "rgb(181,136,99)" : "rgb(240,217,181)"},
        }),
        selected
            ?SVG.rect({
                x:cellXPos, y:cellYPos, width:cellSizeFromFen, height:cellSizeFromFen,
                style:{fill:"green", fillOpacity:"0.4"},
            })
            :null,
        chCode
            ?SVG.image({
                x:cellXPos, y:cellYPos, width:cellSizeFromFen, height:cellSizeFromFen,
                href:codeToImg(chCode),
            }):null

    )
}