const cellSize = 65;
const cellStyle = {
    width: cellSize + "px",
    height: cellSize + "px",
    fontSize: cellSize*0.9 + "px",
    lineHeight: cellSize*1.08 + "px",
}

const ChessBoardCell = ({setRootComponentState, coords,backgroundColor,borderColor,code}) => {

    function clicked() {
        doRpcCall("cellLeftClicked", {coords:coords}, setRootComponentState)
    }

    function getCursorType() {
        if (code == 0) {
            return "default"
        } else {
            return "pointer"
        }
    }

    function codeToStr(code) {
        if (code == 0) {
            return ""
        } else {
            return String.fromCharCode(code)
        }
    }

    return re('div', {style: {
                ...cellStyle,
                backgroundColor:backgroundColor,
                ...(borderColor?{backgroundColor:borderColor}:{}),
                cursor:getCursorType()},
            onClick: clicked},
            codeToStr(code)
    )
}