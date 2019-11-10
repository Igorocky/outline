
const ChessBoardFromFen = React.memo( ({fen, moveFromTo}) => {

    const board = _.map(_.range(0, 8), x => _.map(_.range(0, 8), y => ({x:x,y:y})))

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

    return RE.svg({width:cellSize*8, height:cellSize*8},
        _.range(7, -1, -1).map(y =>
            _.range(0, 8).map(x => re(ChessBoardCellFromFen, {
                key:x+"-"+y,
                chCode:board[x][y].chCode,
                x:x,y:y,
                selected:board[x][y].selected
            }))
        )
    )
}, (o,n) => o.fen == n.fen)