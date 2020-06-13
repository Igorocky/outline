"use strict";

const STATE_INFO_DTO = {
    stateId: "stateId",
    stateType: "stateType",
    createdAt: "createdAt",
    lastInMsgAt: "lastInMsgAt",
    lastOutMsgAt: "lastOutMsgAt",
    viewRepresentation: "viewRepresentation",
}

const BeStatesView = ({}) => {
    usePageTitle({pageTitleProvider: () => "BE States", listenFor:[]})

    const [beStates, setBeStates] = useState(null)

    useEffect(() => {
        doRpcCall("listBeStates", {}, beStatesResponse => {
            setBeStates(beStatesResponse)
        })
    }, [])

    if (!beStates) {
        return RE.LinearProgress({})
    } else {
        return paper(RE.Table({size:"small"},
            RE.TableHead({},
                RE.TableRow({},
                    RE.TableCell({}, "ID"),
                    RE.TableCell({}, "TYPE"),
                    RE.TableCell({}, "CREATED AT"),
                    RE.TableCell({}, "IN MSG"),
                    RE.TableCell({}, "OUT MSG"),
                    RE.TableCell({}, "STATE"),
                )
            ),
            RE.TableBody({},
                beStates.map((beState,idx) => RE.TableRow({key:beState[STATE_INFO_DTO.stateId], className:"grey-background-on-hover"},
                    RE.TableCell({}, beState[STATE_INFO_DTO.stateId]),
                    RE.TableCell({}, beState[STATE_INFO_DTO.stateType]),
                    RE.TableCell({}, beState[STATE_INFO_DTO.createdAt]),
                    RE.TableCell({}, beState[STATE_INFO_DTO.lastInMsgAt]),
                    RE.TableCell({}, beState[STATE_INFO_DTO.lastOutMsgAt]),
                    RE.TableCell({}, RE.div({style:{maxWidth:"300px", maxHeight:"100px", overflow:"scroll"}},
                        JSON.stringify(beState[STATE_INFO_DTO.viewRepresentation])
                    )),
                ))
            )
        ))
    }
}