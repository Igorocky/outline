"use strict";

const PgnEditor = ({pgnStr, autoResponse, onSave, onCancel}) => {
    return re(ChessComponent, {
        showPracticeTab:true,
        onBackendCreated: backend => backend.call(
            "loadFromPgn", {
                pgn:pgnStr,
                tabToOpen:pgnStr?CHESS_COMPONENT_STAGE.moves:CHESS_COMPONENT_STAGE.initialPosition,
                autoResponse:autoResponse
            }),
        onSave:onSave,
        onCancel:onCancel,
    })
}