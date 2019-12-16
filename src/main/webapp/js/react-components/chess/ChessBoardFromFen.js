
const ChessBoardFromFen = React.memo( ({fen, moveFromTo, wPlayer, bPlayer, flipped, arrow}) => {

    const fromX = moveFromTo?moveFromTo.charCodeAt(0)-97:null
    const fromY = moveFromTo?moveFromTo.charCodeAt(1)-49:null
    const toX = moveFromTo?moveFromTo.charCodeAt(2)-97:null
    const toY = moveFromTo?moveFromTo.charCodeAt(3)-49:null
    const board = _.map(_.range(0, 8), x => _.map(_.range(0, 8), y => ({
        x:cellCoordsAfterFlip(flipped,x),
        y:cellCoordsAfterFlip(flipped,y),
        selected: x==fromX && y==fromY || x==toX && y==toY
    })))

    function cellCoordsAfterFlip(flipped, coord) {
        return flipped?7-coord:coord
    }

    if (fen) {
        decodeFen(fen)
    } else {
        decodeFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq -")
    }

    function decodeFen(fen) {
        let cellPointer = 0;
        let charPointer = 0;
        while (cellPointer < 64) {
            cellPointer = processNextCharInFen(fen.charCodeAt(charPointer), cellPointer);
            charPointer++;
        }
    }

    function processNextCharInFen(chCode, cellPointer) {
        if (chCodeToImg[chCode]) {
            let x = cellPointer%8;
            let y = 7-Math.floor(cellPointer/8);
            board[x][y].chCode = chCode
            return cellPointer + 1;
        } else if(chCode == 47/*"/"*/) {
            return cellPointer;
        } else {
            return cellPointer + chCode - 48;
        }
    }

    const cellSizeFromFenCoeff = cellSizeFromFen/45
    const triangleLength = 35*cellSizeFromFenCoeff
    const triangleHeight = triangleLength
    const lineWidth = 15*cellSizeFromFenCoeff
    const halfLineWidth = Math.floor(lineWidth/2);
    const halfTriangleHeight = Math.floor(triangleHeight/2);
    function renderArrow({from, to}) {
        const dx = from.x-to.x
        const dy = from.y-to.y
        const length = Math.floor(Math.sqrt(dx*dx+dy*dy))
        const handleLength = length-triangleLength;
        let angle = Math.atan(dy/dx)*180/Math.PI
        // const possibleMoves = [
        //     {depth: 1, move: "e4e6"},
        //     {depth: 11, move: "e4f6"},
        //     {depth: 12, move: "e4g5"},
        //     {depth: 2, move: "e4g4"},
        //     {depth: 21, move: "e4g3"},
        //     {depth: 22, move: "e4f2"},
        //     {depth: 3, move: "e4e2"},
        //     {depth: 31, move: "e4d2"},
        //     {depth: 32, move: "e4c3"},
        //     {depth: 4, move: "e4c4"},
        //     {depth: 41, move: "e4c5"},
        //     {depth: 42, move: "e4d6"},
        // ]
        if (dx >= 0) {
            if (dy == 0) {
                angle = -angle+180
            } else {
                angle = angle+180
            }
        }
        return SVG.g({transform:"translate(" + from.x + ", " + from.y + ") rotate(" + angle + ")"},
            SVG.path({
                d:"M0,0"
                    +" L0,"+halfLineWidth
                    +" L"+handleLength+","+halfLineWidth
                    +" L"+handleLength+","+halfTriangleHeight
                    +" L"+length+",0"
                    +" L"+handleLength+","+(-halfTriangleHeight)
                    +" L"+handleLength+","+(-halfLineWidth)
                    +" L0,"+(-halfLineWidth)
                    + " Z",
                style:{stroke:"blue", fill:"blue", opacity:0.85}
            })
        )
    }

    function moveToArrowCoords(move,flipped) {
        const fromX = cellCoordsAfterFlip(flipped,move.charCodeAt(0)-97)
        const fromY = cellCoordsAfterFlip(flipped,move.charCodeAt(1)-49)
        const toX = cellCoordsAfterFlip(flipped,move.charCodeAt(2)-97)
        const toY = cellCoordsAfterFlip(flipped,move.charCodeAt(3)-49)

        return {
            from: {
                x: xCoordFromChessboardToSvg(fromX)+cellSizeFromFen/2,
                y: yCoordFromChessboardToSvg(fromY)+cellSizeFromFen/2,
            },
            to: {
                x: xCoordFromChessboardToSvg(toX)+cellSizeFromFen/2,
                y: yCoordFromChessboardToSvg(toY)+cellSizeFromFen/2,
            }
        }
    }

    const upperPlayer = flipped?wPlayer:bPlayer
    const lowerPlayer = flipped?bPlayer:wPlayer
    return RE.Container.col.top.left({},{},
        upperPlayer,
        RE.svg({width:cellSizeFromFen*8, height:cellSizeFromFen*8},
            _.range(7, -1, -1).map(y =>
                _.range(0, 8).map(x => re(ChessBoardCellFromFen, {
                    key:x+"-"+y,
                    chCode:board[x][y].chCode,
                    x:board[x][y].x,
                    y:board[x][y].y,
                    selected:board[x][y].selected
                }))
            ),
            arrow?renderArrow(moveToArrowCoords(arrow,flipped)):null
        ),
        lowerPlayer,
    )
}, (o,n) => o.fen == n.fen && o.flipped == n.flipped && o.arrow == n.arrow)