const PUZZLES_TO_REPEAT_TABS = {
    puzzles:{title: "Puzzles To Repeat", id: "puzzles"},
    comments:{title: "Comments", id: "comments"},
}

const PuzzlesToRepeatReport = ({match, redirect}) => {
    const [reportData, setReportData] = useState(null)
    const currTabId = getByPath(match, ["params", "tab"])

    useEffect(() => {
        if (currTabId) {
            doRpcCall(
                "rpcRunReport",
                {name: currTabId == PUZZLES_TO_REPEAT_TABS.puzzles.id ? "puzzles-to-repeat" : "puzzle-comments"},
                res => setReportData(res)
            )
            document.title = currTabId == PUZZLES_TO_REPEAT_TABS.puzzles.id
                ? "Puzzles to repeat"
                : "Puzzle comments"
        } else {
            setReportData(undefined)
            document.title = APP_CONFIG_NAME
        }
    }, [currTabId])

    function navigateToPuzzle(puzzleId) {
        window.open(PATH.createNodeWithIdPath(puzzleId))
    }

    function renderStartPuzzle(puzzleId, url, hasPgn) {
        return re(PuzzlesToRepeatReport_IconButtonWithMemory,{onClick: () => {
                window.open(PATH.createNodeWithIdPath(puzzleId))
                if (hasPgn == 1) {
                    window.open(PATH.createChessboardWithPractice(puzzleId))
                } else {
                    window.open(url)
                }
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
            onTabMouseUp: (event,tabId) => link(redirect, PATH.createPuzzlesToRepeatPath(tabId)).onMouseUp(event),
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

    if (!currTabId) {
        redirect(PATH.createPuzzlesToRepeatPath(PUZZLES_TO_REPEAT_TABS.puzzles.id))
        return RE.LinearProgress({})
    } else {
        return renderTabs()
    }
}

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
