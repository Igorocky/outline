
const StartGameAnalysisDialog = ({initDepth, initNumberOfThreads, onCancel, onStartAnalysis}) => {
    const [depth, setDepth] = useState(initDepth)
    const [numberOfThreads, setNumberOfThreads] = useState(initNumberOfThreads)

    return RE.Dialog({open:true},
        RE.DialogTitle({},
            "Start analysis"
        ),
        RE.DialogContent({},
            RE.Container.col.top.right({},{style:{marginBottom:"10px"}},
                RE.Container.row.right.center({},{style:{marginLeft:"10px"}},
                    RE.span({}, "Depth: "),
                    RE.TextField({
                        autoFocus: true,
                        value: depth,
                        variant: "outlined",
                        onChange: e => setDepth(e.target.value),
                    })
                ),
                RE.Container.row.right.center({},{style:{marginLeft:"10px"}},
                    RE.span({}, "Number of threads: "),
                    RE.TextField({
                        value: numberOfThreads,
                        variant: "outlined",
                        onChange: e => setNumberOfThreads(e.target.value),
                    })
                )
            )
        ),
        RE.DialogActions({},
            RE.Button({onClick: onCancel}, "Cancel"),
            RE.Button({color:"primary", variant:"contained",
                onClick: () => onStartAnalysis({depth:depth,numberOfThreads:numberOfThreads})}, "Start")
        )
    )
}