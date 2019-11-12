
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

    function renderArrow(fromTo) {
        return SVG.line({
            x1:fromTo.from.x, x2:fromTo.to.x, y1:fromTo.from.y, y2:fromTo.to.y,
            style:{stroke:"blue", strokeWidth:"10"}
        })
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