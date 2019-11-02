const PuzzlesToRepeatReport_IconButtonWithMemory = ({onClick}) => {
    const [clicked, setClicked] = useState(false)
    const [hovered, setHovered] = useState(false);

    function getColor() {
        return clicked?"green":(hovered?"yellow":"grey")
    }

    function getIconName() {
        return clicked?"play_circle_filled":"play_circle_outline"
    }

    return RE.IconButton({
            size: "small",
            color: "inherit",
            onClick: () => {
                setClicked(true)
                onClick()
            },
            onMouseEnter: () => setHovered(true),
            onMouseLeave: () => setHovered(false),
            style: {color: getColor()}
        },
        RE.Icon({}, getIconName())
    )
}

const PuzzlesToRepeatReport = () => {
    const [reportData, setReportData] = useState(null)

    useEffect(() => doRpcCall(
        "rpcRunReport",
        {name:"puzzles-to-repeat"},
        res => setReportData(res)
    ),[])

    function navigateToPuzzle(puzzleId) {
        window.open(PATH.createNodeWithIdPath(puzzleId))
    }

    function renderStartPuzzle(puzzleId, url) {
        return re(PuzzlesToRepeatReport_IconButtonWithMemory,{onClick: () => {
                window.open(PATH.createNodeWithIdPath(puzzleId))
                window.open(url)
        }})
    }

    function renderReport() {
        return re(ReportResult, {...reportData, actions: {
                navigateToPuzzle: navigateToPuzzle,
                renderStartPuzzle: renderStartPuzzle,
        }})
    }

    return RE.Container.col.top.left({},{style: {marginBottom:"10px"}},
        RE.span({style:{fontSize:"30px"}}, "Puzzles To Repeat"),
        reportData
            ?renderReport()
            :RE.CircularProgress({})
    )
}