"use strict";

const ChessManagerAudioView = ({}) => {
    usePageTitle({pageTitleProvider: () => "ChessManagerAudioView", listenFor:[]})

    const backendState = useBackendState({
        stateType: "ChessManagerAudio",
        // onBackendStateCreated: processBackendStateCreated,
        // onMessageFromBackend: chessComponentResponse => {
        //     if (chessComponentResponse.chessComponentView) {
        //         setChessComponentState(chessComponentResponse.chessComponentView)
        //         setInitialPgnHashCode(currPgnHashCode => {
        //             if (currPgnHashCode == null) {
        //                 return chessComponentResponse.chessComponentView.pgn
        //             } else {
        //                 return currPgnHashCode
        //             }
        //         })
        //     }
        // }
    })


    return "ChessManagerAudioView"
}