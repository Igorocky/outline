"use strict";

const PgnEditor = ({pgnStr, onSave, onCancel}) => {
    return re(ChessComponent, {
        showPracticeTab:true,
        onBackendCreated: backend => backend.call(
            "loadFromPgn", {
                pgn:pgnStr,
                tabToOpen:pgnStr?CHESS_COMPONENT_STAGE.moves:CHESS_COMPONENT_STAGE.initialPosition
            }),
        onSave:onSave,
        onCancel:onCancel,
    })
}