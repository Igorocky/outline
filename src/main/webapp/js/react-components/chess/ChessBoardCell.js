const cellSize = 75;
const cellStyle = {
    width: cellSize + "px",
    height: cellSize + "px",
    fontSize: cellSize*0.9 + "px",
    lineHeight: cellSize*1.08 + "px",
}

const ChessBoardCell = ({setComponentState, x,y,backgroundColor,highlighted}) => {

    function clicked() {
        doRpcCall("cellClicked",{x:x,y:y}, resp => setComponentState(resp))
    }

    return re('div', {key:x+":"+y,
                style: {...cellStyle, backgroundColor:backgroundColor, onClick: clicked},
            },
            String.fromCharCode(9816)
    )
}