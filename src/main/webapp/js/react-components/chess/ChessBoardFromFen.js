
const ChessBoardFromFen = React.memo( ({fen, moveFromTo, wPlayer, bPlayer, flipped}) => {

    const fromX = moveFromTo?moveFromTo.charCodeAt(0)-97:null
    const fromY = moveFromTo?moveFromTo.charCodeAt(1)-49:null
    const toX = moveFromTo?moveFromTo.charCodeAt(2)-97:null
    const toY = moveFromTo?moveFromTo.charCodeAt(3)-49:null
    const board = _.map(_.range(0, 8), x => _.map(_.range(0, 8), y => ({
        x:flipped?7-x:x,y:flipped?7-y:y,selected: x==fromX && y==fromY || x==toX && y==toY
    })))

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
            )
        ),
        lowerPlayer,
    )
}, (o,n) => o.fen == n.fen && o.flipped == n.flipped)