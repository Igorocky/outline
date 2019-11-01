const PuzzlesToRepeatReport = () => {
    const [reportData, setReportData] = useState(null)

    useEffect(() => doRpcCall(
        "rpcRunReport",
        {name:"puzzles-to-repeat"},
        res => setReportData(res)
    ),[])

    function renderReport() {
        return re(ReportResult, {...reportData})
    }

    return RE.Container.col.top.left({},{style: {marginBottom:"10px"}},
        RE.span({style:{fontSize:"30px"}}, "Active Puzzles"),
        reportData
            ?renderReport()
            :RE.CircularProgress({})
    )
}