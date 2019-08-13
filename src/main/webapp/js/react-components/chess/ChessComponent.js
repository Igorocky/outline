const ChessComponent = () => {

    const [chessComponentState, setChessComponentState] = useState(null)

    function setRootComponentState(newState) {
        setChessComponentState(newState)
    }

    useEffect(() => {
        doRpcCall("initialState", {}, setRootComponentState)
    }, [])

    function renderChessBoard() {
        return re(ChessBoard,{key:"ChessBoard", setRootComponentState:setRootComponentState,
            ...chessComponentState.chessBoard})
    }

    function renderAvailablePieces() {
        if (chessComponentState.availablePieces) {
            return re(AvailablePiecesList,{key:"AvailablePiecesList", setRootComponentState:setRootComponentState,
                availablePieces:chessComponentState.availablePieces})
        } else {
            return null
        }
    }

    function renderPageContent() {
        if (chessComponentState) {
            return re(Container.row.left.top, {
                    childStyle:{marginLeft:"5px",marginTop:"5px"}
                },
                renderChessBoard(),
                renderAvailablePieces()
            )
        } else {
            return re(LinearProgress, {key:"LinearProgress",color:"secondary"})
        }
    }

    return renderPageContent()
}