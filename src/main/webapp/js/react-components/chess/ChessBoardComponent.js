const ChessBoardComponent = () => {

    const [chessBoardComponentState, setChessBoardComponentState] = useState(null)

    useEffect(() => {
        doRpcCall("initialChessboard", {}, resp => setChessBoardComponentState(resp))
    }, [])

    function renderChessBoard() {
        return re(ChessBoard,{key:"ChessBoard", setComponentState:setChessBoardComponentState,
            ...chessBoardComponentState.chessBoard})
    }

    function renderPageContent() {
        if (chessBoardComponentState) {
            return [
                renderChessBoard()
            ]
        } else {
            return re(LinearProgress, {key:"LinearProgress",color:"secondary"})
        }
    }

    return renderPageContent()
}