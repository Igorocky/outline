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

const PUZZLES_TO_REPEAT_TABS = {
    puzzles:{title: "Puzzles To Repeat", id: "puzzles"},
    comments:{title: "Comments", id: "comments"},
}

const PuzzlesToRepeatReport = () => {
    const [reportData, setReportData] = useState(null)
    const [currTabId, setCurrTabId] = useState(PUZZLES_TO_REPEAT_TABS.puzzles.id)

    useEffect(() => doRpcCall(
        "rpcRunReport",
        {name:currTabId == PUZZLES_TO_REPEAT_TABS.puzzles.id?"puzzles-to-repeat":"puzzle-comments"},
        res => setReportData(res)
    ),[currTabId])

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

    function renderCurrReport() {
        if (reportData) {
            return renderReport()
        } else {
            return RE.CircularProgress({})
        }
    }

    function renderTabs() {
        return reTabs({
            selectedTab:currTabId,
            onTabSelected: newTabId => setCurrTabId(newTabId),
            tabs: {
                [PUZZLES_TO_REPEAT_TABS.puzzles.id]: {
                    label: PUZZLES_TO_REPEAT_TABS.puzzles.title,
                    render: renderCurrReport
                },
                [PUZZLES_TO_REPEAT_TABS.comments.id]: {
                    label: PUZZLES_TO_REPEAT_TABS.comments.title,
                    render: renderCurrReport
                },
            }
        })
    }

    return renderTabs()
}